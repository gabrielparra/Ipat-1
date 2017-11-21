package com.gcatech.ipat;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.Map;

import domain.PnsNotification;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage);
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            sendPayloadNotification(remoteMessage.getData());
        }
    }

    // [END receive_message]
    private void sendPayloadNotification(Map<String, String> data) {

        Gson gson = new Gson();
        if (data != null) {
            String notification = data.get("notification");

            if (notification != null && !notification.equals("")) {
                PnsNotification pnsNotification = gson.fromJson(notification, PnsNotification.class);
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("pnsNotification", true);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);


                intent.setAction(Long.toString(System.currentTimeMillis()));
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                builder.setContentTitle(pnsNotification.getTitle());
                builder.setContentText(pnsNotification.getBody());
                builder.setColor(Color.argb(0, 0, 63, 94));
                builder.setSmallIcon(R.drawable.ic_launcher);
                builder.setAutoCancel(true);
                builder.setContentIntent(pendingIntent);
                Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                builder.setSound(sound);
             /*   Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                if (pnsNotification.getSound() != null) {
                    Uri tempSound = Uri.parse(pnsNotification.getSound());
                    Ringtone ringtone = RingtoneManager.getRingtone(applicationContext, tempSound);
                    if (ringtone != null) {
                        sound = tempSound;
                    }
                }

                builder.setSound(sound);*/
                NotificationManagerCompat.from(this).notify(pnsNotification.getId(), builder.build());
            }
        }
    }
}
