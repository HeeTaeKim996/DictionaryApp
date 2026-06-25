package com.example.a33_plus_dictionary;

import java.util.ArrayList;

public class DicData
{
    public String dicName;
    public ArrayList<SPair> data;
    public ArrayList<Integer> shakePos;

    public DicData(String InDicName ,ArrayList<SPair> InData)
    {
        dicName = InDicName;
        if(InData == null)
        {
            data = new ArrayList<SPair>();
            shakePos = new ArrayList<Integer>();
        }
        else
        {
            data = InData;
            shakePos = new ArrayList<Integer>(data.size());
            for(int i = 0; i < data.size(); i++)
            {
                shakePos.add(i);
            }
        }
    }
}
