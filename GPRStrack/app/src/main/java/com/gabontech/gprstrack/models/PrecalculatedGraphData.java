package com.gabontech.gprstrack.models;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;


public class PrecalculatedGraphData
{
    public String sensor_id;
    public ArrayList<Float> sensorDataValues = new ArrayList<>();
    public ArrayList<Long> sensorDataTimestamps = new ArrayList<>();
    public ArrayList<Entry> yVals;
    public ArrayList<String> xVals;
}
