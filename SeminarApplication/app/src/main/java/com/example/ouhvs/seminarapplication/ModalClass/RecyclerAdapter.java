package com.example.ouhvs.seminarapplication.ModalClass;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.ouhvs.seminarapplication.Activities.ImageCompress;
import com.example.ouhvs.seminarapplication.FCM.Constants;
import com.example.ouhvs.seminarapplication.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by OUHVS on 12-04-2018.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder>{

    Context context;
    ArrayList<UserData> userData;
    String senderName;

    public RecyclerAdapter(Context context,ArrayList<UserData> userData,String name ){
        this.context=context;
        this.userData=userData;
        this.senderName=name;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.contactsdisplay,null);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        holder.name.setText(userData.get(position).getName());
        holder.name.setTypeface(holder.name.getTypeface());
        holder.contactNo.setText(userData.get(position).getMobileno());
        holder.contactNo.setTypeface(holder.contactNo.getTypeface());

        holder.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConnectingToInternet()) {

                    StringRequest sr = new StringRequest(Request.Method.POST, "https://lanetteamvarsha.000webhostapp.com/firebaseApi/index1.php", new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (jsonObject.getInt("success") == 1) {
                                    Toast.makeText(context, "Image sent to " + userData.get(position).getName() + " successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "Could'nt send image ", Toast.LENGTH_SHORT).show();
                                }
                                Log.e("onResponse: ", jsonObject.toString());
                            } catch (JSONException e) {
                                Toast.makeText(context, "exception " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("onErrorResponse: ", "Problem sending notification " + error.getMessage());
                        }
                    }) {
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("title", senderName + " sent you an image.");
                            params.put("message", Constants.S3_URL + ImageCompress.destFile.getName());
                            params.put("regId", userData.get(position).getFcmId());
                            return params;
                        }
                    };

                    RequestQueue rq = Volley.newRequestQueue(context);
                    rq.add(sr);
                }else {
                    Toast.makeText(context, "There's no internet connection. Please turn on internet. ", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return userData.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView name,contactNo;
        Button send;

        public MyViewHolder(View itemView) {
            super(itemView);

            name=itemView.findViewById(R.id.tvName);
            contactNo=itemView.findViewById(R.id.tvContactNo);
            send=itemView.findViewById(R.id.btnSend);
        }
    }

    private boolean isConnectingToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager
                .getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

}
