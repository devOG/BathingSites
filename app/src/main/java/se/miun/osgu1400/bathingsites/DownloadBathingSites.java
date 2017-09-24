package se.miun.osgu1400.bathingsites;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.webkit.DownloadListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadBathingSites extends AppCompatActivity {

    private String urlAddress;

    ProgressDialog mProgressDialog;
    DownloadTask downloadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_bathing_sites);

        // Get base URL from settings or use second argument of prefs.getString if no URL could be found
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        urlAddress = prefs.getString("download_url", "http://dt031g.programvaruteknik.nu/badplatser/koordinater-utf8/");

        // Create webview
        WebView webView = (WebView) findViewById(R.id.webview);
        webView.loadUrl(urlAddress);
        webView.setWebViewClient(new MyWebViewClient());
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                downloadFile(url);
            }
        });

        mProgressDialog = new ProgressDialog(DownloadBathingSites.this);
        mProgressDialog.setMessage("Downloading bathing sites...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);

        downloadTask = new DownloadTask(DownloadBathingSites.this);

        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                downloadTask.cancel(true);
            }
        });
    }

    private void downloadFile(String url) {
        downloadTask.execute(url);

        // Create DT obj again in order to execute multiple times
        downloadTask = new DownloadTask(DownloadBathingSites.this);
    }

    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // Expect HTTP 200 OK, so we don't mistakenly save error report instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return null;
                }

                // This will be useful to display download percentage
                // Might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // Create output file in cache
                File outputDir = context.getCacheDir();
                String fileName = "bathingsites.csv";
                File outputFile = new File(outputDir, fileName);

                // Download the file
                input = connection.getInputStream();
                output = new FileOutputStream(outputFile);

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // Allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // Publish the progress....
                    if (fileLength > 0) // Only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }

                addToDatabase(outputFile); // Add to database
                outputFile.delete(); // Delete downloaded file

            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Take CPU lock to prevent CPU from going off if the user presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // Length is known, set indeterminate to false
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            mProgressDialog.dismiss();
            if (result != null)
                Toast.makeText(context,"Couldn't download bathing sites", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(context,"Bathing sites downloaded", Toast.LENGTH_SHORT).show();
        }
    }

    private void addToDatabase(File outputFile) {

        BufferedReader br = null;
        String line = "";

        try {
            br = new BufferedReader(new FileReader(outputFile));

            // Loop through file and handle each bathing site
            while ((line = br.readLine()) != null) {

                // Clean up and separate string
                line = line.replaceFirst("\"", "");
                String coordinates = line.substring(0, line.indexOf("\""));
                String nameAndAddress = line.substring(line.indexOf("\"") + 1);
                nameAndAddress = nameAndAddress.replaceAll("\"", "");

                // Get coordinates
                String longitude = coordinates.substring(0, coordinates.indexOf(","));
                String latitude = coordinates.substring(coordinates.indexOf(",") + 1);
                latitude = latitude.replaceAll(",", "");

                // Get name (and address if it exists)
                String name = "";
                String address = "";
                if (nameAndAddress.contains(",")) {
                    name = nameAndAddress.substring(0, nameAndAddress.indexOf(","));
                    address = nameAndAddress.substring(nameAndAddress.indexOf(",") + 1);
                } else {
                    name = nameAndAddress;
                }

                SQLDatabase myDb = new SQLDatabase(this);

                // Check if bathing site already exists in the database
                if (!myDb.bathingSiteExists(longitude, latitude)) {
                    // Add bathing site to database
                    myDb.insertData(name, "", address, longitude, latitude, 0, "", "");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Web view client class
    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}
