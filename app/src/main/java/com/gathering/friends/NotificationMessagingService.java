package com.gathering.friends;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.gathering.friends.activities.CallActivity;
import com.gathering.friends.activities.HomePage;
import com.gathering.friends.util.Constants;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static android.app.Notification.DEFAULT_SOUND;

public class NotificationMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        createIncomingCallNotification("12111", "photoUri");
    }

    private void createIncomingCallNotification(String callerUsername, String photoUri) {
        RemoteViews customView = new RemoteViews(getPackageName(), R.layout.custom_call_notification);
        Intent notificationIntent = new Intent(this, HomePage.class);
        //Intent hungupIntent = new Intent (this, HungUpBroadcast::class.java)
        Intent answerIntent = new Intent(this, CallActivity.class);
        answerIntent.putExtra("user_type", Constants.CALL_RECEIVER);

        if (callerUsername != null) {
            answerIntent.putExtra("other_user_id", callerUsername);
            customView.setTextViewText(R.id.name, callerUsername);
        } else
            customView.setTextViewText(R.id.name, getResources().getString(R.string.app_name));

//        customView.setImageViewBitmap (R.id.photo, Notification (intent.getStringExtra ("user
//                        _thumbnail_image")))

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        PendingIntent hungupPendingIntent = PendingIntent.getBroadcast(this, 0,
//                hungupIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent answerPendingIntent = PendingIntent.getActivity(this, 0,
                answerIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        customView.setOnClickPendingIntent(R.id.btnAccept,
                answerPendingIntent);
//        customView.setOnClickPendingIntent (R.id.btnDecline,
//                hungupPendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("INCOMING_CALL_WORKSPACE2451",
                    "INCOMING_CALL",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notification Channel for Incoming Calls");
            channel.setSound(null, null);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, "INCOMING_CALL_WORKSPACE2451")
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                                R.drawable.ic_launcher_foreground))
                        .setContentTitle(getResources().getString(R.string.app_name))
                        .setTicker("Call_STATUS")
                        .setContentText("Incoming Call")
                        .setDefaults(Notification.DEFAULT_LIGHTS)
                        .setDefaults(DEFAULT_SOUND)
                        .setCategory(NotificationCompat.CATEGORY_CALL)
                        .setOngoing(true)
                        .setVibrate(null)
                        .setFullScreenIntent(pendingIntent, true)
                        .setSound(defaultSoundUri)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                        .setCustomContentView(customView)
                        .setCustomBigContentView(customView);

        NotificationManagerCompat notificationManagerCompat =
                NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(18242, notificationBuilder.build());

    }

    /*
    public void sendNotification(String messageTitle, String messageBody) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(CHANNEL_DESCRIPTION);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, SplashActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.app_logo)
                        .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                                R.drawable.app_logo))
                        .setContentTitle(messageTitle)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(contentIntent);

        NotificationManagerCompat notificationManagerCompat =
                NotificationManagerCompat.from(this);

        // Since android Oreo notification channel is needed.

        int unique_id = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
        notificationManagerCompat.notify(unique_id, notificationBuilder.build());
        //Log.d(TAG, "sendNotification = run");
    }
     */
}
