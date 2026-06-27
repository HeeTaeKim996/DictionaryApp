package com.example.a33_plus_dictionary;

public class AppConfig
{
    private static int VERSION = 11;
    public static int GetVersionByInt() { return VERSION;}
    public static String GetVersionByString()
    {
        return String.valueOf(VERSION / 10) + "_" + String.valueOf(VERSION % 10);
    }

}
