package com.example.persistentserviceapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final long TRIPLE_PRESS_INTERVAL = 2000; // 2 seconds interval for triple press
    private int volumeDownPressCount = 0;
    private long lastVolumeDownPressTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Start the foreground service
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        startService(serviceIntent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the volume down button was pressed
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastVolumeDownPressTime <= TRIPLE_PRESS_INTERVAL) {
                volumeDownPressCount++;
            } else {
                volumeDownPressCount = 1; // Reset if interval exceeded
            }
            lastVolumeDownPressTime = currentTime;

            // Check if volume down was pressed thrice
            if (volumeDownPressCount == 3) {
                Log.d(TAG, "Volume down button pressed thrice");
                volumeDownPressCount = 0; // Reset count
            }

            return true; // Indicate that the event has been handled
        }
        return super.onKeyDown(keyCode, event); // Pass other key events to the default handler
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Optionally stop the service when the app is closed
        // stopService(new Intent(this, ForegroundService.class));
    }
}
