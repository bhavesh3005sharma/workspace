package com.gathering.friends.util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.util.Collections;

public class Helper {

    public static void toast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void sendNotificationToUser(final String username, final JSONObject dataJson) {
        Query queryUsers = FirebaseDatabase.getInstance().getReference("users");

        queryUsers.orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String token = "";
                            Log.i("TAG", "onDataChange: token retrieved " + token);
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                if (ds.hasChild("fcm_token"))
                                    token = ds.child("fcm_token").getValue().toString();
                            }
                            sendNotification(token, dataJson);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private static void sendNotification(final String regToken, final JSONObject dataJson) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    OkHttpClient client = new OkHttpClient();
                    client.setProtocols(Collections.singletonList(Protocol.HTTP_1_1));
                    JSONObject json = new JSONObject();
                    json.put("data", dataJson);
                    json.put("to", regToken);
                    RequestBody body = RequestBody.create(Constants.JSON, json.toString());
                    Request request = new Request.Builder()
                            .header("Authorization", "key=" + Constants.LEGACY_SERVER_KEY)
                            .url("https://fcm.googleapis.com/fcm/send")
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    String finalResponse = response.body().string();
                    Log.i("TAG", "doInBackground: " + finalResponse);
                } catch (Exception e) {
                    Log.d("TAG", e + "");
                }
                return null;
            }
        }.execute();

    }
}
