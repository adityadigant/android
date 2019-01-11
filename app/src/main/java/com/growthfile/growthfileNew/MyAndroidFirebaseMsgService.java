package com.growthfile.growthfileNew;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyAndroidFirebaseMsgService extends FirebaseMessagingService{
        private static final String message = "MyAndroidFCMService";

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.d("token",  s);
        Intent intent = new Intent(this,MainActivity. class);
        intent.putExtra("token",s);

        startActivity(intent);
    }



    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Log data to Log Cat

        Log.d(message, "From: " + remoteMessage.getFrom());
        Log.d(message, "Notification Message Body: " + remoteMessage.getNotification().getBody());

        //create notification
        createNotification(remoteMessage.getNotification().getBody());
    }




        private void createNotification( String messageBody) {
            Intent intent = new Intent( this , MainActivity. class );
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent resultIntent = PendingIntent.getActivity( this , 0, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            Uri notificationSoundURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder( this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Android Tutorial Point FCM Tutorial")
                    .setContentText(messageBody)
                    .setAutoCancel( true )
                    .setSound(notificationSoundURI)
                    .setContentIntent(resultIntent);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(0, mNotificationBuilder.build());
        }
}
