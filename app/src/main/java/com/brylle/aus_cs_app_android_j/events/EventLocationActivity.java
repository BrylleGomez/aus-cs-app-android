package com.brylle.aus_cs_app_android_j.events;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;

import com.brylle.aus_cs_app_android_j.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class EventLocationActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private double latitude;
    private double longitude;
    private String locationName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_location);

        // Retrieve latitude, longitude, and location name from intent
        Intent intent = getIntent();
        latitude = intent.getDoubleExtra("eventLatitude", 0.0);
        longitude = intent.getDoubleExtra("eventLongitude", 0.0);
        locationName = intent.getStringExtra("eventLocation");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker to the event location and move the camera
        LatLng eventPin = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(eventPin).title(locationName));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(eventPin));
        mMap.animateCamera( CameraUpdateFactory.zoomTo( 16.0f ) );
    }
}
