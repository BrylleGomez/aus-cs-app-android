package com.brylle.aus_cs_app_android_j.events;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.brylle.aus_cs_app_android_j.AppUtils;
import com.brylle.aus_cs_app_android_j.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class EventsFragment extends Fragment {

    /* Variables */

    private ArrayList<Event> eventsList = new ArrayList<>();                                                // list to store event objects retrieved from Firebase
    private RecyclerView eventsView;                                                                        // recycler view to display objects
    private EventAdapter eventsAdapter;                                                                     // adapter to bind event objects in array list to recycler view
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();                         // retrieve current Firebase user
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();                          // retrieve Firestore instance
    private CollectionReference firestoreEventList = firebaseFirestore.collection("events");    // retrieve reference to "events" collection

    /* Initializer Functions */

    public EventsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Objects
        eventsView = Objects.requireNonNull(getView()).findViewById(R.id.events_recyclerview);

        // Fetches all event database entries and stores them in an array list of event objects
        firestoreEventList.get()                                                // Fetch all event entries from database
            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot fetchedEvents) {

                    for (DocumentSnapshot fetchedEvent : fetchedEvents) {       // Iterate through all fetched events
                        addFetchedEventToArrayList(fetchedEvent);
                    }

                    // load recycler view from adapter
                    loadRecyclerView();

                }

            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("EventsFragment", "Error fetching events: ", e);
                }
            });

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Log.d("Menu", "onCreate called and set hasoptionsmenu as true!");
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        Log.d("Menu", "Menu created from fragment!");
    }

    /* Helper Functions */

    private void addFetchedEventToArrayList(DocumentSnapshot fetchedEvent) {
        // Store details of event fetched from Firebase onto an event object
        // Add this event object to the events array list

        // Store info of each fetched event in temp variable
        @NonNull int eventId = fetchedEvent.getLong("event_id").intValue();
        String eventName = fetchedEvent.getString("event_name");
        String startDate = fetchedEvent.getString("start_date");
        String endDate = fetchedEvent.getString("end_date");
        String startTime = fetchedEvent.getString("start_time");
        String endTime = fetchedEvent.getString("end_time");

        // Create an Event object with the retrieved event info (in temp variables)
        // Add created Event object to the container
        eventsList.add(
                new Event(
                        eventId,
                        eventName,
                        startDate,
                        endDate,
                        startTime,
                        endTime
                )
        );
        Log.d("EventsFragment", fetchedEvent.getString("event_name") + "added!");

    }

    private void registerEvent(Event event) {
        // Register a user to an event when the "Register" button is clicked
        // (1) Adds a user to an event's registered_students field in Firebase
        // and (2) adds an event to a user's registered_events in Firebase
        Toast.makeText(getContext(), "Retrieving event " + event.getID(), Toast.LENGTH_LONG).show();
        final int eventID = event.getID();


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

    private void loadRecyclerView() {

        // Set up recycler view
        eventsAdapter = new EventAdapter(eventsList, new EventAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Event event) {
                // Bind a click listener to the "Register Button"
                // of each recycler view item
                registerEvent(event);
            }
        });
        eventsView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventsView.setAdapter(eventsAdapter);

    }

}
