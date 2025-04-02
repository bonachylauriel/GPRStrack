package com.gabontech.gprstrack.models;


public class AlertSavedGeofence
{
    public int id, zone;
    public String time_from, time_to;

    public AlertSavedGeofence()
    {
        time_from = "00:00";
        time_to = "23:59";
    }
}
