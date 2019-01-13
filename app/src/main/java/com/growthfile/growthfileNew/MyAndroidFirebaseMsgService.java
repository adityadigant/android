package com.growthfile.growthfileNew;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Iterator;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class MyAndroidFirebaseMsgService extends FirebaseMessagingService{
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.e("newToken",token);

    }



    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if(remoteMessage == null) return;
        Log.d("fcm","message taken");

        Intent broadCastIntent = new Intent();
        broadCastIntent.setAction(MainActivity.BROADCAST_ACTION);
        broadCastIntent.putExtra("data","abc");
        sendBroadcast(broadCastIntent);

    }

}
