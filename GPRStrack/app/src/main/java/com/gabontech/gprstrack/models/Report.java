package com.gabontech.gprstrack.models;

import java.util.ArrayList;


public class Report
{
    public int id, user_id, type, show_addresses, stops;
    public String title, email;
    public String format, dateFrom, dateTo;
    public int daily, weekly, zones_instead;
    public int speed_limit;
    public ArrayList<Integer> devices;
    public ArrayList<Integer> geofences;

    public Report()
    {
        title = "";
        email = "";
        format = "";
        devices = new ArrayList<>();
        geofences = new ArrayList<>();
    }

}
