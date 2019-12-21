package tz.co.nezatech.apps.twigapmt;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import tz.co.nezatech.apps.twigapmt.api.PMTService;
import tz.co.nezatech.apps.twigapmt.model.TokenResponse;
import tz.co.nezatech.apps.twigapmt.util.Constants;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getName();
    private Button loginButton;
    boolean usrValid = false;
    boolean pwdValid = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText usrText = findViewById(R.id.username);
        usrValid = !usrText.getText().toString().isEmpty();
        usrText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                usrValid = s.length() > 3;
                loginButton.setEnabled(usrValid && pwdValid);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        final EditText pwdText = findViewById(R.id.password);
        pwdValid = !pwdText.getText().toString().isEmpty();
        pwdText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                pwdValid = s.length() > 3;
                loginButton.setEnabled(usrValid && pwdValid);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        loginButton = findViewById(R.id.login);
        loginButton.setEnabled(usrValid && pwdValid);
        loginButton.setOnClickListener(v -> {
            login(usrText.getText().toString(), pwdText.getText().toString());
        });
    }

    private static Retrofit retrofit = null;

    private void login(String username, String password) {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        PMTService pmtService = retrofit.create(PMTService.class);
        Call<TokenResponse> call = pmtService.getToken(username, password, "password", Constants.OAUTH2_CLIENT_ID, Constants.OAUTH2_CLIENT_SECRET);
        ProgressBar progressBar = findViewById(R.id.loading);
        progressBar.setVisibility(View.VISIBLE);
        call.enqueue(new Callback<TokenResponse>() {
            @Override
            public void onResponse(Call<TokenResponse> call, Response<TokenResponse> response) {
                progressBar.setVisibility(View.GONE);
                TokenResponse body = response.body();
                if (body != null) {
                    Log.d(TAG, "Success login: " + body.getAccessToken());
                    Snackbar.make(loginButton, "Successfully logged in", Snackbar.LENGTH_LONG).show();

                    SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(Constants.CHANNEL_ID, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putString(Constants.OAUTH2_TOKEN_KEY, body.getAccessToken());
                    editor.putString(Constants.OAUTH2_REFRESH_TOKEN_KEY, body.getRefreshToken());
                    editor.putString(Constants.OAUTH2_TOKEN_TYPE_KEY, body.getTokenType());
                    editor.commit();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    Log.e(TAG, "Failed login: " + response.message());
                    Snackbar.make(loginButton, "Login failed. Try again with correct credentials", Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<TokenResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Failed login");
                Snackbar.make(loginButton, "Login failed. Try again with correct credentials", Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
