package com.brylle.aus_cs_app_android_j.events;

import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    // declaring some fields.
    private ArrayList<Event> list;
    public EventAdapter(ArrayList<Event> arrayList) {
        this.list = arrayList;
    }

    @NonNull
    @Override
    public EventAdapter.EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull EventAdapter.EventViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    // Inner ViewHolder Class for Event
    public class EventViewHolder extends RecyclerView.ViewHolder {
        TextView nameView, dateView, timeView;

        public EventViewHolder(View itemView) {
            super(itemView);
            Log.v("ViewHolder","in View Holder");
            name = itemView.findViewById(R.id.textView);
            number = itemView.findViewById(R.id.textView2);
            addedOn = itemView.findViewById(R.id.textView3);
        }
    }

}