package com.example.ouhvs.seminarapplication.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ouhvs.seminarapplication.FCM.NotificationUtils;
import com.example.ouhvs.seminarapplication.ModalClass.UserData;
import com.example.ouhvs.seminarapplication.R;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

public class Home extends BaseClass {

    Gson gson;
    String object;
    UserData userData;
    TextView tvGreeting;

    private BroadcastReceiver mRegistrationBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals("registrationComplete")) {
                    FirebaseMessaging.getInstance().subscribeToTopic("global");

                    //displayFirebaseRegId();

                } else if (intent.getAction().equals("pushNotification")) {
                    // new push notification is received

                    Bundle extras = intent.getExtras();
                    if (extras != null) {
                        String str = extras.getString("foreground");

                        if (str != null) {
                            Toast.makeText(getApplicationContext(), "Push notification: " + intent.getStringExtra("message"), Toast.LENGTH_LONG).show();
                        }
                    } else {

                        String message = intent.getStringExtra("message");

                        Toast.makeText(getApplicationContext(), "Push notification: " + message, Toast.LENGTH_LONG).show();
                    }
                }
            }
        };
    }

    public void initViews() {
        tvGreeting=(TextView)findViewById(R.id.tv_greeting);

        if(!GlobalClass.pref.contains("userDetail")){
            Intent intent=new Intent(Home.this,MainActivity.class);
            startActivity(intent);
            finish();
        }else {
            gson=new Gson();
            object=GlobalClass.pref.getString("userDetail","");
            userData=gson.fromJson(object,UserData.class);
            tvGreeting.setText("Welcome "+userData.getName());
            Log.d("User data",userData.getName()+" "+userData.getMobileno()+" "+userData.getPassword()+" "+userData.getFcmId());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter("registrationComplete"));

        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter("pushNotification"));

        NotificationUtils.clearNotifications(getApplicationContext());
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }
}
