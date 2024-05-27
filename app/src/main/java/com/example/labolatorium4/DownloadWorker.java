package com.example.labolatorium4;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadWorker extends Worker {

    private static final String TAG = "DownloadWorker";

    public DownloadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String urlString = getInputData().getString("url");

        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            int fileLength = connection.getContentLength();
            if (fileLength == -1) {
                return Result.failure();
            }

            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "downloadedfile");
            InputStream input = connection.getInputStream();
            FileOutputStream output = new FileOutputStream(file);

            byte[] data = new byte[4096];
            int total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                updateProgress(total, fileLength);
                output.write(data, 0, count);
            }

            output.close();
            input.close();

            showCompletionNotification(file.getAbsolutePath());
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Download error: ", e);
            return Result.failure();
        }
    }

    private void updateProgress(int progress, int fileLength) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "DownloadChannel")
                .setContentTitle("Downloading File")
                .setContentText("Download in progress")
                .setSmallIcon(R.drawable.ic_download)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setProgress(fileLength, progress, false);

        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, builder.build());

        Intent intent = new Intent("com.example.labolatorium4.PROGRESS_UPDATE");
        PostepInfo postepInfo = new PostepInfo(progress, fileLength, "Pobieranie trwa");
        intent.putExtra("progress_info", postepInfo);
        getApplicationContext().sendBroadcast(intent);
    }

    private void showCompletionNotification(String filePath) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "DownloadChannel")
                .setContentTitle("Download Complete")
                .setContentText("File downloaded to: " + filePath)
                .setSmallIcon(R.drawable.ic_download)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(filePath), "application/octet-stream");
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, builder.build());

        Intent progressIntent = new Intent("com.example.labolatorium4.PROGRESS_UPDATE");
        PostepInfo postepInfo = new PostepInfo(0, 0, "Pobieranie zakończone");
        progressIntent.putExtra("progress_info", postepInfo);
        getApplicationContext().sendBroadcast(progressIntent);
    }
}
