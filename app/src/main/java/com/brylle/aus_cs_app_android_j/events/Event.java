package com.brylle.aus_cs_app_android_j.events;

import android.util.Log;

import com.google.firebase.firestore.GeoPoint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class Event {

    // class to represent event entries in the events page of the app
    // the following private members are the same as the fields of an event entry in a database
    private int event_id;
    private String event_name;
    private double event_latitude;
    private double event_longitude;
    private String event_location;
    private String start_date;
    private String end_date;
    private String start_time;
    private String end_time;

    // constructor
    public Event(
            int event_id,
            String event_name,
            double event_latitude,
            double event_longitude,
            String event_location,
            String start_date,
            String end_date,
            String start_time,
            String end_time) {
        this.event_id = event_id;
        this.event_name = event_name;
        this.event_latitude = event_latitude;
        this.event_longitude = event_longitude;
        this.event_location = event_location;
        this.start_date = start_date;
        this.end_date = end_date;
        this.start_time = start_time;
        this.end_time = end_time;
    }

    // prints a log output of the event object
    public void print() {
        Log.d("Debug", this.event_name + "\nDate: " + this.start_date + " - " + this.end_date + "\nTime: " + this.start_time + " - " + this.end_time);
    }

    // returns the event_id field of this class
    public int getID(){
        return this.event_id;
    }

    // returns the event_name field of this class
    public String getName(){
        return this.event_name;
    }

    public String getStartDate() {
        return this.start_date;
    }

    public String getEndDate() {
        return this.end_date;
    }

    public String getStartTime() {
        return this.start_time;
    }

    public String getEndTime() {
        return this.end_time;
    }

    // returns a formatted string with the start and end dates of an event object
    public String getDates() {
        return "Date: " + this.start_date + " - " + this.end_date;
    }

    // returns a formatted string with the start and end times of an event object
    public String getTimes(){
        return "Time: " + this.start_time + " - " + this.end_time;
    }

    public double getLatitude() {
        return this.event_latitude;
    }

    public double getLongitude() {
        return this.event_longitude;
    }

    // returns the event_location of this class
    public String getLocation() {
        return this.event_location;
    }

    // Comparator class to enable sorting event arraylists by start date
    public static class EventStartDateComparator implements Comparator<Event> {
        @Override
        public int compare(Event event1, Event event2) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
            Date date1;
            Date date2;
            try {
                // parse dates from text
                date1 = dateFormat.parse(event1.getStartDate());
                date2 = dateFormat.parse(event2.getStartDate());
                return date1.compareTo(date2);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return 0;
        }
    }

}

