package com.brylle.aus_cs_app_android_j.events;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.brylle.aus_cs_app_android_j.AppUtils;
import com.brylle.aus_cs_app_android_j.home.HomeActivity;
import com.brylle.aus_cs_app_android_j.profile.RegisteredEventsActivity;
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

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

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
                            if (!queryDocumentSnapshots.isEmpty()) {        // if there is a match
                                // perform Firestore functions that log in volunteer's attendance
                                userAttendedEvent(queryDocumentSnapshots);
                            } else {
                                Toast.makeText(getApplicationContext(),"Error, unrecognized QR code!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            }


                        }
                    });
        }

    }

    public void userAttendedEvent(QuerySnapshot queryDocumentSnapshots) {

        for (final DocumentSnapshot fetchedEvent : queryDocumentSnapshots) {
            final int eventHours = fetchedEvent.getLong(AppUtils.KEY_EVENT_HOURS).intValue();
            final String eventName = fetchedEvent.getString(AppUtils.KEY_EVENT_NAME);
            final int eventID = fetchedEvent.getLong((AppUtils.KEY_EVENT_ID)).intValue();
            fetchedEvent.getReference().update(AppUtils.KEY_PAST_VOLUNTEERS, FieldValue.arrayUnion(currentUser.getEmail()))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("QRScanActivity", "(1) User has successfully attended the event " + eventName + "!");
                            Toast.makeText(getApplicationContext(), "Successfully attended for " + eventName, Toast.LENGTH_LONG).show();

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
                                                    // 6. Remove event from user's registered_events list if exists, otherwise do nothing
                                                    unregisterEvent(eventID);
                                                }

                                                //7. Display event name in the QRActivity textview
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

    private void unregisterEvent(final int eventID) {
        // Unregister a user from an event when the "Unregister" button is clicked
        // (1) Removes a user from an event's registered_students field in Firebase
        // and (2) removes an event from a user's registered_events field in Firebase

        // (1) Remove user from event's registered_students array
        firestoreEventList.whereEqualTo(AppUtils.KEY_EVENT_ID, eventID)      // query: look for event document that matches event id of clicked event
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (final DocumentSnapshot fetchedEvent : queryDocumentSnapshots) {   // loop through every database hit, SHOULD ONLY BE ONE MATCH THO
                            // final DocumentSnapshot eventSnapshot = document;
                            // Remove student from event's registered_students array
                            removeStudentFromEventList(fetchedEvent);

                            // (2) Remove event object to user's registered_events
                            removeEventFromUserList(fetchedEvent);

                            Log.d("RegEventsFragment", "User has successfully unregistered for event " + eventID + "!");
                        }
                    }
                });

    }

    private void removeStudentFromEventList(final DocumentSnapshot fetchedEvent) {

        fetchedEvent.getReference().update(AppUtils.KEY_REGISTERED_STUDENTS, FieldValue.arrayRemove(currentUser.getEmail()))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("EventsFragment", "2: User has successfully unregistered from event " + fetchedEvent.getId() + "!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("EventsFragment", "2: Error unregistering user from event " + fetchedEvent.getId() + "!", e);
                    }
                });

    }

    private void removeEventFromUserList(final DocumentSnapshot fetchedEvent) {

        firebaseFirestore.collection("users").whereEqualTo(AppUtils.KEY_EMAIL, currentUser.getEmail())      // query: look for user document that matches email of current user
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot document : queryDocumentSnapshots) {      // loop through every database hit, SHOULD ONLY BE ONE MATCH THO
                            final HashMap<String,Object> eventEntry = new HashMap<>();
                            eventEntry.put(AppUtils.KEY_EVENT_ID, fetchedEvent.getLong(AppUtils.KEY_EVENT_ID));
                            eventEntry.put(AppUtils.KEY_EVENT_NAME, fetchedEvent.getString(AppUtils.KEY_EVENT_NAME));
                            eventEntry.put(AppUtils.KEY_EVENT_COORDS, fetchedEvent.getGeoPoint(AppUtils.KEY_EVENT_COORDS));
                            eventEntry.put(AppUtils.KEY_EVENT_LOCATION, fetchedEvent.getString(AppUtils.KEY_EVENT_LOCATION));
                            eventEntry.put(AppUtils.KEY_START_DATE, fetchedEvent.getString(AppUtils.KEY_START_DATE));
                            eventEntry.put(AppUtils.KEY_END_DATE, fetchedEvent.getString(AppUtils.KEY_END_DATE));
                            eventEntry.put(AppUtils.KEY_START_TIME, fetchedEvent.getString(AppUtils.KEY_START_TIME));
                            eventEntry.put(AppUtils.KEY_END_TIME, fetchedEvent.getString(AppUtils.KEY_END_TIME));
                            // add hashmap to registered_events of user
                            document.getReference().update(AppUtils.KEY_REGISTERED_EVENTS, FieldValue.arrayRemove(eventEntry))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d("EventsFragment", "1: User has unsuccessfully registered for event " + fetchedEvent.getId() + "!");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w("EventsFragment", "1: Error unregistering user to event " + fetchedEvent.getId() + "!", e);
                                        }
                                    });

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("EventsFragment", "1: Error fetching user through query! ", e);
                    }
                });

    }

}
