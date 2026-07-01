package com.example.a33_plus_dictionary;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity
{
    MainInterface mainInterface;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mainInterface = new MainInterface();
        mainInterface.Initialize(this);
        setContentView(mainInterface.GetRootView());

//        Calander.Instance().TempTest(this);
    }
}