package com.example.ouhvs.seminarapplication.Activities;

import android.app.Application;
import android.content.SharedPreferences;

public class GlobalClass extends Application{

    public static SharedPreferences pref;
    public static SharedPreferences.Editor editor;

    @Override
    public void onCreate() {
        super.onCreate();

        pref=getApplicationContext().getSharedPreferences("ah_firebase", 0);
        editor=pref.edit();
    }
}
