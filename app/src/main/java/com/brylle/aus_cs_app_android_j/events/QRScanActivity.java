package com.brylle.aus_cs_app_android_j.events;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.brylle.aus_cs_app_android_j.AppUtils;
import com.brylle.aus_cs_app_android_j.R;
import com.brylle.aus_cs_app_android_j.home.HomeActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class QRScanActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private CollectionReference firestoreEventList = firebaseFirestore.collection("events");    // retrieve reference to "events" collection
    private ZXingScannerView scannerView;
    private int REQUEST_CAMERA_PERMISSION = 201;
    private Toast AttSuccess;
    int time = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_qrscan);
        try {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                AttSuccess = Toast.makeText(this, "Attendance Successfully Taken!", Toast.LENGTH_SHORT);
                scannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
                setContentView(scannerView);
                //mScannerView!!.setResultHandler(this)
                //mScannerView!!.startCamera()
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Set the scanner view as the content view
    }

    @Override
    public void onResume() {
        super.onResume();
        if (scannerView != null) {
            scannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
            scannerView.startCamera();    // Start camera on resume
        }

    }

    public void onPause() {
        super.onPause();
        if (scannerView != null) {
            scannerView.stopCamera();         // Stop camera on pause
        }
    }

    @Override
    public void handleResult(final Result result) {
        AttSuccess.show();

        //1. When student scans QR, QR code is stored onto an int
        int scannedQR = 0;  // 0 is default (error) value
        try
        {
            scannedQR = Integer.parseInt(result.getText());
        }
        catch (NumberFormatException nfe)
        {
            Log.d("Debug", "Failed to convert QR Code to integer value!");
        }

        //2. Perform a query looking for event whose ID matches this QRCode
        if (currentUser != null) {
            firestoreEventList.whereEqualTo(AppUtils.KEY_EVENT_ID, scannedQR).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            //3. Fetch the event, retrieve hours for event and save in int, and add user email to past_volunteers array
                            for (final DocumentSnapshot fetchedEvent : queryDocumentSnapshots) {
                                final int eventHours = fetchedEvent.getLong(AppUtils.KEY_EVENT_HOURS).intValue();
                                final String eventName = fetchedEvent.getString(AppUtils.KEY_EVENT_NAME);
                                final int eventID = fetchedEvent.getLong((AppUtils.KEY_EVENT_ID)).intValue();
                                fetchedEvent.getReference().update(AppUtils.KEY_PAST_VOLUNTEERS, FieldValue.arrayUnion(currentUser.getEmail()))
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("QRScanActivity", "(1) User has successfully attended the event " + eventName + "!");
                                                Toast.makeText(getApplicationContext(), "Successfully registered for " + eventName, Toast.LENGTH_LONG).show();

                                                //4. Fetch user, add HRS to volunteer_hours
                                                if (currentUser != null) {      // Fetch user from database using query
                                                    firebaseFirestore.collection("users").whereEqualTo(AppUtils.KEY_EMAIL, currentUser.getEmail())      // query: look for user document in Firestore using email of current user
                                                            .get()
                                                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                @Override
                                                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                                    for (DocumentSnapshot document : queryDocumentSnapshots) {      // loop through every database hit, SHOULD ONLY BE ONE MATCH THO
                                                                        DocumentReference fetchedUser = document.getReference();
                                                                        // check whether user has already attended the event!!
                                                                        // 5. Add event ID to user's list of volunteered_events
                                                                        fetchedUser.update(AppUtils.KEY_VOLUNTEERED_EVENTS, FieldValue.arrayUnion(eventID));     // add current event to user's volunteered_events array
                                                                        int userCurrentHours = document.getLong(AppUtils.KEY_VOLUNTEER_HOURS).intValue();
                                                                        Log.d("Debug", "Old hours = " + userCurrentHours);
                                                                        userCurrentHours += eventHours;
                                                                        fetchedUser.update(AppUtils.KEY_VOLUNTEER_HOURS, userCurrentHours);
                                                                        Log.d("Debug", "Updated hours = " + userCurrentHours);

                                                                    }

                                                                    //6. Display event name in the QRActivity textview
                                                                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                                                    intent.putExtra("qrData", eventName);
                                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                    startActivity(intent);

                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.w("EventsFragment", "Error fetching user through query! ", e);
                                                                }
                                                            });
                                                }

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w("EventsFragment", "(1) Error registering user to event " + eventName + "!", e);
                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("QR Scan Activity", "Error fetching event through query! ", e);
                        }
                    });
        }

    }


}
