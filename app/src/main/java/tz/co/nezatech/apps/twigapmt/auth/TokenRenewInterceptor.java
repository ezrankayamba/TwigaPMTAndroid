package tz.co.nezatech.apps.twigapmt.auth;

import android.util.Log;
import okhttp3.Interceptor;
import okhttp3.Response;

import java.io.IOException;

public class TokenRenewInterceptor implements Interceptor {
    final static String TAG = TokenRenewInterceptor.class.getName();
    private Session session;

    public TokenRenewInterceptor(Session session) {
        this.session = session;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());

        String newToken = response.header("x-auth-token");

        if (newToken != null) {
            Log.d(TAG, "New Token: " + newToken);
            session.saveToken(newToken);
        }

        return response;
    }
}