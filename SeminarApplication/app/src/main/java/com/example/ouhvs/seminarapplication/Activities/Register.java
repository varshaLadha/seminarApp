package com.example.ouhvs.seminarapplication.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.ouhvs.seminarapplication.ModalClass.UserData;
import com.example.ouhvs.seminarapplication.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

public class Register extends AppCompatActivity {

    EditText uname,passwd,mno;
    String username,password,mobileno,regId;
    Button register;
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        uname=(EditText)findViewById(R.id.userName);
        passwd=(EditText)findViewById(R.id.password);
        mno=(EditText)findViewById(R.id.mobileno);
        register=(Button)findViewById(R.id.register);

        regId=GlobalClass.pref.getString("regId",null);

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals("registrationComplete")) {
                    FirebaseMessaging.getInstance().subscribeToTopic("global");

//                    displayFirebaseRegId();

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

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username=uname.getText().toString().trim();
                password=passwd.getText().toString().trim();
                mobileno=mno.getText().toString().trim();

                StringRequest sr=new StringRequest(Request.Method.POST, "https://lanetteamvarsha.000webhostapp.com/seminarApi/register.php", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject=new JSONObject(response);
                        if(jsonObject.getInt("success")==1){
                            Toast.makeText(Register.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                            UserData userData=new UserData(username,password,mobileno,regId);
                            Gson gson=new Gson();
                            String object=gson.toJson(userData);
                            GlobalClass.editor.putString("userDetail",object);
                            GlobalClass.editor.commit();
                            Intent intent=new Intent(Register.this,Home.class);
                            startActivity(intent);
                            finish();
                        }else
                        {
                            Toast.makeText(Register.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                            Log.i("Details ","Username "+username+" Password "+password+" MobileNo "+mobileno+" FCMID "+regId);
                        }
                    } catch (JSONException e) {
                        Toast.makeText(Register.this, "Error "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("Exception",e.getMessage());
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(Register.this, "Failure : "+error.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Error response",error.getMessage());
                Log.e("Stack trace",error.getStackTrace().toString());
            }
            }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> params= new HashMap<String,String>();
                    params.put("username",username);
                    params.put("password",password);
                    params.put("mobileno",mobileno);
                    params.put("fcmId",regId);
                    return params;
                }
            };
        RequestQueue rq= Volley.newRequestQueue(Register.this);
        rq.add(sr);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter("registrationComplete"));

        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter("pushNotification"));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }
}
