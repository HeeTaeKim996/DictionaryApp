package com.example.a33_plus_dictionary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.a33_plus_dictionary.databinding.LogInformBinding;
import androidx.appcompat.app.AlertDialog;
public class DebugHelper
{
    private static DebugHelper _instance = new DebugHelper();
    private DebugHelper(){}

    public static DebugHelper Instance()
    {
        return _instance;
    }

    public void ShowInformInterface(Context context, ViewGroup parent, String informString)
    {
        LogInformBinding logBinding = LogInformBinding.inflate(LayoutInflater.from(context));
        AlertDialog logDialog = new AlertDialog.Builder(context).setView(logBinding.getRoot())
                .create();
        logDialog.show();

        logBinding.textInform.setText(informString);
        logBinding.buttonOk.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                logDialog.dismiss();
            }
        });
    }

}
