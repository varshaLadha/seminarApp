package com.example.ouhvs.seminarapplication.Activities;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.toolbox.StringRequest;
import com.example.ouhvs.seminarapplication.FCM.NotificationUtils;
import com.example.ouhvs.seminarapplication.R;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    Button login,register;
    private static final String TAG = "MainActivity";
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    String regId,token;
    static String message,title;
    private static final int PERMISSION_REQUEST_CODE=200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        onNewIntent(getIntent());

        token= FirebaseInstanceId.getInstance().getToken();
        //Toast.makeText(this, "Token = "+token, Toast.LENGTH_SHORT).show();

        register=(Button)findViewById(R.id.register);
        login=(Button)findViewById(R.id.login);

        if(GlobalClass.pref.contains("userDetail")){
            Intent intent=new Intent(MainActivity.this,Home.class);
            startActivity(intent);
            finish();
        }

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(MainActivity.this,Register.class);
                startActivity(i);
                finish();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(MainActivity.this,Login.class);
                startActivity(i);
                finish();
            }
        });

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals("registrationComplete")) {
                    FirebaseMessaging.getInstance().subscribeToTopic("global");

                    displayFirebaseRegId();

                } else if (intent.getAction().equals("pushNotification")) {
                    // new push notification is received

                    Bundle extras = intent.getExtras();
                    if (extras != null) {
                        String str = extras.getString("foreground");

                        if (str != null) {
                            message = intent.getStringExtra("message");
                            title=intent.getStringExtra("title");
                            Toast.makeText(getApplicationContext(), "Push notification: " + intent.getStringExtra("message"), Toast.LENGTH_LONG).show();
                        }
                    } else {

                        message = intent.getStringExtra("message");
                        title=intent.getStringExtra("title");

                        Toast.makeText(getApplicationContext(), "Push notification: " + message+" "+intent.getStringExtra("title"), Toast.LENGTH_LONG).show();
                    }
                }
            }
        };

        displayFirebaseRegId();

        if(!check_permission()){
            requestPermissions();
        }
    }

    private void displayFirebaseRegId() {
        token= FirebaseInstanceId.getInstance().getToken();

        Log.e(TAG, "Firebase reg id: " + token);

        if (!TextUtils.isEmpty(token)) {
            Log.d("RegId", "Firebase registration Id is "+token);
        }
        else {
            Log.e("Error", "Firebase registration Id is not received yet");
            //Toast.makeText(this, "Firebase Reg Id is not received yet!", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle bundle=intent.getExtras();
//        String title="";
//        if(!TextUtils.isEmpty(bundle.getString("title")));
//        {
//            title=bundle.getString("title");
//        }
        if(bundle!=null){
            title=bundle.getString("title");
            message=bundle.getString("message");

            if(bundle.containsKey("message")){
                Toast.makeText(getApplicationContext(), "Push notification: from new intent MA message " + bundle.getString("message")+" "+bundle.getString("title"), Toast.LENGTH_LONG).show();
            }else if(bundle.containsKey("data")){
                Toast.makeText(getApplicationContext(), "Push notification: from new intent  MA dattapayload " + bundle.getString("data payload"), Toast.LENGTH_LONG).show();
            }

        }

    }

    private boolean check_permission()
    {
        int readPermission= ContextCompat.checkSelfPermission(getApplicationContext(),READ_EXTERNAL_STORAGE);
        int writePermission=ContextCompat.checkSelfPermission(getApplicationContext(),WRITE_EXTERNAL_STORAGE);

        return readPermission== PackageManager.PERMISSION_GRANTED && writePermission==PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions(){
        ActivityCompat.requestPermissions(this,new String[]{READ_EXTERNAL_STORAGE,WRITE_EXTERNAL_STORAGE},PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case PERMISSION_REQUEST_CODE:
                if(grantResults.length>0){
                    boolean readAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean writeAccepted=grantResults[1]==PackageManager.PERMISSION_GRANTED;

                    if(readAccepted && writeAccepted ){
                        Toast.makeText(this, "All permissions are provided by you", Toast.LENGTH_LONG).show();
                    }else {
                        Toast.makeText(this, "App needs to access to your location,camera and storage permission", Toast.LENGTH_SHORT).show();
                    }

                    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                        if (shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE) || shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                            showMessageOKCancel("You need to allow all the permissions or you cannot access the application", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
                                        requestPermissions(new String[]{READ_EXTERNAL_STORAGE,WRITE_EXTERNAL_STORAGE},PERMISSION_REQUEST_CODE);
                                    }
                                }
                            });
                            return;
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener){
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("Ok",okListener)
                .setNegativeButton("Cancel",null)
                .create()
                .show();
    }
}
