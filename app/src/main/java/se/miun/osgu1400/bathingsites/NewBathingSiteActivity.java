package se.miun.osgu1400.bathingsites;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

public class NewBathingSiteActivity extends AppCompatActivity {

    private NewBathingSiteFragment newBathingSiteFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_bathing_site);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setNumberOfBathingSites();

        newBathingSiteFragment = (NewBathingSiteFragment) getSupportFragmentManager().findFragmentById(R.id.newBathingSiteFragment);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set number of bathing sites every time the activity is shown
        setNumberOfBathingSites();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu
        getMenuInflater().inflate(R.menu.menu_new_bathing_site, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.clear:
                clearButtonPressed();
                return true;
            case R.id.save:
                saveButtonPressed();
                return true;
            case R.id.weather:
                weatherButtonPressed();
                return true;
            case R.id.settings:
                settingsButtonPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Reset all fields
    private void clearButtonPressed() {
        newBathingSiteFragment.clearAllFields();
    }

    private void saveButtonPressed() {

        // If validateInput is true, it means that validation has passed
        // and all required fields are filled out
        if (newBathingSiteFragment.validateInput()) {

            String name = newBathingSiteFragment.getName().getText().toString();
            String description = newBathingSiteFragment.getDescription().getText().toString();
            String address = newBathingSiteFragment.getAddress().getText().toString();
            String longitude = newBathingSiteFragment.getLongitude().getText().toString();
            String latitude = newBathingSiteFragment.getLatitude().getText().toString();
            String waterTemp = newBathingSiteFragment.getWaterTemp().getText().toString();
            String dateForTemp = newBathingSiteFragment.getDateForTemp().getText().toString();
            Float grade = newBathingSiteFragment.getRatingBar().getRating();

            SQLDatabase myDb = new SQLDatabase(this);

            if (myDb.bathingSiteExists(longitude,latitude) && !longitude.isEmpty() && !latitude.isEmpty()) {
                // Bathing site already exists in the database
                Snackbar snackbar = Snackbar.make(findViewById(R.id.save),
                        "Bathing site with coordinates (" + longitude + "," + latitude + ") already exists", Snackbar.LENGTH_LONG);
                snackbar.show();
            } else {
                newBathingSiteFragment.clearAllFields();

                // Put data in database
                myDb.insertData(name, description, address, longitude, latitude, grade, waterTemp, dateForTemp);

                InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);

                // Check if the soft keyboard is open and close it
                if(imm.isAcceptingText()) {
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }

                // Create a thread and close the activity
                // Let it sleep for 2000 ms (LENGTH_SHORT) so the user can see snackbar message
                Thread thread = new Thread(){
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                            NewBathingSiteActivity.this.finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };

                Snackbar snackbar = Snackbar.make(findViewById(R.id.save), "Bathing site saved...", Snackbar.LENGTH_SHORT);
                snackbar.show();
                thread.start();
            }
        }
    }

    private void setNumberOfBathingSites() {

        // Check if device is in landscape mode to prevent crash
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            // Get number of saved bathing sites in the database
            SQLDatabase myDb = new SQLDatabase(this);
            int numberOfBathingSites = myDb.numberOfBathingSites();

            // Create a BathingSitesView and set number of bathing sites
            BathingSitesView bsw = (BathingSitesView) findViewById(R.id.mainActivityBathingSitesView);
            bsw.setNumberOfBathingSites(numberOfBathingSites);
        }
    }

    private void settingsButtonPressed() {
        // Go to Settings activity
        Intent intent = new Intent(this, Settings.class);
        this.startActivity(intent);
    }

    private void weatherButtonPressed() {

        // Get base URL from settings or use second argument of prefs.getString if no URL could be found
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String baseUrl = prefs.getString("weather_url", "http://dt031g.programvaruteknik.nu/badplatser/weather.php");
        String primaryUrl = baseUrl; // Try to get weather from this URL first
        String secondaryUrl = baseUrl; // This one will be used if no weather could be found with primaryUrl

        String address = newBathingSiteFragment.getAddress().getText().toString();
        String longitude = newBathingSiteFragment.getLongitude().getText().toString();
        String latitude = newBathingSiteFragment.getLatitude().getText().toString();

        if (address.isEmpty() && latitude.isEmpty() && latitude.isEmpty()) {
            Toast.makeText(this, "Enter an address or longitude/latitude to see current weather", Toast.LENGTH_LONG).show();
            return;
        }

        // If both address and long/lat has been entered, two URLs will be passed to check for weather.
        // Long/lat will always be prioritized over address.
        if (!address.isEmpty() && !latitude.isEmpty() && !latitude.isEmpty()) {
            primaryUrl += "?location=" + longitude + "|" + latitude + "&language=SW";
            secondaryUrl += "?location=" + address + "&language=SW";
        } else if (longitude.isEmpty() && latitude.isEmpty()) {
            primaryUrl += "?location=" + address + "&language=SW";
            secondaryUrl = null;
        } else {
            primaryUrl += "?location=" + longitude + "|" + latitude + "&language=SW";
            secondaryUrl = null;
        }

        Weather weather = new Weather(this, primaryUrl, secondaryUrl);
        weather.downloadWeatherData();
    }
}