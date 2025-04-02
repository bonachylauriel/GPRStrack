package com.gabontech.gprstrack.utils;

import java.util.Locale;


public class Lang
{
    public static String getCurrentLanguage()
    {
        return Locale.getDefault().getLanguage();
    }
}
