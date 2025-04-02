package com.gabontech.gprstrack.models;

import java.util.ArrayList;


public class Alert
{
    public int id, user_id, active;
    public String name, email, mobile_phone;
    public int overspeed_speed, overspeed_distance, ac_alarm;
    public ArrayList<Integer> devices;
    public ArrayList<Integer> drivers;
    public ArrayList<AlertSavedGeofence> geofences;
    public ArrayList<AlertSavedEvent> events_custom;

    public Alert()
    {
        name = "";
        email = "";
        mobile_phone = "";
        devices = new ArrayList<>();
        drivers = new ArrayList<>();
        geofences = new ArrayList<>();
        events_custom = new ArrayList<>();
    }
}