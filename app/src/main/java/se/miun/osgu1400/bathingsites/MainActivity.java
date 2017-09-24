package se.miun.osgu1400.bathingsites;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        setNumberOfBathingSites();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NewBathingSiteActivity.class);
                startActivity(intent);
            }
        });
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.download) {
            downloadButtonPressed();
        } else if (id == R.id.settings) {
            settingsButtonPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void downloadButtonPressed() {
        // Go to DownloadBathingSites activity
        Intent intent = new Intent(this, DownloadBathingSites.class);
        this.startActivity(intent);
    }

    private void settingsButtonPressed() {
        // Go to Settings activity
        Intent intent = new Intent(this, Settings.class);
        this.startActivity(intent);
    }

    private void setNumberOfBathingSites() {
        // Get number of saved bathing sites in the database
        SQLDatabase myDb = new SQLDatabase(this);
        int numberOfBathingSites = myDb.numberOfBathingSites();

        // Create a BathingSitesView and set number of bathing sites
        BathingSitesView bsw = (BathingSitesView) findViewById(R.id.mainActivityBathingSitesView);
        bsw.setNumberOfBathingSites(numberOfBathingSites);
    }
}
