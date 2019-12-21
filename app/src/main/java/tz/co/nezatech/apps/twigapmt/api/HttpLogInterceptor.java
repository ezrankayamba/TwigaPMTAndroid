package tz.co.nezatech.apps.twigapmt.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class HttpLogInterceptor {
    private static OkHttpClient httpClient;

    public static OkHttpClient getClient() {
        if (httpClient == null) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.level(HttpLoggingInterceptor.Level.BASIC);
            httpClient = new OkHttpClient.Builder().addInterceptor(interceptor).build();
        }
        return httpClient;
    }
}
