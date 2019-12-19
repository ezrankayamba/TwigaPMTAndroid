package tz.co.nezatech.apps.twigapmt.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import tz.co.nezatech.apps.twigapmt.Constants;
import tz.co.nezatech.apps.twigapmt.R;

import java.util.Optional;

public class GeofenceReceiver extends BroadcastReceiver {
    private static final String TAG = GeofenceReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive triggered");
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = "Error: " + geofencingEvent.getErrorCode();
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
        } else {
            geofencingEvent.getTriggeringGeofences().forEach(geofence -> {
                Log.d(TAG, "Project: " + geofence.getRequestId());
                Toast.makeText(context, "Project: " + geofence.getRequestId(), Toast.LENGTH_LONG).show();
            });
            Optional<Geofence> first = geofencingEvent.getTriggeringGeofences().stream().findFirst();
            if (first.isPresent()) {
                Geofence geofence = first.get();
                String requestId = geofence.getRequestId();
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Constants.CHANNEL_ID)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle("Project Around here!")
                        .setContentText("Project: " + requestId)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.notify(200, builder.build());

                Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(500);
                }
            }
        }
    }
}
