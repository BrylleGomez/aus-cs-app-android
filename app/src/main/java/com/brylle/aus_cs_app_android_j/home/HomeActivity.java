package com.brylle.aus_cs_app_android_j.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.brylle.aus_cs_app_android_j.R;
import com.brylle.aus_cs_app_android_j.events.EventsFragment;
import com.brylle.aus_cs_app_android_j.events.QRFragment;
import com.brylle.aus_cs_app_android_j.events.QRScanActivity;
import com.brylle.aus_cs_app_android_j.profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends FragmentActivity {

    /* Variables */

    FragmentManager fragmentManager = getSupportFragmentManager();

    Fragment pfragment = new ProfileFragment();
    Fragment efragment = new EventsFragment();
    Fragment qfragment = new QRFragment();

    Fragment active = efragment;

    /* Initializer functions */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize navigation toolbar
        BottomNavigationView bottomNavigation = findViewById(R.id.navigationView);

        // Get intent from QR activity
        Intent intent = getIntent();    // get intent from QR activity (in the case it was started by QRScanActivity)
        String data = intent.getStringExtra("qrData");      // will be null if this activity was NOT started by QRScanActivity
        Bundle bundle = new Bundle();                             // create Bundle to pass to QRFragment info from QRScanActivity (in the case this activity was started by QRScanActivity)
        if (data != null) {     // if non empty intent (coming from QR activity), pass data to QR fragment
            bundle.putString("qrSerial", data);
            //bundle.putBoolean("qrValid", true);                     // inform QRFragment that there is a QR code to be read
            qfragment.setArguments(bundle);
            fragmentManager.beginTransaction().add(R.id.frameLayout,qfragment,"3").commit();
            fragmentManager.beginTransaction().add(R.id.frameLayout,pfragment,"2").hide(pfragment).commit();
            fragmentManager.beginTransaction().add(R.id.frameLayout,efragment,"1").hide(efragment).commit();
        } else {    // if empty intent then load home page normally
            //bundle.putBoolean("qrValid", false);                    // inform QRFragment that there is NO QR code to be read
            //qfragment.setArguments(bundle);
            fragmentManager.beginTransaction().add(R.id.frameLayout,qfragment,"3").hide(qfragment).commit();
            fragmentManager.beginTransaction().add(R.id.frameLayout,pfragment,"2").hide(pfragment).commit();
            fragmentManager.beginTransaction().add(R.id.frameLayout,efragment,"1").commit();
        }

        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    }

    /* Action functions */

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            // Fragment selectedFragment = null;
            switch (menuItem.getItemId()) {
                case R.id.eventlistBtn:
                    fragmentManager.beginTransaction().hide(active).show(efragment).commit();
                    active = efragment;
                    return true;
                case R.id.QRBtn:
                    fragmentManager.beginTransaction().hide(active).show(qfragment).commit();
                    active = qfragment;
                    return true;
                case R.id.profileBtn:
                    fragmentManager.beginTransaction().hide(active).show(pfragment).commit();
                    active = pfragment;
                    return true;
            }
            return false;
        }
    };

}
