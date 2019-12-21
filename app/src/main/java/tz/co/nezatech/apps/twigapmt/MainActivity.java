package tz.co.nezatech.apps.twigapmt;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.snackbar.Snackbar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import tz.co.nezatech.apps.twigapmt.adapter.IdNameSpinnerAdapter;
import tz.co.nezatech.apps.twigapmt.api.HttpLogInterceptor;
import tz.co.nezatech.apps.twigapmt.api.PMTService;
import tz.co.nezatech.apps.twigapmt.model.IdName;
import tz.co.nezatech.apps.twigapmt.model.Project;
import tz.co.nezatech.apps.twigapmt.receiver.GeofenceReceiver;
import tz.co.nezatech.apps.twigapmt.util.Constants;
import tz.co.nezatech.apps.twigapmt.util.PermissionUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    private List<Project> projects;
    private PendingIntent geofencePendingIntent;
    private static Retrofit retrofit = null;
    private String accessToken;
    private String tokenType;
    private SharedPreferences sharedPrefs;
    private Spinner regionSpinner;
    private IdNameSpinnerAdapter regionAdapter;
    private IdName region;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        createNotificationChannel();
    }

    private static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void init() {
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION
        };
        if (!hasPermissions(this, permissions)) {
            ActivityCompat.requestPermissions(this, permissions, 100);
            return;
        }
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .client(HttpLogInterceptor.getClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        PMTService pmtService = retrofit.create(PMTService.class);

        regionSpinner = findViewById(R.id.regionSelect);
        pmtService.getRegions(String.format("%s %s", tokenType, accessToken)).enqueue(new Callback<List<IdName>>() {
            @Override
            public void onResponse(Call<List<IdName>> call, Response<List<IdName>> response) {
                List<IdName> list = response.body();
                if (list == null) {
                    Snackbar.make(regionSpinner, "Fetching regions failed", Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(regionSpinner, "Successfully fetched regions", Snackbar.LENGTH_LONG).show();
                    regionAdapter = new IdNameSpinnerAdapter(MainActivity.this, R.layout.id_name_spinner_item, list.toArray(new IdName[0]));
                    regionSpinner.setAdapter(regionAdapter);
                }
            }

            @Override
            public void onFailure(Call<List<IdName>> call, Throwable t) {

            }
        });
        regionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                region = regionAdapter.getItem(position);
                Toast.makeText(MainActivity.this, "Region: " + region.getName(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapter) {
            }
        });

        Button btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(v -> {
            if (region == null) {
                Toast.makeText(MainActivity.this, "Please select region!", Toast.LENGTH_LONG);
            } else {
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
        });
    }

    void loadGeofences() {
        PendingIntent geofencePendingIntent = getGeofencePendingIntent();
        GeofencingClient geofencingClient = LocationServices.getGeofencingClient(this);
        geofencingClient.removeGeofences(geofencePendingIntent);
        if (projects == null || projects.isEmpty()) {
            Snackbar.make(regionSpinner, String.format("No any project in %s", region.getName()), Snackbar.LENGTH_LONG).show();
            return;
        }

        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        List<Geofence> geofences = projects.stream().filter(project -> project.getLat() == 0).map(prj ->
                new Geofence.Builder()
                        .setRequestId(prj.getId() + "")
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setCircularRegion(prj.getLat(), prj.getLng(), Constants.GEOFENCE_RADIUS)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                        .build()).collect(Collectors.toList());

        builder.addGeofences(geofences);
        GeofencingRequest geofencingRequest = builder.build();


        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
                .addOnSuccessListener(this, aVoid -> {
                    Log.d(TAG, "Successfully added!");
                    Snackbar.make(regionSpinner,
                            String.format("Successfully created geofences for %d project(s) in %s", projects.size(), region.getName()),
                            Snackbar.LENGTH_LONG).show();
                })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "Failed to add!" + e.getMessage());
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d(TAG, "Permissions: " + Arrays.asList(permissions));
        Log.d(TAG, "Grants: " + Arrays.asList(grantResults));
        switch (requestCode) {
            case 100: {
                if (PermissionUtil.verifyPermissions(grantResults)) {
                    Log.d(TAG, "All permissions granted");
                    init();
                } else {
                    Log.e(TAG, "Some permissions not granted");
                }
            }
            break;
            default:
                Log.e(TAG, "Unknown result");
                break;
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(Constants.CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
