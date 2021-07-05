package com.gathering.friends.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.gathering.friends.database.Prefs;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HungUpBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // stop call service and remove ongoing call notification
        context.stopService(new Intent(context, CallService.class));

        // reject the call and notify the user by changing key at firebase db
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("users").child(Prefs.getUser(context).getUsername()).child("connectionId").setValue(Constants.REJECTED);
    }
}
