package com.example.ouhvs.seminarapplication.FCM;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.ouhvs.seminarapplication.Activities.GlobalClass;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import static android.content.ContentValues.TAG;

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d("MyRefreshedToken", token);

        storeRegIdInPref(token);

        Intent registrationComplete = new Intent("registrationComplete");
        registrationComplete.putExtra("token", token);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    private void storeRegIdInPref(String token) {
        GlobalClass.editor.putString("regId", token);
        GlobalClass.editor.commit();
        Log.d(TAG, "SharedPreference token: " + token);
    }
}
