package com.gabontech.gprstrack.models;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;


public class Geofence
{
    public int id, user_id, active;
    public String name;
    public String coordinates;
    public String polygon_color;
    public List<LatLng> coordinatesList;
}
