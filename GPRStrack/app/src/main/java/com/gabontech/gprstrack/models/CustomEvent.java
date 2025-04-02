package com.gabontech.gprstrack.models;

import java.util.ArrayList;

public class CustomEvent
{
    public int id, user_id;
    public String protocol;
    public String message;
    public int always;
    public ArrayList<CustomEventCondition> conditions;
    public ArrayList<CustomEventTag> tags;
}
