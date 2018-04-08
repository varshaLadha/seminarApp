package com.example.ouhvs.seminarapplication.FCM;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.example.ouhvs.seminarapplication.Activities.Home;
import com.example.ouhvs.seminarapplication.Activities.MainActivity;
import com.example.ouhvs.seminarapplication.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private NotificationUtils notificationUtils;
    NotificationManager notifManager;
    private static final String ADMIN_CHANNEL_ID ="admin_channel";
    final int icon = R.mipmap.ic_launcher;


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Data: " + remoteMessage.getData());

        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            try {
                if(remoteMessage.getData().size()>0) {
                    setupDataChannels(new JSONObject(remoteMessage.getData().toString()));
                }
                else {
                    setupChannels(remoteMessage.getNotification().getBody(),new JSONObject(remoteMessage.getData().toString()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else{
            if(remoteMessage.getNotification()!=null){
                Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
                String message=remoteMessage.getNotification().getBody();
                handleNotification(message);
            }

            if (remoteMessage.getData().size() > 0) {
                Log.d(TAG, "Message data payload: " + remoteMessage.getData());

                try {
                    JSONObject json = new JSONObject(remoteMessage.getData().toString());
                    handleDataMessage(json);
                } catch (Exception e) {
                    Log.e(TAG, "Exception: " + e.getMessage());
                }
            }
        }
    }

    private void handleNotification(String message) {
        Log.d("handel notification","Handel notification called");
        if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
            // app is in foreground, broadcast the push message
            Intent pushNotification = new Intent("pushNotification");
            pushNotification.putExtra("message", message);
            pushNotification.putExtra("foreground","true");
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

            // play notification sound
            NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
            notificationUtils.playNotificationSound();

        }else{
            Intent pushNotification = new Intent("pushNotification");
            pushNotification.putExtra("message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

            NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
            notificationUtils.playNotificationSound();
            showNotificationMessage(getApplicationContext(), "New notification", message, "time", pushNotification);
        }
    }

    private void handleDataMessage(JSONObject json) {
        Log.d("handel datanotification","Handel data notification called");
        Log.e(TAG, "push json: " + json.toString());

        try {
            JSONObject data = json.getJSONObject("data");

            String title = data.getString("title");
            String message = data.getString("message");
            boolean isBackground = data.getBoolean("is_background");
            String imageUrl = data.getString("image");
            String timestamp = data.getString("timestamp");
            JSONObject payload = data.getJSONObject("payload");

            Log.e(TAG, "title: " + title);
            Log.e(TAG, "message: " + message);
            Log.e(TAG, "isBackground: " + isBackground);
            Log.e(TAG, "payload: " + payload.toString());
            Log.e(TAG, "imageUrl: " + imageUrl);
            Log.e(TAG, "timestamp: " + timestamp);

            String msgData= message;

            if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
                // app is in foreground, broadcast the push message
                Intent pushNotification = new Intent("pushNotification");
                pushNotification.putExtra("message", msgData);
                pushNotification.putExtra("title",title);
                pushNotification.putExtra("foreground","true");
                LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

                // play notification sound
                NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
                notificationUtils.playNotificationSound();
                //showNotificationMessage(getApplicationContext(), "New notification", message, "time", pushNotification);

            } else {
                // app is in background, show the notification in notification tray
                Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
                resultIntent.putExtra("message", msgData);
                resultIntent.putExtra("title",title);
                // check for image attachment
                if (TextUtils.isEmpty(imageUrl)) {
                    showNotificationMessage(getApplicationContext(), title, message, timestamp, resultIntent);
                } else {
                    // image is present, show notification with image
                    showNotificationMessageWithBigImage(getApplicationContext(), title, message, timestamp, resultIntent, imageUrl);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Json Exception: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent);
    }

    private void showNotificationMessageWithBigImage(Context context, String title, String message, String timeStamp, Intent intent, String imageUrl) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent, imageUrl);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void notify(int id, Notification.Builder notification) {
        getManager().notify(id, notification.build());
    }

    private NotificationManager getManager() {
        if (notifManager == null) {
            notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return notifManager;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupDataChannels(JSONObject obj) throws JSONException {
        Log.d("setupdatachannel","set up data channel called");
        JSONObject jobj = obj.getJSONObject("data");
        final Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                + "://" + getApplicationContext().getPackageName() + "/raw/notification");
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

        CharSequence adminChannelName = getString(R.string.default_notification_channel_name);
        String adminChannelDescription = getString(R.string.default_notification_channel_description);

        NotificationChannel adminChannel;
        adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_LOW);
        adminChannel.setDescription(adminChannelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.RED);
        adminChannel.enableVibration(true);
        if (notifManager != null) {
            notifManager.createNotificationChannel(adminChannel);
        }

        if(!NotificationUtils.isAppIsInBackground(getApplicationContext())){
            Log.d("setupdatachannel","set up data channel called foreground");
            try {
                Intent pushNotification = new Intent("pushNotification");
                pushNotification.putExtra("message", jobj.getString("message"));
                pushNotification.putExtra("title",jobj.getString("title"));
                pushNotification.putExtra("foreground", "true");
                Log.d("data",jobj.getString("message")+" "+jobj.getString("title"));
                LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        else {
            Log.d("setupdatachannel","set up data channel called background");
            try {
                Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
                resultIntent.putExtra("message", jobj.getString("message"));
                resultIntent.putExtra("title",jobj.getString("title"));
                resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                final PendingIntent resultPendingIntent =
                        PendingIntent.getActivity(
                                getApplicationContext(),
                                0,
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );

                NotificationCompat.Builder notificationBuilder =
                        new NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setStyle(inboxStyle)
                                .setWhen(getTimeMilliSec(jobj.getString("timestamp")))
                                .setContentTitle(jobj.getString("title"))
                                .setContentText(jobj.getString("message"))
                                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), icon))
                                .setContentIntent(resultPendingIntent)
                                .setAutoCancel(true);

                notifManager=getManager();
                notifManager.notify(100, notificationBuilder.build());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels(String message,JSONObject data) throws JSONException {
        Log.d("setupchannel","set up channel called");
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
            Log.d("setupchannel","set up channel called foreground");
            Intent pushNotification = new Intent("pushNotification");
            pushNotification.putExtra("message", message);
            pushNotification.putExtra("foreground", "true");
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
        }
        else {
            Log.d("setupchannel","set up channel called background");
            try {
                Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
                resultIntent.putExtra("message", data.getString("message"));

                resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                final PendingIntent resultPendingIntent =
                        PendingIntent.getActivity(
                                getApplicationContext(),
                                0,
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );

                NotificationCompat.Builder notificationBuilder =
                        new NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setStyle(inboxStyle)
                                .setWhen(getTimeMilliSec(data.getString("timestamp")))
                                .setContentTitle(data.getString("title"))
                                .setContentText(data.getString("message"))
                                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), icon))
                                .setContentIntent(resultPendingIntent)
                                .setAutoCancel(true);

                notifManager.notify(100, notificationBuilder.build());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static long getTimeMilliSec(String timeStamp) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = format.parse(timeStamp);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
