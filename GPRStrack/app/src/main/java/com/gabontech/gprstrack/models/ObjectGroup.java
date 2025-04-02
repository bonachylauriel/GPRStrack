package com.gabontech.gprstrack.models;


public class ObjectGroup
{
    public int id, user_id;
    public String title;

    @Override
    public String toString() {
        return title;
    }
}
