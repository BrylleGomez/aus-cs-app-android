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
import com.google.firebase.firestore.GeoPoint;
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

                            // load recycler view from adapter
                            loadRecyclerView();

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

    private void addFetchedEventToArrayList(HashMap<String, Object> eventDetails) {
        // Create an Event object with the retrieved event info (in temp variables)
        // Add created Event object to the container
        Log.d("RegisteredActivity", eventDetails.toString());
        // retrieve event details from the hashmap and store in temp variables
        int tempEventID = ((Long) eventDetails.get("event_id")).intValue();
        String tempEventName = (String) eventDetails.get(AppUtils.KEY_EVENT_NAME);
        double tempEventLatitude = ((GeoPoint) eventDetails.get(AppUtils.KEY_EVENT_COORDS)).getLatitude();
        double tempEventLongitude = ((GeoPoint) eventDetails.get(AppUtils.KEY_EVENT_COORDS)).getLongitude();
        String tempEventLocation = (String) eventDetails.get(AppUtils.KEY_EVENT_LOCATION);
        String tempStartDate = (String) eventDetails.get(AppUtils.KEY_START_DATE);
        String tempEndDate = (String) eventDetails.get(AppUtils.KEY_END_DATE);
        String tempStartTime = (String) eventDetails.get(AppUtils.KEY_START_TIME);
        String tempEndTime = (String) eventDetails.get(AppUtils.KEY_END_TIME);
        // create Event object, add event details from temp variables, and add event to event list array
        regEventsList.add(
                new Event(
                        tempEventID,
                        tempEventName,
                        tempEventLatitude,
                        tempEventLongitude,
                        tempEventLocation,
                        tempStartDate,
                        tempEndDate,
                        tempStartTime,
                        tempEndTime
                )
        );
    }

    private void loadRecyclerView() {

        // set up recycler view
        regEventsAdapter = new RegEventAdapter(regEventsList, new RegEventAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Event event) {
                // Bind a click listener to the recyclerview item

                // create intent, pass clicked event as parcelable extra, and start activity
                Intent intent = new Intent(getApplicationContext(), RegEventDetailsActivity.class);
                intent.putExtra("eventID", event.getID());
                intent.putExtra("eventName", event.getName());
                intent.putExtra("eventDates", event.getDates());
                intent.putExtra("eventTimings", event.getTimes());
                intent.putExtra("eventLocation", event.getLocation());
                intent.putExtra("eventLatitude", event.getLatitude());
                intent.putExtra("eventLongitude", event.getLongitude());
                startActivity(intent);
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

}
