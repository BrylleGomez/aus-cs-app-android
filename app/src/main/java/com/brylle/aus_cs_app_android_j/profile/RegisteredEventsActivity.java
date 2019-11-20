package com.brylle.aus_cs_app_android_j.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import com.brylle.aus_cs_app_android_j.AppUtils;
import com.brylle.aus_cs_app_android_j.R;
import com.brylle.aus_cs_app_android_j.events.Event;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegisteredEventsActivity extends AppCompatActivity {

    /* Variables */

    private ArrayList<Event> regEventsList = new ArrayList<>();
    private RecyclerView regEventsView;
    private RegEventAdapter regEventsAdapter;
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private CollectionReference firestoreUserList = firebaseFirestore.collection("users");
    private CollectionReference firestoreEventList = firebaseFirestore.collection("events");
    private FrameLayout messageFrame;
    private TextView noEventsMessage;

    /* Initializer Functions */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registered_events);

        // Initialize Objects
        regEventsView = findViewById(R.id.regevents_recyclerview);
        messageFrame = findViewById(R.id.regevents_frame);
        noEventsMessage = findViewById(R.id.regevents_textview_message);

        // Hide frame layout containing message
        messageFrame.setVisibility(View.GONE);

        // (1) Look for user in database
        if (currentUser != null) {      // Fetch user from database using query
            firestoreUserList.whereEqualTo(AppUtils.KEY_EMAIL, currentUser.getEmail())      // query: look for user document that matches email of current user
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (DocumentSnapshot document : queryDocumentSnapshots) {              // loop through every database hit, SHOULD ONLY BE ONE MATCH THO

                                Map<String, Object> map = document.getData();                       // get a map containing all event fields
                                for (Map.Entry<String, Object> entry : map.entrySet()) {            // loop through all event fields in map
                                    if (entry.getKey().equals(AppUtils.KEY_REGISTERED_EVENTS)) {    // look for the "registered_events" array
                                        ArrayList<HashMap<String, Object>> registeredEvents = (ArrayList<HashMap<String, Object>>) entry.getValue();    // retrieve an array list containing all events in the "registered_events" array
                                        for (HashMap<String, Object> eventDeets : registeredEvents) {       // loop through all events (which are hashmaps of event details)
                                            // Create an Event object with the retrieved event info (in temp variables)
                                            // Add created Event object to the container
                                            Log.d("RegisteredActivity", eventDeets.toString());
                                            // retrieve event details from the hashmap and store in temp variables
                                            int tempEventID = ((Long) eventDeets.get("event_id")).intValue();
                                            String tempEventName = (String) eventDeets.get(AppUtils.KEY_EVENT_NAME);
                                            String tempStartDate = (String) eventDeets.get(AppUtils.KEY_START_DATE);
                                            String tempEndDate = (String) eventDeets.get(AppUtils.KEY_END_DATE);
                                            String tempStartTime = (String) eventDeets.get(AppUtils.KEY_START_TIME);
                                            String tempEndTime = (String) eventDeets.get(AppUtils.KEY_END_TIME);
                                            // create Event object, add event details from temp variables, and add event to event list array
                                            regEventsList.add(
                                                    new Event(
                                                            tempEventID,
                                                            tempEventName,
                                                            tempStartDate,
                                                            tempEndDate,
                                                            tempStartTime,
                                                            tempEndTime
                                                    )
                                            );
                                        }
                                    }
                                }
                            }

                            // set up recycler view
                            regEventsAdapter = new RegEventAdapter(regEventsList, new RegEventAdapter.OnItemClickListener() {
                                @Override
                                public void onItemClick(Event event) {
                                    // Unregister a user from an event when the "Unregister" button is clicked
                                    Toast.makeText(getApplicationContext(), "Pressed event " + event.getID(), Toast.LENGTH_SHORT).show();
                                    final int eventID = event.getID();
                                    final String userEmail = currentUser.getEmail();


                                    // (1) Remove user from event's registered_students array
                                    firestoreEventList.whereEqualTo(AppUtils.KEY_EVENT_ID, eventID)      // query: look for event document that matches event id of clicked event
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                for (DocumentSnapshot document : queryDocumentSnapshots) {   // loop through every database hit, SHOULD ONLY BE ONE MATCH THO
                                                    final DocumentSnapshot eventSnapshot = document;
                                                    // Remove student from event's registered_students array
                                                    document.getReference().update(AppUtils.KEY_REGISTERED_STUDENTS, FieldValue.arrayRemove(userEmail))
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Log.d("EventsFragment", "2: User has successfully unregistered from event " + eventID + "!");
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.w("EventsFragment", "2: Error unregistering user from event " + eventID + "!", e);
                                                            }
                                                        });

                                                    // (2) Remove event object to user's registered_events
                                                    firestoreUserList.whereEqualTo(AppUtils.KEY_EMAIL, currentUser.getEmail())      // query: look for user document that matches email of current user
                                                            .get()
                                                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                @Override
                                                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                                    for (DocumentSnapshot document : queryDocumentSnapshots) {      // loop through every database hit, SHOULD ONLY BE ONE MATCH THO
                                                                        final HashMap<String,Object> eventEntry = new HashMap<>();
                                                                        eventEntry.put(AppUtils.KEY_EVENT_ID, eventSnapshot.getLong(AppUtils.KEY_EVENT_ID));
                                                                        eventEntry.put(AppUtils.KEY_EVENT_NAME, eventSnapshot.getString(AppUtils.KEY_EVENT_NAME));
                                                                        eventEntry.put(AppUtils.KEY_START_DATE, eventSnapshot.getString(AppUtils.KEY_START_DATE));
                                                                        eventEntry.put(AppUtils.KEY_END_DATE, eventSnapshot.getString(AppUtils.KEY_END_DATE));
                                                                        eventEntry.put(AppUtils.KEY_START_TIME, eventSnapshot.getString(AppUtils.KEY_START_TIME));
                                                                        eventEntry.put(AppUtils.KEY_END_TIME, eventSnapshot.getString(AppUtils.KEY_END_TIME));
                                                                        document.getReference().update(AppUtils.KEY_REGISTERED_EVENTS, FieldValue.arrayRemove(eventEntry))
                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void aVoid) {
                                                                                        Log.d("EventsFragment", "1: User has unsuccessfully registered for event " + eventID + "!");
                                                                                        // Update UI recycler view
                                                                                        //regEventsList.remove(event)
                                                                                        finish();
                                                                                        startActivity(getIntent());
                                                                                        // recreate();
                                                                                    }
                                                                                })
                                                                                .addOnFailureListener(new OnFailureListener() {
                                                                                    @Override
                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                        Log.w("EventsFragment", "1: Error unregistering user to event " + eventID + "!", e);
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
                                        });

                                }
                            });
                            regEventsView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                            regEventsView.setAdapter(regEventsAdapter);
                            // if user did not register for any events, display message
                            if (regEventsList.isEmpty()) {
                                messageFrame.setVisibility(View.VISIBLE);
                            } else {
                                messageFrame.setVisibility(View.GONE);
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

    /* Helper Functions */

}
