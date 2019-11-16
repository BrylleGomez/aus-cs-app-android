package com.brylle.aus_cs_app_android_j.intro;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.brylle.aus_cs_app_android_j.R;

public class SplashActivity extends Activity {

    int loadTime = 2000;        // time to display splash screen

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Handler for splash screen display duration
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do following after specified delay
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            }
        }, loadTime);
    }
}
