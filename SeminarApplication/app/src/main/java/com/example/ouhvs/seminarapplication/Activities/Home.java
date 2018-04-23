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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.ouhvs.seminarapplication.FCM.Constants;
import com.example.ouhvs.seminarapplication.ModalClass.UserData;
import com.example.ouhvs.seminarapplication.R;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Home extends BaseClass {

    Gson gson;
    String object,firebaseId,title,message;
    UserData userData;
    TextView tvGreeting;
    ImageView ivReceivedImage;
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if(!GlobalClass.pref.contains("userDetail")){
            Intent intent=new Intent(Home.this,MainActivity.class);
            startActivity(intent);
            finish();
        }else {
            initViews();

            onNewIntent(getIntent());

            if(Constants.title!=null && Constants.message!=null)
            {
                Toast.makeText(this, Constants.title, Toast.LENGTH_SHORT).show();
                Picasso.with(Home.this)
                        .load(Constants.message)
                        .into(ivReceivedImage);
                ivReceivedImage.setVisibility(View.VISIBLE);
                //MainActivity.title=null;
                //MainActivity.message=null;
            }

            mRegistrationBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    if (intent.getAction().equals("registrationComplete")) {
                        FirebaseMessaging.getInstance().subscribeToTopic("global");

                    } else if (intent.getAction().equals("pushNotification")) {

                        Bundle extras = intent.getExtras();
                        if (extras != null) {
                            String str = extras.getString("foreground");

                            if (str != null) {

                                Constants.title=intent.getStringExtra("title");
                                Constants.message=intent.getStringExtra("message");
                                Toast.makeText(getApplicationContext(), Constants.title, Toast.LENGTH_LONG).show();

                                Picasso.with(Home.this)
                                        .load(Constants.message)
                                        .into(ivReceivedImage);
                                ivReceivedImage.setVisibility(View.VISIBLE);
                            }
                        } else {

                            Constants.title=intent.getStringExtra("title");
                            Constants.message = intent.getStringExtra("message");
                            Picasso.with(Home.this)
                                    .load(Constants.message)
                                    .into(ivReceivedImage);
                            ivReceivedImage.setVisibility(View.VISIBLE);
                            Toast.makeText(getApplicationContext(), Constants.title, Toast.LENGTH_LONG).show();
                        }
                    }
                }
            };
        }
    }

    public void initViews()
    {
        firebaseId= FirebaseInstanceId.getInstance().getToken();
        tvGreeting=(TextView)findViewById(R.id.tv_greeting);
        ivReceivedImage=(ImageView)findViewById(R.id.ivReceivedImage);

        if(!GlobalClass.pref.contains("userDetail")){
            Intent intent=new Intent(Home.this,MainActivity.class);
            startActivity(intent);
            finish();
        }else {
            gson=new Gson();
            object=GlobalClass.pref.getString("userDetail","");
            userData=gson.fromJson(object,UserData.class);
            tvGreeting.setText("Welcome "+userData.getName());

            if (!userData.getFcmId().equals(firebaseId)) {

                StringRequest sr=new StringRequest(Request.Method.POST, "https://lanetteamvarsha.000webhostapp.com/seminarApi/fcmIdUpdate.php", new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject=new JSONObject(response);
                            if(jsonObject.getInt("success")==1){

                                UserData userData1=new UserData(userData.getName(),userData.getPassword(),userData.getMobileno(),firebaseId);
                                Gson gson=new Gson();
                                String object1=gson.toJson(userData1);
                                GlobalClass.editor.putString("userDetail",object1);
                                GlobalClass.editor.commit();
                                initViews();
                            }
                            else {
                                Log.e("Error",jsonObject.getString("message"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error",error.getMessage());
                    }
                }){
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String,String> params=new HashMap<String, String>();
                        params.put("fcmId",firebaseId);
                        params.put("mobileno",userData.getMobileno());
                        return params;
                    }
                };

                RequestQueue rq= Volley.newRequestQueue(this);
                rq.add(sr);
            }

            Log.d("User data",userData.getName()+" "+userData.getMobileno()+" "+userData.getPassword()+" "+userData.getFcmId());
            Log.d("fcm id",firebaseId+"");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle bundle=intent.getExtras();

        if(bundle!=null){
            Constants.title=bundle.getString("title");
            Constants.message=bundle.getString("message");

            Log.d("Notification content",Constants.title+" "+Constants.message);
            if(bundle.containsKey("message")){
                Toast.makeText(getApplicationContext(),Constants.title, Toast.LENGTH_LONG).show();
            }else if(bundle.containsKey("data")){
                Toast.makeText(getApplicationContext(), "Push notification: from new intent  HOME dattapayload " + bundle.getString("data payload"), Toast.LENGTH_LONG).show();
            }

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
