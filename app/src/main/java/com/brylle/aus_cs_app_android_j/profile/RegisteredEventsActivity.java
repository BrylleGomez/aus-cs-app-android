package com.brylle.aus_cs_app_android_j.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import com.brylle.aus_cs_app_android_j.AppUtils;
import com.brylle.aus_cs_app_android_j.R;
import com.brylle.aus_cs_app_android_j.events.Event;
import com.brylle.aus_cs_app_android_j.home.AboutActivity;
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

    private ArrayList<Event> regEventsList = new ArrayList<>();                                                 // list to store objects retrieved from Firebase
    private RecyclerView regEventsView;                                                                         // recycler view to display objects
    private RegEventAdapter regEventsAdapter;                                                                   // adapter to bind event objects in array list to recycler view
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();                             // retrieve current Firebase user
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();                              // retrieve Firestore instance
    private CollectionReference firestoreEventList = firebaseFirestore.collection("events");       // retrieve reference to "events" collection
    private FrameLayout messageFrame;

    /* Initializer Functions */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registered_events);

        // Initialize Objects
        regEventsView = findViewById(R.id.regevents_recyclerview);
        messageFrame = findViewById(R.id.regevents_frame);

        // Hide frame layout containing message
        messageFrame.setVisibility(View.GONE);

        // Fetches all event entries in a user's registered_events field and stores them in an array list of event objects
        if (currentUser != null) {      // Fetch user from database using query
            firebaseFirestore.collection("users").whereEqualTo(AppUtils.KEY_EMAIL, currentUser.getEmail())      // query: look for user document that matches email of current user
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (DocumentSnapshot fetchedUser : queryDocumentSnapshots) {              // loop through every database hit, SHOULD ONLY BE ONE MATCH THO
                                Map<String, Object> map = fetchedUser.getData();                       // get a map containing all user fields
                                for (Map.Entry<String, Object> entry : map.entrySet()) {            // loop through all user fields in map
                                    if (entry.getKey().equals(AppUtils.KEY_REGISTERED_EVENTS)) {    // look for the "registered_events" array
                                        ArrayList<HashMap<String, Object>> registeredEvents = (ArrayList<HashMap<String, Object>>) entry.getValue();    // retrieve an array list containing all events in the "registered_events" array
                                        for (HashMap<String, Object> eventDetails : registeredEvents) {       // loop through all events (which are hashmaps of event details)
                                            addFetchedEventToArrayList(eventDetails);
                                        }
                                    }
                                }
                            }

                            // set up recycler view
                            regEventsAdapter = new RegEventAdapter(regEventsList, new RegEventAdapter.OnItemClickListener() {
                                @Override
                                public void onItemClick(Event event) {
                                    unregisterEvent(event);
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

    private void unregisterEvent(Event event) {
        // Unregister a user from an event when the "Unregister" button is clicked
        // (1) Removes a user from an event's registered_students field in Firebase
        // and (2) removes an event from a user's registered_events field in Firebase

        // (1) Remove user from event's registered_students array
        firestoreEventList.whereEqualTo(AppUtils.KEY_EVENT_ID, event.getID())      // query: look for event document that matches event id of clicked event
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

                        }
                    }
                });

    }

    private void addFetchedEventToArrayList(HashMap<String, Object> eventDetails) {
        // Create an Event object with the retrieved event info (in temp variables)
        // Add created Event object to the container
        Log.d("RegisteredActivity", eventDetails.toString());
        // retrieve event details from the hashmap and store in temp variables
        int tempEventID = ((Long) eventDetails.get("event_id")).intValue();
        String tempEventName = (String) eventDetails.get(AppUtils.KEY_EVENT_NAME);
        String tempStartDate = (String) eventDetails.get(AppUtils.KEY_START_DATE);
        String tempEndDate = (String) eventDetails.get(AppUtils.KEY_END_DATE);
        String tempStartTime = (String) eventDetails.get(AppUtils.KEY_START_TIME);
        String tempEndTime = (String) eventDetails.get(AppUtils.KEY_END_TIME);
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
                            eventEntry.put(AppUtils.KEY_START_DATE, fetchedEvent.getString(AppUtils.KEY_START_DATE));
                            eventEntry.put(AppUtils.KEY_END_DATE, fetchedEvent.getString(AppUtils.KEY_END_DATE));
                            eventEntry.put(AppUtils.KEY_START_TIME, fetchedEvent.getString(AppUtils.KEY_START_TIME));
                            eventEntry.put(AppUtils.KEY_END_TIME, fetchedEvent.getString(AppUtils.KEY_END_TIME));
                            document.getReference().update(AppUtils.KEY_REGISTERED_EVENTS, FieldValue.arrayRemove(eventEntry))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d("EventsFragment", "1: User has unsuccessfully registered for event " + fetchedEvent.getId() + "!");
                                            // Update UI recycler view
                                            refreshActivity();
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

    private void refreshActivity() {
        finish();
        startActivity(getIntent());
    }

}
