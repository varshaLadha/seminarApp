package com.example.ouhvs.seminarapplication.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.example.ouhvs.seminarapplication.FCM.NotificationUtils;
import com.example.ouhvs.seminarapplication.ModalClass.UserData;
import com.example.ouhvs.seminarapplication.R;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {
    EditText mobileno,password;
    Button login;
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
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
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });
    }

    public void initViews(){
        mobileno=(EditText)findViewById(R.id.mobileno);
        password=(EditText)findViewById(R.id.password);
        login=(Button)findViewById(R.id.login);
    }

    public void loginUser()
    {
        final String mno,pwd;
        mno=mobileno.getText().toString().trim();
        pwd=password.getText().toString().trim();

        if(TextUtils.isEmpty(mno) || TextUtils.isEmpty(pwd)){
            Toast.makeText(this, "Please enter the detail", Toast.LENGTH_SHORT).show();
        }else {
            StringRequest sr=new StringRequest(Request.Method.POST, "https://lanetteamvarsha.000webhostapp.com/seminarApi/login.php", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject=new JSONObject(response);
                        if(jsonObject.getInt("success")==1){
                            UserData userData=new UserData(jsonObject.getString("username"),pwd,mno,jsonObject.getString("fcmId"));
                            Gson gson=new Gson();
                            String object=gson.toJson(userData);
                            GlobalClass.editor.putString("userDetail",object);
                            GlobalClass.editor.commit();
                            Intent intent=new Intent(Login.this,Home.class);
                            startActivity(intent);
                            finish();
                        }else {
                            Toast.makeText(Login.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(Login.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(Login.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> params=new HashMap<String, String>();
                    params.put("mobileno",mno);
                    params.put("password",pwd);
                    return params;
                }
            };
            RequestQueue rq= Volley.newRequestQueue(Login.this);
            rq.add(sr);
        }
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
