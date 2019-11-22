package com.brylle.aus_cs_app_android_j.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.brylle.aus_cs_app_android_j.R;
import com.brylle.aus_cs_app_android_j.events.Event;

import java.util.ArrayList;

public class RegEventAdapter extends RecyclerView.Adapter<RegEventAdapter.EventViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Event event);
    }

    // declaring some fields.
    private ArrayList<Event> list;
    private RegEventAdapter.OnItemClickListener clickListener;

    public RegEventAdapter(ArrayList<Event> arrayList, RegEventAdapter.OnItemClickListener clickListener) {
        this.list = arrayList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public RegEventAdapter.EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_regevents_event_viewholder,parent,false);

        return new RegEventAdapter.EventViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RegEventAdapter.EventViewHolder holder, int position) {
        holder.bind(list.get(position), clickListener);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // Inner ViewHolder Class for Event
    public class EventViewHolder extends RecyclerView.ViewHolder {
        TextView nameView, dateView, timeView;

        public EventViewHolder(View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.fragment_regevents_event_viewholder_name);
            dateView = itemView.findViewById(R.id.fragment_regevents_event_viewholder_date);
            timeView = itemView.findViewById(R.id.fragment_regevents_event_viewholder_time);
        }

        public void bind(final Event event, final OnItemClickListener clickListener) {
            nameView.setText(event.getName());
            dateView.setText(event.getDates());
            timeView.setText(event.getTimes());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickListener.onItemClick(event);
                }
            });
        }

    }

}