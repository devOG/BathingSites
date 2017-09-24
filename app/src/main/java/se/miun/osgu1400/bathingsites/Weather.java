package se.miun.osgu1400.bathingsites;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Weather {

    private ProgressDialog progressDialog;
    private DownloadTask downloadTask;
    private Context context;
    private String primaryUrl;
    private String secondaryUrl;

    public Weather(Context ctx, String pUrl, String sUrl) {

        context = ctx;
        primaryUrl = pUrl;
        secondaryUrl = sUrl;

        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Getting current weather...");
        progressDialog.setIndeterminate(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(true);

        downloadTask = new DownloadTask();

        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                downloadTask.cancel(true);
            }
        });
    }

    private void showWeatherDialog(Map<String, String> result) {

        // If ANY weather data is found, this value will be changed to true,
        // and a dialog will pop up and show the data to the user
        boolean weatherDataFound = false;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View view = inflater.inflate(R.layout.weather_dialog, null);

        TextView address = (TextView) view.findViewById(R.id.weatherAddress);
        TextView temp = (TextView) view.findViewById(R.id.weatherTemp);
        TextView condition = (TextView) view.findViewById(R.id.weatherCondition);
        TextView humidity = (TextView) view.findViewById(R.id.weatherHumidity);
        TextView windSpeed = (TextView) view.findViewById(R.id.weatherWindSpeed);
        WebView image = (WebView) view.findViewById(R.id.weatherImage);
        Button closeButton = (Button) view.findViewById(R.id.weatherOkButton);

        // A control is made to check if the value is "null".
        // This way we can show data if it exists, or do whatever we
        // want if it doesn't. For example, if we didn't get a value,
        // we can inform the user that it's missing or simply show nothing.

        // Address
        if (result.get("address").equals("null") || result.get("address").isEmpty()) {
            address.setText("");
        } else {
            address.setText(result.get("address"));
            weatherDataFound = true;
        }

        // Condition
        if (result.get("condition").equals("null") || result.get("address").isEmpty()) {
            condition.setText("");
        } else {
            condition.setText(result.get("condition"));
            weatherDataFound = true;
        }

        // Temp
        if (result.get("temp_c").equals("null") || result.get("address").isEmpty()) {
            temp.setText("");
        } else {
            String tempStr = result.get("temp_c") + "°C";
            temp.setText(tempStr);
            weatherDataFound = true;
        }

        // Humidity
        if (result.get("humidity").equals("null") || result.get("address").isEmpty()) {
            humidity.setText("Humidity: value is missing");
        } else {
            String humidityStr = "Humidity: " + result.get("humidity");
            humidity.setText(humidityStr);
            weatherDataFound = true;
        }

        // Wind speed
        if (result.get("wind_kph").equals("null") || result.get("address").isEmpty()) {
            windSpeed.setText("Wind: value is missing");
        } else {
            double windSpeedInMps = Double.valueOf(result.get("wind_kph")) * 1000 / 3600;
            String windSpeedInMpsStr = "Wind: " + String.valueOf(String.format("%.1f", windSpeedInMps)) + " m/s";
            windSpeed.setText(windSpeedInMpsStr);
            weatherDataFound = true;
        }

        // Image
        if (result.get("image").equals("null") || result.get("address").isEmpty()) {
            image = null;
        } else {
            image.loadUrl(result.get("image"));
            weatherDataFound = true;
        }

        // If no weather data was found, check secondary URL if it's not null
        if (!weatherDataFound) {
            if (secondaryUrl != null) {
                primaryUrl = secondaryUrl;
                secondaryUrl = null;
                downloadWeatherData();
                return;
            } else {
                Toast.makeText(context, "Weather data is not available for this address. Please enter another.", Toast.LENGTH_LONG).show();
                return;
            }
        }

        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.show();

        closeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    // Downloading weather data
    public void downloadWeatherData() {
        downloadTask.execute(primaryUrl);

        // Create DT obj again in order to execute multiple times
        downloadTask = new DownloadTask();
    }

    private class DownloadTask extends AsyncTask<String, Integer, Map<String, String>> {

        private static final String REQUEST_METHOD = "GET";
        private static final int READ_TIMEOUT = 15000;
        private static final int CONNECTION_TIMEOUT = 15000;

        @Override
        protected Map<String, String> doInBackground(String... sUrl) {

            String stringUrl = sUrl[0];
            Map<String, String> result = new HashMap<String, String>();
            String inputLine = "";

            try {
                // Create a URL object holding the url
                URL myUrl = new URL(stringUrl);

                // Create a connection
                HttpURLConnection connection = (HttpURLConnection) myUrl.openConnection();

                // Set methods and timeouts
                connection.setRequestMethod(REQUEST_METHOD);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setConnectTimeout(CONNECTION_TIMEOUT);

                // Connect to our url
                connection.connect();

                // Create a new InputStreamReader
                InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());

                // Create a new buffered reader
                BufferedReader reader = new BufferedReader(streamReader);

                // Check if the line we are reading is not null
                while((inputLine = reader.readLine()) != null) {

                    // Clean up inputLine and add to result
                    String tmpStr = inputLine.replace("<br>", "");
                    String key = tmpStr.substring(0, tmpStr.indexOf(":"));
                    String value = tmpStr.substring(tmpStr.indexOf(":") + 1);
                    result.put(key, value);
                }

                // Close our InputStream and Buffered reader
                reader.close();
                streamReader.close();

            } catch(IOException e) {
                e.printStackTrace();
                result = null;
            }

            return result;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(Map<String, String> result) {

            progressDialog.dismiss();

            if (result != null) {
                showWeatherDialog(result);
            } else {
                // Result is equal to NULL because an exception was thrown when trying to connect
                // to the server. This could happen for multiple reasons. Maybe the user entered
                // a inaccurate URL in the settings? More error checking should probably be done
                // in order to notify the user why this happened.
                Toast.makeText(context, "Something went wrong...", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
