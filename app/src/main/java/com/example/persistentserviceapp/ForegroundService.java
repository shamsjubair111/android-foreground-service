package com.example.persistentserviceapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.Manifest;
import androidx.core.app.ActivityCompat;
import android.content.pm.PackageManager;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class ForegroundService extends Service {

    private static final String TAG = "ForegroundService";
    private static final String CHANNEL_ID = "ForegroundServiceChannel";
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private static final long LOCATION_UPDATE_INTERVAL = 10000;
    private static final long LOCATION_FASTEST_INTERVAL = 10000;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service Created");

        // Initialize the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Create a location callback to receive location updates
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    // Get the last location from the result
                    android.location.Location location = locationResult.getLastLocation();
                    if (location != null) {
                        // Log the location coordinates for debugging
                        Log.d(TAG, "Location: " + location.getLatitude() + ", " + location.getLongitude());
                    } else {
                        Log.d(TAG, "Location is null");
                    }
                }
            }
        };

        // Start receiving location updates
        startLocationUpdates();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();

        // Create a notification for the foreground service
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText("Location updates are active")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .build();

        // Start the service in the foreground
        startForeground(1, notification);

        // Keep logging service status
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000); // Log every 5 seconds
                    Log.d(TAG, "Service is active in the foreground");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }).start();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop location updates when the service is destroyed
        stopLocationUpdates();
        Log.d(TAG, "Service Destroyed");
    }

    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel);
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(LOCATION_UPDATE_INTERVAL); // Update interval
        locationRequest.setFastestInterval(LOCATION_FASTEST_INTERVAL); // Fastest update interval
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // Use GPS for accuracy

        // Check if location permissions are granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission not granted for location updates");
            return;
        }

        // Start receiving location updates
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        Log.d(TAG, "Location updates started");
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
        Log.d(TAG, "Location updates stopped");
    }
}
