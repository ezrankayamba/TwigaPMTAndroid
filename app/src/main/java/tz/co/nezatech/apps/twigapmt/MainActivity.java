package tz.co.nezatech.apps.twigapmt;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import tz.co.nezatech.apps.twigapmt.adapter.IdNameAdapter;
import tz.co.nezatech.apps.twigapmt.adapter.RecyclerTouchListner;
import tz.co.nezatech.apps.twigapmt.api.HttpLogInterceptor;
import tz.co.nezatech.apps.twigapmt.api.PMTService;
import tz.co.nezatech.apps.twigapmt.model.IdName;
import tz.co.nezatech.apps.twigapmt.util.Constants;
import tz.co.nezatech.apps.twigapmt.util.PermissionUtil;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    private static Retrofit retrofit = null;
    private String accessToken;
    private String tokenType;
    private SharedPreferences sharedPrefs;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

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

    public static boolean hasPermissions(Context context) {
        if (context != null && Constants.PERMISSIONS != null) {
            for (String permission : Constants.PERMISSIONS) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void init() {
        if (!hasPermissions(this)) {
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

        PMTService pmtService = retrofit.create(PMTService.class);
        recyclerView = findViewById(R.id.regionList);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));


        pmtService.getRegions(String.format("%s %s", tokenType, accessToken)).enqueue(new Callback<List<IdName>>() {
            @Override
            public void onResponse(Call<List<IdName>> call, Response<List<IdName>> response) {
                List<IdName> list = response.body();
                if (list == null) {
                    Snackbar.make(recyclerView, "Fetching regions failed", Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(recyclerView, "Successfully fetched regions", Snackbar.LENGTH_LONG).show();
                    mAdapter = new IdNameAdapter(list.toArray(new IdName[0]));
                    recyclerView.setAdapter(mAdapter);
                    recyclerView.addOnItemTouchListener(new RecyclerTouchListner(MainActivity.this, recyclerView, new RecyclerTouchListner.ClickListener() {
                        @Override
                        public void onClick(View view, int position) {
                            IdName region = list.get(position);
                            Snackbar.make(recyclerView, String.format("Region selected: %s", region.getName()), Snackbar.LENGTH_LONG).show();

                            Intent intent = new Intent(MainActivity.this, RegionGeofenceActivity.class);
                            intent.putExtra(Constants.EXTRAS_REGION, region);
                            startActivity(intent);
                        }

                        @Override
                        public void onLongClick(View view, int position) {

                        }
                    }));

                }
            }

            @Override
            public void onFailure(Call<List<IdName>> call, Throwable t) {

            }
        });
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
