package com.brylle.aus_cs_app_android_j.events;

import android.util.Log;

public class Event {

    // class to represent event entries in the events page of the app
    // the following private members are the same as the fields of an event entry in a database
    private int event_id;
    private String event_name;
    private String start_date;
    private String end_date;
    private String start_time;
    private String end_time;

    // constructor
    public Event(
            int event_id,
            String event_name,
            String start_date,
            String end_date,
            String start_time,
            String end_time) {
        this.event_id = event_id;
        this.event_name = event_name;
        this.start_date = start_date;
        this.end_date = end_date;
        this.start_time = start_time;
        this.end_time = end_time;
    }

    // prints a log output of the event object
    public void print() {
        Log.d("EventClass", event_name + "\nDate: " + start_date + " - " + end_date + "\nTime: " + start_time + " - " + end_time);
    }

    // returns the event_id field of this class
    public int getID(){
        return event_id;
    }

    // returns the event_name field of this class
    public String getName(){
        return event_name;
    }

    // returns a formatted string with the start and end dates of an event object
    public String getDates() {
        return "Date: " + start_date + " - " + end_date;
    }

    // returns a formatted string with the start and end times of an event object
    public String getTimes(){
        return "Time: " + start_time + " - " + end_time;
    }

}

