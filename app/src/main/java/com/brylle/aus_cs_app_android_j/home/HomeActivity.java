package com.brylle.aus_cs_app_android_j.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.brylle.aus_cs_app_android_j.R;
import com.brylle.aus_cs_app_android_j.events.EventsFragment;
import com.brylle.aus_cs_app_android_j.events.QRScanActivity;
import com.brylle.aus_cs_app_android_j.profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends FragmentActivity {

    /* Variables */

    //lateinit var toolbar: ActionBar
    FragmentManager fragmentManager = getSupportFragmentManager();

    Fragment pfragment = new ProfileFragment();
    Fragment efragment = new EventsFragment();

    Fragment active = efragment;

    /* Initializer functions */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize navigation toolbar
        //toolbar = supportActionBar!!
        BottomNavigationView bottomNavigation = findViewById(R.id.navigationView);

        fragmentManager.beginTransaction().add(R.id.frameLayout,pfragment,"2").hide(pfragment).commit();
        fragmentManager.beginTransaction().add(R.id.frameLayout,efragment,"1").commit();

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
                    startActivity(new Intent(getApplicationContext(), QRScanActivity.class));
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
