package com.brylle.aus_cs_app_android_j.events;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.brylle.aus_cs_app_android_j.AppUtils;
import com.brylle.aus_cs_app_android_j.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

public class EventDetailsActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_event_details);

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
        nameText = findViewById(R.id.event_details_header);
        dateText = findViewById(R.id.event_details_textview_date);
        timeText = findViewById(R.id.event_details_textview_time);
        locationText = findViewById(R.id.event_details_textview_location);
        locationPin = findViewById(R.id.event_details_img);
        registerButton = findViewById(R.id.event_details_btn);

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
                registerEvent(eventID);
            }
        });

    }

    /* Helper Functions */

    private void registerEvent(final int eventID) {
        // Register a user to an event when the "Register" button is clicked
        // (1) Adds a user to an event's registered_students field in Firebase
        // and (2) adds an event to a user's registered_events in Firebase

        // (1) Add user to event's registered_students
        if (currentUser != null) {
            firestoreEventList.whereEqualTo(AppUtils.KEY_EVENT_ID, eventID)      // query: look for the event document that matches event id of clicked event
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (DocumentSnapshot fetchedEvent : queryDocumentSnapshots) {   // loop through every database hit, SHOULD ONLY BE ONE MATCH THO
                                // final DocumentSnapshot eventSnapshot = fetchedEvent;
                                // Update event's registered_students field with (email of) new student
                                fetchedEvent.getReference().update(AppUtils.KEY_REGISTERED_STUDENTS, FieldValue.arrayUnion(currentUser.getEmail()))
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("EventsFragment", "(1) User has successfully registered for event " + eventID + "!");
                                                Toast.makeText(getApplicationContext(), "Successfully registered for " + eventName, Toast.LENGTH_LONG).show();
                                                EventDetailsActivity.this.finish();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w("EventsFragment", "(1) Error registering user to event " + eventID + "!", e);
                                            }
                                        });

                                // (2) Add event object to user's registered_events
                                addEventToUserList(fetchedEvent);

                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w("EventsFragment", "2: Error fetching user through query! ", e);
                }
            });
        }

    }

    private void addEventToUserList(final DocumentSnapshot eventSnapshot) {
        // add an event entry to a user's registered_events field in Firestore
        // so user can keep track of which events he/she has registered for

        if (currentUser != null) {      // Fetch user from database using query
            firebaseFirestore.collection("users").whereEqualTo(AppUtils.KEY_EMAIL, currentUser.getEmail())      // query: look for user document in Firestore using email of current user
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (DocumentSnapshot document : queryDocumentSnapshots) {      // loop through every database hit, SHOULD ONLY BE ONE MATCH THO
                                // create hashmap of event details
                                final HashMap<String,Object> eventEntry = new HashMap<>();
                                eventEntry.put(AppUtils.KEY_EVENT_ID, eventSnapshot.getLong(AppUtils.KEY_EVENT_ID));
                                eventEntry.put(AppUtils.KEY_EVENT_NAME, eventSnapshot.getString(AppUtils.KEY_EVENT_NAME));
                                eventEntry.put(AppUtils.KEY_EVENT_COORDS, eventSnapshot.getGeoPoint(AppUtils.KEY_EVENT_COORDS));
                                eventEntry.put(AppUtils.KEY_EVENT_LOCATION, eventSnapshot.getString(AppUtils.KEY_EVENT_LOCATION));
                                eventEntry.put(AppUtils.KEY_START_DATE, eventSnapshot.getString(AppUtils.KEY_START_DATE));
                                eventEntry.put(AppUtils.KEY_END_DATE, eventSnapshot.getString(AppUtils.KEY_END_DATE));
                                eventEntry.put(AppUtils.KEY_START_TIME, eventSnapshot.getString(AppUtils.KEY_START_TIME));
                                eventEntry.put(AppUtils.KEY_END_TIME, eventSnapshot.getString(AppUtils.KEY_END_TIME));
                                // add hashmap to registered_events of user
                                document.getReference().update(AppUtils.KEY_REGISTERED_EVENTS, FieldValue.arrayUnion(eventEntry))
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("EventsFragment", "(2) User has successfully registered for event " + eventSnapshot.getLong(AppUtils.KEY_EVENT_ID) + "!");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w("EventsFragment", "(2) Error registering user to event " + eventSnapshot.getLong(AppUtils.KEY_EVENT_ID) + "!", e);
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

}
