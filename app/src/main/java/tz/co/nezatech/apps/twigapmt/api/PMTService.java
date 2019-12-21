package tz.co.nezatech.apps.twigapmt.api;

import retrofit2.Call;
import retrofit2.http.*;
import tz.co.nezatech.apps.twigapmt.model.IdName;
import tz.co.nezatech.apps.twigapmt.model.Project;
import tz.co.nezatech.apps.twigapmt.model.TokenResponse;

import java.util.List;

public interface PMTService {
    @GET("api/projects/{regionId}/")
    Call<List<Project>> listProjects(@Header("Authorization") String token, @Path("regionId") int regionId);

    @GET("api/regions/")
    Call<List<IdName>> getRegions(@Header("Authorization") String token);

    @POST("api/oauth2/token/")
    @FormUrlEncoded
    Call<TokenResponse> getToken(@Field("username") String username, @Field("password") String password, @Field("grant_type") String grantType, @Field("client_id") String clientId, @Field("client_secret") String clientSecret);
}
