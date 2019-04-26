package abhiandroid.com.ultimatewebview.FCM;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import abhiandroid.com.ultimatewebview.MainActivity;
import abhiandroid.com.ultimatewebview.R;
import abhiandroid.com.ultimatewebview.SplashScreen;


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    NotificationManager notificationManager;
    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();
    public static final String ANDROID_CHANNEL_ID = " abhiandroid.com.ultimatewebview";
    public static final String ANDROID_CHANNEL_NAME = "ANDROID CHANNEL";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e(TAG, "From: " + remoteMessage.getFrom());
        notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (remoteMessage == null)
            return;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            createChannels();

            notificationManager.notify(0  ,  getAndroidChannelNotification(remoteMessage).build());
        }else{

            createNotification(remoteMessage);
        }

    }

    private void createNotification(RemoteMessage remoteMessage) {
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Data Payload: " + remoteMessage.getData().toString());

            try {
                JSONObject json = new JSONObject(remoteMessage.getData().toString());
                Log.d("url ll", json.get("url") + "");
                handleDataMessage(remoteMessage.getNotification().getTitle(),remoteMessage.getNotification().getBody(), json);
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        } else if (remoteMessage.getNotification() != null) {
            Log.e(TAG, "Notification Body: " + remoteMessage.getNotification().getBody());
            handleNotification(remoteMessage.getNotification().getTitle(),remoteMessage.getNotification().getBody());
        }
    }

    ///// ////////////////////////////////
    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getAndroidChannelNotification(RemoteMessage remoteMessage) {
        Intent resultIntent = null;
        String message,title;
        message=remoteMessage.getNotification().getBody();
        title=remoteMessage.getNotification().getTitle();

        if (remoteMessage.getData().size() > 0) {

            try {
                JSONObject json = new JSONObject(remoteMessage.getData().toString());
                String url = json.get("url").toString();
                resultIntent = new Intent(getApplicationContext(), SplashScreen.class);

                resultIntent.putExtra("message", message);
                resultIntent.putExtra("url", url);
                //handleDataMessage(remoteMessage.getNotification().getBody(), json);
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        } else if (remoteMessage.getNotification() != null) {
            resultIntent = new Intent(getApplicationContext(), SplashScreen.class);

            resultIntent.putExtra("message", message);
            resultIntent.putExtra("url", "");
            // handleNotification(remoteMessage.getNotification().getBody());
        }

        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0,   resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);



        return new Notification.Builder(getApplicationContext(), ANDROID_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.app_icon)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentIntent(contentIntent)
                ;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createChannels() {

        // create android channel
        NotificationChannel androidChannel = new NotificationChannel(ANDROID_CHANNEL_ID,
                ANDROID_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        // Sets whether notifications posted to this channel should display notification lights
        androidChannel.enableLights(true);
        // Sets whether notification posted to this channel should vibrate.
        androidChannel.enableVibration(true);
        // Sets the notification light color for notifications posted to this channel
        androidChannel.setLightColor(Color.GREEN);
        // Sets whether notifications posted to this channel appear on the lockscreen or not
        androidChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        notificationManager.createNotificationChannel(androidChannel);
    }

    ///////////////////////////////////////////////////////////

    private void handleNotification(String title,String message) {
        Intent resultIntent = new Intent(getApplicationContext(), SplashScreen.class);
        resultIntent.putExtra("message", message);
        resultIntent.putExtra("url", "");

        // check for image attachment
        sendNotification("",title, message, resultIntent);
    }

    private void handleDataMessage(String title,String message, JSONObject json) {
        Log.e(TAG, "push json: " + json.toString());

        try {
            String url = json.get("url").toString();
            Intent resultIntent = new Intent(getApplicationContext(), SplashScreen.class);
            resultIntent.putExtra("message", message);
            resultIntent.putExtra("url", url);

            // check for image attachment
            sendNotification(url,title, message, resultIntent);

        } catch (JSONException e) {
            Log.e(TAG, "Json Exception: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }


    private void sendNotification(String url, String title,String msg,Intent resultIntent) {
        int requestID = (int) System.currentTimeMillis();


        PendingIntent intent =
                PendingIntent.getActivity(getApplicationContext(), requestID, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mNotifyBuilder;
        NotificationManager mNotificationManager;

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
            mNotifyBuilder = new NotificationCompat.Builder(this)
                    .setContentTitle(title)
                    .setContentText(msg)
                    .setPriority(1);
            mNotifyBuilder.setSmallIcon(R.drawable.app_icon);

        } else {
            // Lollipop specific setColor method goes here.
            mNotifyBuilder = new NotificationCompat.Builder(this)
                    .setContentTitle(title)
                    .setContentText(msg)
                    .setColor(Color.WHITE)
                    .setPriority(1);
            mNotifyBuilder.setSmallIcon(R.drawable.app_icon);
            mNotifyBuilder.setLargeIcon(((BitmapDrawable) getResources().getDrawable(R.drawable.app_icon)).getBitmap());

        }


        // Set pending intent

        // Set Vibrate, Sound and Light
        int defaults = 0;
        defaults = defaults | Notification.DEFAULT_LIGHTS;
        defaults = defaults | Notification.DEFAULT_VIBRATE;
        defaults = defaults | Notification.DEFAULT_SOUND;

        mNotifyBuilder.setDefaults(defaults);
        // Set autocancel
        mNotifyBuilder.setAutoCancel(true);
        mNotifyBuilder.setContentIntent(intent);
        // Post a notification
        mNotificationManager.notify(requestID, mNotifyBuilder.build());
    }
}