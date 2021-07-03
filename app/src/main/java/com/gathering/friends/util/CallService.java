package com.gathering.friends.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.gathering.friends.R;
import com.gathering.friends.activities.CallActivity;
import com.gathering.friends.activities.HomePage;

import java.util.Date;

import static android.app.Notification.DEFAULT_SOUND;

public class CallService extends Service {

    CountDownTimer timer;
    MediaPlayer player;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String callerUsername = intent.getStringExtra("callerUsername");
        String photoUri = intent.getStringExtra("photoUri");

        //set up a countdown timer to stop the service automatically after 30 seconds
        timer = new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                // Person has not responded the call
                Log.i("TAG", "onFinish: missed call detected");
                createMissedCallNotification(callerUsername);

                stopService(new Intent(CallService.this, CallService.class));
            }
        }.start();

        RemoteViews customView = new RemoteViews(getPackageName(), R.layout.custom_call_notification);
        RemoteViews customViewExpanded = new RemoteViews(getPackageName(), R.layout.custom_call_notification_expanded);

        Intent notificationIntent = new Intent(this, CallActivity.class);
        notificationIntent.putExtra("user_type", Constants.CALL_RECEIVER);

        Intent hungupIntent = new Intent(this, HungUpBroadcast.class);

        Intent answerIntent = new Intent(this, CallActivity.class);
        answerIntent.putExtra("user_type", Constants.CALL_RECEIVER);
        answerIntent.putExtra("call_status", Constants.CALL_PICKED);

        if (callerUsername != null) {
            answerIntent.putExtra("other_user_id", callerUsername);
            customView.setTextViewText(R.id.name, callerUsername);
            customViewExpanded.setTextViewText(R.id.name, callerUsername);
        } else {
            customView.setTextViewText(R.id.name, getResources().getString(R.string.app_name));
            customViewExpanded.setTextViewText(R.id.name, getResources().getString(R.string.app_name));
        }

        if (photoUri != null && !photoUri.isEmpty()) {
            customView.setImageViewUri(R.id.photo, Uri.parse(photoUri));
            customViewExpanded.setImageViewUri(R.id.photo, Uri.parse(photoUri));
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent hungupPendingIntent = PendingIntent.getBroadcast(this, 0,
                hungupIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent answerPendingIntent = PendingIntent.getActivity(this, 0,
                answerIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        customViewExpanded.setOnClickPendingIntent(R.id.btnAccept,
                answerPendingIntent);
        customViewExpanded.setOnClickPendingIntent(R.id.btnDecline,
                hungupPendingIntent);

        sendNotification(pendingIntent, customView, customViewExpanded);
        return super.onStartCommand(intent, flags, startId);
    }

    private void createMissedCallNotification(String callerUsername) {
        sendNotification("Missed Call", callerUsername + " called you.");
    }

    public void sendNotification(String messageTitle, String messageBody) {

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(Constants.WORKSPACE_CHANNEL_ID,
                    Constants.WORKSPACE_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(Constants.WORKSPACE_CHANNEL_DESCRIPTION);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, HomePage.class), PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, Constants.WORKSPACE_CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
//                        .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
//                                R.drawable.app_logo))
                        .setContentTitle(messageTitle)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(contentIntent);

        NotificationManagerCompat notificationManagerCompat =
                NotificationManagerCompat.from(this);

        int unique_id = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
        notificationManagerCompat.notify(unique_id, notificationBuilder.build());
    }

    private void sendNotification(PendingIntent pendingIntent, RemoteViews customView, RemoteViews customViewExpanded) {
        Log.i("TAG", "sendNotification: ");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(Constants.WORKSPACE_CHANNEL_ID,
                    Constants.WORKSPACE_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(Constants.WORKSPACE_CHANNEL_DESCRIPTION);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, Constants.WORKSPACE_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_launcher_background)
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
                        .setCustomBigContentView(customViewExpanded);

        int NOTIFICATION_ID = 3005;
        startForeground(NOTIFICATION_ID, notificationBuilder.build());

        // play the default ringtone to attract user attention
        player = MediaPlayer.create(this,
                Settings.System.DEFAULT_RINGTONE_URI);
        player.start();
        Log.i("TAG", "sendNotification: player");
    }

    @Override
    public void onDestroy() {
        // flush the notification and stop service
        stopForeground(true);
        stopSelf();

        // stop playing ringtone
        if (player != null) player.stop();

        // stop countdown timer to reject the call
        if (timer != null) timer.cancel();

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
