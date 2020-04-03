package com.growthfile.growthfileNew;

import android.content.Intent;
import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class MyAndroidFirebaseMsgService extends FirebaseMessagingService{
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.e("newToken",token);
        Intent broadCastIntent = new Intent();
        broadCastIntent.setAction(MainActivity.FCM_TOKEN_REFRESH);
        broadCastIntent.putExtra("new_token",token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if(remoteMessage == null) return;
        Log.d("fcm","message taken");
        Intent broadCastIntent = new Intent();
        broadCastIntent.setAction(MainActivity.BROADCAST_ACTION);

        if(remoteMessage.getData().size() > 0) {
                Map<String, String> params = remoteMessage.getData();
                JSONObject object  = new JSONObject(params);
                Log.e("JSON_OBJECT",object.toString());
                broadCastIntent.putExtra("fcmNotificationData",object.toString());
        };
        sendBroadcast(broadCastIntent);

    }

}
