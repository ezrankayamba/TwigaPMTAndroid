package tz.co.nezatech.apps.twigapmt;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.material.snackbar.Snackbar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import tz.co.nezatech.apps.twigapmt.api.HttpLogInterceptor;
import tz.co.nezatech.apps.twigapmt.api.PMTService;
import tz.co.nezatech.apps.twigapmt.model.IdName;
import tz.co.nezatech.apps.twigapmt.model.Project;
import tz.co.nezatech.apps.twigapmt.receiver.GeofenceReceiver;
import tz.co.nezatech.apps.twigapmt.util.Constants;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RegionGeofenceActivity extends AppCompatActivity implements OnMapReadyCallback {
    static final String TAG = RegionGeofenceActivity.class.getName();
    private static Retrofit retrofit = null;
    private String accessToken;
    private String tokenType;
    private SharedPreferences sharedPrefs;
    private GoogleMap mMap;
    private IdName region;
    private List<Project> projects;
    private PendingIntent geofencePendingIntent;
    private SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_region_geofence);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        region = (IdName) getIntent().getSerializableExtra(Constants.EXTRAS_REGION);
        if (region != null) {
            getSupportActionBar().setTitle(region.getName());
        }
        getSupportActionBar().setSubtitle("Region projects");

        sharedPrefs = getApplicationContext().getSharedPreferences(Constants.CHANNEL_ID, Context.MODE_PRIVATE);
        accessToken = sharedPrefs.getString(Constants.OAUTH2_TOKEN_KEY, null);
        tokenType = sharedPrefs.getString(Constants.OAUTH2_TOKEN_TYPE_KEY, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accessToken == null) {
            Intent intent = new Intent(getBaseContext(), LoginActivity.class);
            startActivity(intent);
            return;
        }
        init();
    }

    PMTService pmtService;

    private void init() {
        if (!MainActivity.hasPermissions(this)) {
            ActivityCompat.requestPermissions(this, Constants.PERMISSIONS, 100);
            return;
        }
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .client(HttpLogInterceptor.getClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        pmtService = retrofit.create(PMTService.class);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        Call<List<Project>> call = pmtService.listProjects(String.format("%s %s", tokenType, accessToken), region.getId());
        call.enqueue(new Callback<List<Project>>() {
            @Override
            public void onResponse(Call<List<Project>> call, Response<List<Project>> response) {
                projects = response.body();
                Log.d(TAG, "Number of projects received: " + projects.size());
                loadGeofences();
            }

            @Override
            public void onFailure(Call<List<Project>> call, Throwable throwable) {
                Log.e(TAG, "Failed: " + throwable.getMessage());
                throwable.printStackTrace();
            }
        });
    }

    private PendingIntent getGeofencePendingIntent() {
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceReceiver.class);
        geofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }

    void loadGeofences() {
        PendingIntent geofencePendingIntent = getGeofencePendingIntent();
        GeofencingClient geofencingClient = LocationServices.getGeofencingClient(this);
        geofencingClient.removeGeofences(geofencePendingIntent);

        List<Project> withLoc = projects.stream().filter(project -> project.getLat() != 0)
                .collect(Collectors.toList());
        if (withLoc.isEmpty()) {
            Snackbar.make(mapFragment.getView(), String.format("No any project in %s", region.getName()), Snackbar.LENGTH_LONG).show();
            return;
        }

        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        List<Geofence> geofences = withLoc.stream().map(prj ->
                new Geofence.Builder()
                        .setRequestId(prj.getId() + "")
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setCircularRegion(prj.getLat(), prj.getLng(), Constants.GEOFENCE_RADIUS)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                        .build()).collect(Collectors.toList());
        Stream<MarkerOptions> markers = withLoc.stream().map(project ->
                new MarkerOptions()
                        .position(new LatLng(project.getLat(), project.getLng()))
                        .title(project.getName())
        );
        List<CircleOptions> circles = withLoc.stream().map(project ->
                new CircleOptions()
                        .center(new LatLng(project.getLat(), project.getLng()))
                        .radius(Constants.GEOFENCE_RADIUS)
                        .fillColor(0x40ff0000)
                        .strokeColor(Color.TRANSPARENT)
                        .strokeWidth(2)).collect(Collectors.toList());
        LatLngBounds.Builder b = new LatLngBounds.Builder();
        for (MarkerOptions m : markers.collect(Collectors.toList())) {
            b.include(m.getPosition());
            mMap.addMarker(m);
            mMap.addCircle(new CircleOptions()
                    .center(m.getPosition())
                    .radius(Constants.GEOFENCE_RADIUS)
                    .strokeColor(Color.BLUE)
                    .strokeWidth(2));
        }
        LatLngBounds bounds = b.build();
        LatLng center = bounds.getCenter();
        if (withLoc.size() > 1) {
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 100);
            mMap.animateCamera(cu);
        } else {
            CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(center, 13);
            mMap.animateCamera(cu);
        }


        builder.addGeofences(geofences);
        GeofencingRequest geofencingRequest = builder.build();

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
                .addOnSuccessListener(this, aVoid -> {
                    Log.d(TAG, "Successfully added!");
                    Snackbar.make(mapFragment.getView(),
                            String.format("Successfully created geofences for %d/%d project(s) in %s", withLoc.size(), projects.size(), region.getName()),
                            Snackbar.LENGTH_LONG).show();
                })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "Failed to add!" + e.getMessage());
                });
    }
}
