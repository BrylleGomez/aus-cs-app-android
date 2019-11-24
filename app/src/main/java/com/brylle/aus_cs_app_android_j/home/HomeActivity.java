package com.brylle.aus_cs_app_android_j.home;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.brylle.aus_cs_app_android_j.R;
import com.brylle.aus_cs_app_android_j.events.EventsFragment;
import com.brylle.aus_cs_app_android_j.events.QRFragment;
import com.brylle.aus_cs_app_android_j.intro.LoginActivity;
import com.brylle.aus_cs_app_android_j.profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    /* Variables */

    FragmentManager fragmentManager = getSupportFragmentManager();

    Fragment pfragment = new ProfileFragment();
    Fragment efragment = new EventsFragment();
    Fragment qfragment = new QRFragment();
    String data;

    Fragment active = efragment;

    /* Initializer functions */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize navigation toolbar
        BottomNavigationView bottomNavigation = findViewById(R.id.navigationView);

        // Get intent from QR activity
        Intent intent = getIntent();                                // get intent from QR activity (in the case it was started by QRScanActivity)
        data = intent.getStringExtra("qrData");      // will be null if this activity was NOT started by QRScanActivity
        Bundle bundle = new Bundle();                             // create Bundle to pass to QRFragment info from QRScanActivity (in the case this activity was started by QRScanActivity)
        if (data != null) {                                         // if non empty intent (coming from QR activity), pass data to QR fragment
            bundle.putString("qrSerial", data);
            qfragment.setArguments(bundle);
            fragmentManager.beginTransaction().add(R.id.frameLayout,qfragment,"3").commit();
            fragmentManager.beginTransaction().add(R.id.frameLayout,pfragment,"2").hide(pfragment).commit();
            fragmentManager.beginTransaction().add(R.id.frameLayout,efragment,"1").hide(efragment).commit();
            data = null;
            active = qfragment;
        } else {    // if empty intent then load home page normally
            fragmentManager.beginTransaction().add(R.id.frameLayout,qfragment,"3").hide(qfragment).commit();
            fragmentManager.beginTransaction().add(R.id.frameLayout,pfragment,"2").hide(pfragment).commit();
            fragmentManager.beginTransaction().add(R.id.frameLayout,efragment,"1").commit();
            active = efragment;
        }

        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_home, menu);
        Log.d("Menu", "Menu inflated from activity!");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_about:
                startActivity(new Intent(getApplicationContext(), AboutActivity.class));
                return true;
            case R.id.menu_signout:
                signoutUser();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    private void signoutUser() {

        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        Log.d("Debug", "Test1");
        builder.setTitle("Are you sure?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(HomeActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    }
                })
                .setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();

    }


}
