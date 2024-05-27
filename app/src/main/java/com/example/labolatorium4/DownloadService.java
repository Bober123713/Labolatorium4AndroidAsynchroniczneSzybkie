package com.example.labolatorium4;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadService extends Service {

    private static final String TAG = "DownloadService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String urlString = intent.getStringExtra("url");
        Log.d(TAG, "Starting download for: " + urlString);
        new DownloadFileTask().execute(urlString);
        return START_NOT_STICKY;
    }

    private class DownloadFileTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... urls) {
            String urlString = urls[0];
            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                int fileLength = connection.getContentLength();
                if (fileLength == -1) {
                    return "File not found.";
                }

                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "downloadedfile");
                InputStream input = connection.getInputStream();
                FileOutputStream output = new FileOutputStream(file);

                byte[] data = new byte[4096];
                int total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }

                output.close();
                input.close();
                return "Downloaded to: " + file.getAbsolutePath();
            } catch (Exception e) {
                Log.e(TAG, "Download error: ", e);
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int progress = values[0];
            Log.d(TAG, "Progress: " + progress + "%");

            NotificationCompat.Builder builder = new NotificationCompat.Builder(DownloadService.this, "DownloadChannel")
                    .setContentTitle("Downloading File")
                    .setContentText("Download in progress")
                    .setSmallIcon(R.drawable.ic_download)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setProgress(100, progress, false);

            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.notify(1, builder.build());
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, result);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(DownloadService.this, "DownloadChannel")
                    .setContentTitle("Download Complete")
                    .setContentText(result)
                    .setSmallIcon(R.drawable.ic_download)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(result.split(": ")[1]), "application/octet-stream");
            PendingIntent pendingIntent = PendingIntent.getActivity(DownloadService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);

            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.notify(1, builder.build());
        }
    }
}
