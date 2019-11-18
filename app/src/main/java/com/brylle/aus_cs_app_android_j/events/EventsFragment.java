package com.brylle.aus_cs_app_android_j.events;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import java.util.Objects;

public class EventsFragment extends Fragment {

    /* Variables */

    private ArrayList<Event> eventsList = new ArrayList<>();
    private RecyclerView eventsView;
    private EventAdapter eventsAdapter;
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private CollectionReference firestoreUserList = firebaseFirestore.collection("users");
    private CollectionReference firestoreEventList = firebaseFirestore.collection("events");

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

        // Fetches all event database entries and stores them in a list of event objects
        firebaseFirestore.collection("events")
            .get()                                  // Fetch all event entries from database
            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {      // Iterate through all fetched events
                        // Store info of each fetched event in temp variable
                        @NonNull int eventId = document.getLong("event_id").intValue();
                        String eventName = document.getString("event_name");
                        String startDate = document.getString("start_date");
                        String endDate = document.getString("end_date");
                        String startTime = document.getString("start_time");
                        String endTime = document.getString("end_time");
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
                        Log.d("EventsFragment", document.getString("event_name") + "added!");
                    }

                    // set up recycler view
                    eventsAdapter = new EventAdapter(eventsList, new EventAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(Event event) {
                            // Adds a user to an event's list of registered students
                            // and adds an event to a user's list of registered events
                            // when the register button of each event is clicked
                            Toast.makeText(getContext(), "Retrieving event " + event.getID(), Toast.LENGTH_LONG).show();
                        }
                    });
                    eventsView.setLayoutManager(new LinearLayoutManager(getContext()));
                    eventsView.setAdapter(eventsAdapter);
                }

            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w("EventsFragment", "Error getting documents: ", e);
                }
            });

    }

    /* Helper Functions */

}
