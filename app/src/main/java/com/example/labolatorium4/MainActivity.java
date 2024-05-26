package com.example.labolatorium4;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText urlEditText;
    private TextView fileInfoTextView;
    private Button downloadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        urlEditText = findViewById(R.id.urlEditText);
        fileInfoTextView = findViewById(R.id.fileInfoTextView);
        downloadButton = findViewById(R.id.downloadButton);

        downloadButton.setOnClickListener(view -> {
            String urlString = urlEditText.getText().toString();
            new DownloadFileInfoTask().execute(urlString);
        });
    }

    private class DownloadFileInfoTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String urlString = urls[0];
            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("HEAD");
                connection.connect();

                int fileSize = connection.getContentLength();
                String fileType = connection.getContentType();

                return "File Size: " + fileSize + " bytes\nFile Type: " + fileType;
            } catch (IOException e) {
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            fileInfoTextView.setText(result);
        }
    }
}
