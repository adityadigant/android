package com.growthfile.growthfileNew;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyAndroidFirebaseMsgService extends FirebaseMessagingService{
        private static final String message = "MyAndroidFCMService";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.e("newToken",token);

    }



    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

    Log.e("FCM_MESSAGE",remoteMessage.getData().toString());

    Map<String, String> dataMap = remoteMessage.getData();



    }







}
