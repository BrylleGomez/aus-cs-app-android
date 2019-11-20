package com.brylle.aus_cs_app_android_j;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

public class CSApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("CSApp", "AUS CS App started!");

        // start service
        Intent service = new Intent(this, CSService.class);
        startService(service);
    }

}
