package com.example.a33_plus_dictionary;

import android.content.Context;
import android.util.Log;

public class Calander
{
    private Calander() {}
    private static Calander _instance = new Calander();

    private final int _400_years_to_days = 146097;
    private final int _100_years_to_days = 36524;
    private final int _4_years_to_days = 1461;
    private final int _1_year_to_days = 365;

    private final int[] MonthToDays = new int[]
            { 0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334, 365};
    private final int[] MonthToDays_LeapYear = new int []
            { 0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335, 366};

    private final String[] DatesToDay = new String[]
            { "일", "월", "화", "수", "목", "금", "토" };

    public static Calander Instance()
    {
        return _instance;
    }

    public int GetDates(int year, int month, int date)
    {
        year--;
        int dates = year * 365
                + year / 4 - year / 100 + year / 400;
        year++;

        if(IsLeapYear(year))
        {
            dates += MonthToDays_LeapYear[month-1];
        }
        else
        {
            dates += MonthToDays[month-1];
        }

        dates += date;

        return dates;
    }

    private String GetCalander(int dates)
    {
        int year = 1;
        int month = 0;
        int date = dates;

        int _400 = (date - 1) / _400_years_to_days;
        year += _400 * 400;
        date -= _400 * _400_years_to_days;

        int _100 = (date - 1) / _100_years_to_days;
        if(_100 == 4) _100 = 3;
        year += _100 * 100;
        date -= _100 * _100_years_to_days;

        int _4 = (date - 1) / _4_years_to_days;
        if(_4 == 25) _4 = 24;
        year += _4 * 4;
        date -= _4 * _4_years_to_days;

        int _1 = (date - 1) / _1_year_to_days;
        if(_1 == 4) _1 = 3;
        year += _1;
        date -= _1 * _1_year_to_days;

        if(IsLeapYear(year))
        {
            for(int i = 1; i <= 12; i++)
            {
                if(date <= MonthToDays_LeapYear[i])
                {
                    month = i;
                    date -= MonthToDays_LeapYear[i-1];
                    break;
                }
            }
        }
        else
        {
            for(int i = 1; i <= 12; i++)
            {
                if(date <= MonthToDays[i])
                {
                    month = i;
                    date -= MonthToDays[i-1];
                    break;
                }
            }
        }


        return CalanderToString(year, month, date) + "/" + DatesToDay[dates % 7];
    }

    private String CalanderToString(int year, int month, int date)
    {
        return String.format("%04d", year) + "/" + String.format("%02d", month)
                + "/" + String.format("%02d", date);
    }

    private boolean IsLeapYear(int year)
    {
        return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
    }










    private  String temp = "";
    public void TempTest(Context context)
    {
        Test(2026);

        Test(1);
        Test(2);

        Test(4);
        Test(5);

        Test(100);
        Test(101);

        Test(400);
        Test(401);

        Test(2000);
        Test(2400);

        Test(1900);
        Test(2100);
        Test(2200);
        Test(2300);

        Log.d("Calander", temp);
    }

    private void Test(int year)
    {
        tes(year, 8, 21);
        tes(year, 1, 1);
        tes(year, 12, 31);
    }


    private void tes(int year, int month, int day)
    {

        int days = GetDates(year, month, day);
        temp += CalanderToString(year, month, day) + " -> "
                + GetCalander(days) + "(" + String.valueOf(days) + ")\n";
    }

}
