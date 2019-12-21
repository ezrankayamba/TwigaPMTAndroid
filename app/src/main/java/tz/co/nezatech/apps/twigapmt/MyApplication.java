package tz.co.nezatech.apps.twigapmt;


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import tz.co.nezatech.apps.twigapmt.api.PMTService;
import tz.co.nezatech.apps.twigapmt.auth.AuthorizationInterceptor;
import tz.co.nezatech.apps.twigapmt.auth.Session;
import tz.co.nezatech.apps.twigapmt.auth.TokenRenewInterceptor;
import tz.co.nezatech.apps.twigapmt.util.Constants;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class MyApplication extends Application {
    private Session session;
    private PMTService apiService;
    private AuthenticationListener authenticationListener;
    private SharedPreferences sharedPref;

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPref = getApplicationContext().getSharedPreferences(Constants.CHANNEL_ID, Context.MODE_PRIVATE);
    }

    public Session getSession() {
        if (session == null) {
            session = new Session() {
                @Override
                public boolean isLoggedIn() {
                    return !getToken().isEmpty();
                }

                @Override
                public void saveToken(String token) {
                    sharedPref.edit().putString(Constants.OAUTH2_TOKEN_KEY, token).commit();
                }

                @Override
                public String getToken() {
                    String token = sharedPref.getString(Constants.OAUTH2_TOKEN_KEY, "");
                    return token;
                }

                @Override
                public void saveUsername(String username) {
                    sharedPref.edit().putString(Constants.OAUTH2_USERNAME, username).commit();
                }

                @Override
                public String getUsername() {
                    return sharedPref.getString(Constants.OAUTH2_USERNAME, null);
                }

                @Override
                public void savePassword(String password) {
                    sharedPref.edit().putString(Constants.OAUTH2_PASSWORD, password).commit();
                }

                @Override
                public String getPassword() {
                    return sharedPref.getString(Constants.OAUTH2_PASSWORD, null);
                }

                @Override
                public void invalidate() {
                    // get called when user become logged out
                    // delete token and other user info
                    // (i.e: email, password)
                    // from the storage

                    // sending logged out event to it's listener
                    // i.e: Activity, Fragment, Service
                    SharedPreferences.Editor editor = sharedPref.edit();
                    Arrays.asList(Constants.OAUTH2_PASSWORD, Constants.OAUTH2_USERNAME, Constants.OAUTH2_TOKEN_KEY).forEach(key -> {
                        editor.remove(key.toString());
                    });
                    if (authenticationListener != null) {
                        authenticationListener.onUserLoggedOut();
                    }
                }
            };
        }

        return session;
    }

    public interface AuthenticationListener {
        void onUserLoggedOut();
    }

    public void setAuthenticationListener(AuthenticationListener listener) {
        this.authenticationListener = listener;
    }

    public PMTService getApiService() {
        if (apiService == null) {
            apiService = provideRetrofit(Constants.BASE_URL).create(PMTService.class);
        }
        return apiService;
    }

    private Retrofit provideRetrofit(String url) {
        return new Retrofit.Builder()
                .baseUrl(url)
                .client(provideOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create(new Gson()))
                .build();
    }

    private OkHttpClient provideOkHttpClient() {
        OkHttpClient.Builder okhttpClientBuilder = new OkHttpClient.Builder();
        okhttpClientBuilder.connectTimeout(30, TimeUnit.SECONDS);
        okhttpClientBuilder.readTimeout(30, TimeUnit.SECONDS);
        okhttpClientBuilder.writeTimeout(30, TimeUnit.SECONDS);

        okhttpClientBuilder.addInterceptor(new TokenRenewInterceptor(session));
        okhttpClientBuilder.addInterceptor(new AuthorizationInterceptor(getApiService(), getSession()));
        return okhttpClientBuilder.build();
    }
}
