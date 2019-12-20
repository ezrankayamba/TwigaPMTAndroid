package tz.co.nezatech.apps.twigapmt;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import tz.co.nezatech.apps.twigapmt.util.Constants;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);

        loginButton.setOnClickListener(v -> {
            login(usernameEditText.getText().toString(), passwordEditText.getText().toString());
        });
    }

    private void login(String username, String password) {
        new LoginTaskTask(username, password, findViewById(R.id.loading)).execute();
    }

    private static class LoginTaskTask extends AsyncTask<Void, Void, String> {
        private String username;
        private String password;
        private ProgressBar progressBar;

        public LoginTaskTask(String username, String password, ProgressBar progressBar) {
            this.username = username;
            this.password = password;
            this.progressBar = progressBar;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... params) {
            HttpsURLConnection conn = null;
            String response = "";
            try {
                URL url = new URL(Constants.BASE_URL + "api/oauth2/token/");
                conn = (HttpsURLConnection ) url.openConnection();
                conn.addRequestProperty("client_id", Constants.OAUTH2_CLIENT_ID);
                conn.addRequestProperty("client_secret", Constants.OAUTH2_CLIENT_SECRET);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");

                String body = String.format("grant_type=password&username=%s&password=%s", username, password);
                Log.d(TAG, "Body: " + body);
                OutputStream out = new BufferedOutputStream(conn.getOutputStream());
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
                writer.write(body);
                writer.flush();

                int code = conn.getResponseCode();
                Log.d(TAG, "Response Code: "+code);
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    Log.i(TAG, line);
                    response += line;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressBar.setVisibility(View.GONE);
            Log.d(TAG, "Response: " + s);
        }
    }

}
