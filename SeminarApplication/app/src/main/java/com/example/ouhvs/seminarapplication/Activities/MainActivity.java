package com.example.ouhvs.seminarapplication.Activities;

import android.support.v7.app.ActionBar;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.ouhvs.seminarapplication.FCM.Constants;
import com.example.ouhvs.seminarapplication.FCM.NotificationUtils;
import com.example.ouhvs.seminarapplication.R;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    Button login,register;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private static final int PERMISSION_REQUEST_CODE=200;
    ActionBar ab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        onNewIntent(getIntent());

        register=(Button)findViewById(R.id.register);
        login=(Button)findViewById(R.id.login);

        ab=getSupportActionBar();
        ab.setTitle("Share Image");

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
                //finish();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(MainActivity.this,Login.class);
                startActivity(i);
                //finish();
            }
        });

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals("pushNotification")) {
                    // new push notification is received

                    Bundle extras = intent.getExtras();
                    if (extras != null) {
                        String str = extras.getString("foreground");

                        if (str != null) {
                            Constants.message = intent.getStringExtra("message");
                            Constants.title=intent.getStringExtra("title");
                            Toast.makeText(getApplicationContext(), Constants.title, Toast.LENGTH_LONG).show();
                        }
                    } else {

                        Constants.message = intent.getStringExtra("message");
                        Constants.title=intent.getStringExtra("title");

                        Toast.makeText(getApplicationContext(), Constants.title, Toast.LENGTH_LONG).show();
                    }
                }
            }
        };

        if(!check_permission()){
            requestPermissions();
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
        if(bundle!=null){
            Constants.title=bundle.getString("title");
            Constants.message=bundle.getString("message");

            if(bundle.containsKey("message")){
                Toast.makeText(getApplicationContext(), Constants.title, Toast.LENGTH_LONG).show();
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
