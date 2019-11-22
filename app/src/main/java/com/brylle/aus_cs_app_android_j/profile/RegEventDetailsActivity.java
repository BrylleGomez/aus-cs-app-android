package com.brylle.aus_cs_app_android_j.profile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.brylle.aus_cs_app_android_j.R;
import com.brylle.aus_cs_app_android_j.events.Event;

public class RegEventDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_event_details);

        // Retrieve parcelable event object
        Intent i = getIntent();
        Event event = i.getParcelableExtra("regEventParcel");
        if (event != null) {
            event.print();  // Debug

        } else {
            Log.d("Debug", "Event object is null!");
        }

    }
}
