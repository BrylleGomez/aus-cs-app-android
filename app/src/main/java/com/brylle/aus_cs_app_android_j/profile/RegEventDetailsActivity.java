package com.brylle.aus_cs_app_android_j.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import com.brylle.aus_cs_app_android_j.AppUtils;
import com.brylle.aus_cs_app_android_j.R;
import com.brylle.aus_cs_app_android_j.events.Event;
import com.brylle.aus_cs_app_android_j.events.EventLocationActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.TimerTask;

public class RegEventDetailsActivity extends AppCompatActivity {

    /* Variables */

    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private CollectionReference firestoreEventList = firebaseFirestore.collection("events");    // retrieve reference to "events" collection
    private TextView nameText;
    private TextView dateText;
    private TextView timeText;
    private TextView locationText;
    private ImageView locationPin;
    private Button registerButton;
    private int eventID;
    private String eventName;
    private String eventDates;
    private String eventTimings;
    private String eventLocation;
    private double eventLatitude;
    private double eventLongitude;

    /* Activity Life Cycle Functions */
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_event_details);

        // Retrieve event details from intent
        Bundle extras = getIntent().getExtras();
        eventID = extras.getInt("eventID");
        eventName = extras.getString("eventName");
        eventDates = extras.getString("eventDates");
        eventTimings = extras.getString("eventTimings");
        eventLocation = extras.getString("eventLocation");
        eventLatitude = extras.getDouble("eventLatitude");
        eventLongitude = extras.getDouble("eventLongitude");

        // Retrieve references to UI elements
        nameText = findViewById(R.id.regevent_details_header);
        dateText = findViewById(R.id.regevent_details_textview_date);
        timeText = findViewById(R.id.regevent_details_textview_time);
        locationText = findViewById(R.id.regevent_details_textview_location);
        locationPin = findViewById(R.id.regevent_details_img);
        registerButton = findViewById(R.id.regevent_details_btn);

        // Update UI elements with event details
        nameText.setText(eventName);
        dateText.setText(eventDates);
        timeText.setText(eventTimings);
        locationText.setText(eventLocation);

        // Set onClick listeners to location pin and button
        locationPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), EventLocationActivity.class);
                // Retrieve latitude and longitude from event geopoint
                intent.putExtra("eventLatitude", eventLatitude); // attach location coordinates to intent
                intent.putExtra("eventLongitude", eventLongitude); // attach location coordinates to intent
                intent.putExtra("eventLocation", eventLocation);
                startActivity(intent);
            }
        });
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unregisterEvent(eventID);

            }
        });

    }

    /* Helper Functions */

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
                            Toast.makeText(getApplicationContext(), "Successfully cancelled registration for " + eventName, Toast.LENGTH_LONG).show();

                            new Timer().schedule(
                                    new TimerTask(){

                                        @Override
                                        public void run(){
                                            startActivity(new Intent(getApplicationContext(), RegisteredEventsActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                        }
                                    }, 1000);
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
