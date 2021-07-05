package com.gathering.friends.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.gathering.friends.R;
import com.gathering.friends.activities.HomePage;
import com.gathering.friends.database.Prefs;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Date;
import java.util.Map;

public class NotificationMessagingService extends FirebaseMessagingService {

    private static final String TAG = "NotificationMessagingService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();

        if (data.containsKey("isCallingNotification")) {
            // start the call service and show incoming call notification
            String callerUsername = data.get("caller_user_name");
            String photoUri = data.get("photoUri");
            Intent intent = new Intent(this, CallService.class);
            intent.putExtra("callerUsername", callerUsername);
            intent.putExtra("photoUri", photoUri);
            Log.i(TAG, "onMessageReceived: " + data);
            startService(intent);
        }

    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d("FCM_TOKEN", token);
        saveTokenToServer(this, token,
                (Prefs.getUser(this) != null) ?
                        Prefs.getUser(this).getUsername() : null);
        super.onNewToken(token);
    }

    private void saveTokenToServer(Context context, String token, String username) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        databaseReference.child(username).child("fcm_token").setValue(token).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // successfully saved
                    Prefs.getUser(context).setFcm_token(token);
                } else {
                    // error
                }
            }
        });
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

}
