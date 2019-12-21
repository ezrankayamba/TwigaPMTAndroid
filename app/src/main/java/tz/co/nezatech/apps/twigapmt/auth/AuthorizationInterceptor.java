package tz.co.nezatech.apps.twigapmt.auth;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import tz.co.nezatech.apps.twigapmt.api.PMTService;
import tz.co.nezatech.apps.twigapmt.model.TokenResponse;
import tz.co.nezatech.apps.twigapmt.util.Constants;

import java.io.IOException;

public class AuthorizationInterceptor implements Interceptor {
    private PMTService apiService;
    private Session session;

    public AuthorizationInterceptor(PMTService apiService, Session session) {
        this.apiService = apiService;
        this.session = session;
    }

    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Response mainResponse = chain.proceed(chain.request());
        Request mainRequest = chain.request();

        if (session.isLoggedIn()) {
            if (mainResponse.code() == 401 || mainResponse.code() == 403) {
                retrofit2.Response<TokenResponse> loginResponse = apiService.getToken(session.getUsername(), session.getPassword(), "password", Constants.OAUTH2_CLIENT_ID, Constants.OAUTH2_CLIENT_SECRET).execute();
                if (loginResponse.isSuccessful()) {
                    TokenResponse authorization = loginResponse.body();
                    session.saveToken(authorization.getAccessToken());
                    Request.Builder builder = mainRequest.newBuilder().header("Authorization", session.getToken()).
                            method(mainRequest.method(), mainRequest.body());
                    mainResponse = chain.proceed(builder.build());
                }
            }
        }
        return mainResponse;
    }
}
