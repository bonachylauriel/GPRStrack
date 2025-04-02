package com.gabontech.gprstrack.models;

public class Service
{
    public int id;
    public String name;
    public String value;

    public Service(Service another)
    {
        this.id = another.id;
        this.name = another.name;
        this.value = another.value;
    }
}