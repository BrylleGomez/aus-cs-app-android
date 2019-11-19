package com.brylle.aus_cs_app_android_j.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.brylle.aus_cs_app_android_j.AppUtils;
import com.brylle.aus_cs_app_android_j.R;
import com.brylle.aus_cs_app_android_j.events.Event;
import com.brylle.aus_cs_app_android_j.events.EventAdapter;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisteredEventsActivity extends AppCompatActivity {

    /* Variables */

    private ArrayList<Event> regEventsList = new ArrayList<>();
    private RecyclerView regEventsView;
    private EventAdapter regEventsAdapter;
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private CollectionReference firestoreUserList = firebaseFirestore.collection("users");
    private CollectionReference firestoreEventList = firebaseFirestore.collection("events");

    /* Initializer Functions */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registered_events);

        // Initialize Objects
        regEventsView = findViewById(R.id.regevents_recyclerview);

        // (1) Look for user in database
        if (currentUser != null) {      // Fetch user from database using query
            firestoreUserList.whereEqualTo(AppUtils.KEY_EMAIL, currentUser.getEmail())      // query: look for user document that matches email of current user
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (DocumentSnapshot document : queryDocumentSnapshots) {      // loop through every database hit, SHOULD ONLY BE ONE MATCH THO

                                Map<String, Object> map = document.getData();
                                for (Map.Entry<String, Object> entry : map.entrySet()) {
                                    if (entry.getKey().equals(AppUtils.KEY_REGISTERED_EVENTS)) {
                                        ArrayList<HashMap<String, Object>> registeredEvents = (ArrayList<HashMap<String, Object>>) entry.getValue();
                                        for (HashMap<String, Object> eventDeets : registeredEvents) {
                                            // Create an Event object with the retrieved event info (in temp variables)
                                            // Add created Event object to the container
                                            Log.d("TAG2", eventDeets.toString());
                                            int tempEventID = ((Long) eventDeets.get("event_id")).intValue();
                                            String test = String.valueOf(tempEventID);
                                            Log.d("TAG2", test);
                                            String tempEventName = (String) eventDeets.get(AppUtils.KEY_EVENT_NAME);
                                            String tempStartDate = (String) eventDeets.get(AppUtils.KEY_START_DATE);
                                            String tempEndDate = (String) eventDeets.get(AppUtils.KEY_END_DATE);
                                            String tempStartTime = (String) eventDeets.get(AppUtils.KEY_START_TIME);
                                            String tempEndTime = (String) eventDeets.get(AppUtils.KEY_END_TIME);
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
                            regEventsAdapter = new EventAdapter(regEventsList, new EventAdapter.OnItemClickListener() {
                                @Override
                                public void onItemClick(Event event) {
                                    // Adds a user to an event's list of registered students
                                    // and adds an event to a user's list of registered events
                                    // when the register button of each event is clicked
                                    Toast.makeText(getApplicationContext(), "Pressed event " + event.getID(), Toast.LENGTH_SHORT).show();
                                    //final int eventID = event.getID();
                                }
                            });
                            regEventsView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                            regEventsView.setAdapter(regEventsAdapter);

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
