package tz.co.nezatech.apps.twigapmt.api;

import retrofit2.Call;
import retrofit2.http.GET;
import tz.co.nezatech.apps.twigapmt.model.Project;

import java.util.List;

public interface PMTService {
    @GET("projects/json")
    Call<List<Project>> listProjects();
}
