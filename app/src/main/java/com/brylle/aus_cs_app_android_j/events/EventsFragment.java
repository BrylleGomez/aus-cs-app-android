package com.brylle.aus_cs_app_android_j.events;

import android.content.Intent;
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

import com.brylle.aus_cs_app_android_j.AppUtils;
import com.brylle.aus_cs_app_android_j.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
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
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    /* Helper Functions */

    private void addFetchedEventToArrayList(DocumentSnapshot fetchedEvent) {
        // Store details of event fetched from Firebase onto an event object
        // Add this event object to the events array list

        boolean is_past = fetchedEvent.getBoolean(AppUtils.KEY_IS_PAST);
        if (is_past) return;

        // Store info of each fetched event in temp variable
        @NonNull int eventId = fetchedEvent.getLong(AppUtils.KEY_EVENT_ID).intValue();
        String eventName = fetchedEvent.getString(AppUtils.KEY_EVENT_NAME);
        double eventLatitude = fetchedEvent.getGeoPoint(AppUtils.KEY_EVENT_COORDS).getLatitude();
        double eventLongitude = fetchedEvent.getGeoPoint(AppUtils.KEY_EVENT_COORDS).getLongitude();
        String eventLocation = fetchedEvent.getString(AppUtils.KEY_EVENT_LOCATION);
        String startDate = fetchedEvent.getString(AppUtils.KEY_START_DATE);
        String endDate = fetchedEvent.getString(AppUtils.KEY_END_DATE);
        String startTime = fetchedEvent.getString(AppUtils.KEY_START_TIME);
        String endTime = fetchedEvent.getString(AppUtils.KEY_END_TIME);

        // Create an Event object with the retrieved event info (in temp variables)
        // Add created Event object to the container
        eventsList.add(
                new Event(
                        eventId,
                        eventName,
                        eventLatitude,
                        eventLongitude,
                        eventLocation,
                        startDate,
                        endDate,
                        startTime,
                        endTime
                )
        );
        Log.d("EventsFragment", fetchedEvent.toString() + " added!");

    }

    private void loadRecyclerView() {

        // sort events array list according to start date
        Collections.sort(eventsList, new Event.EventStartDateComparator());

        // Set up recycler view
        eventsAdapter = new EventAdapter(eventsList, new EventAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Event event) {
                // Bind a click listener to the reyclerview item

                // create intent, pass event object members as extras, and start activity
                Intent intent = new Intent(getContext(), EventDetailsActivity.class);
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
        eventsView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventsView.setAdapter(eventsAdapter);

    }

}
