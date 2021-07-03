package com.gathering.friends.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.gathering.friends.Database.Prefs;
import com.gathering.friends.databinding.ActivityHomePageBinding;
import com.gathering.friends.util.Constants;
import com.gathering.friends.util.Helper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HomePage extends AppCompatActivity {

    ActivityHomePageBinding activityHomePageBinding;
    private final String[] permissions = new String[]{(Manifest.permission.CAMERA), (Manifest.permission.RECORD_AUDIO)};
    private final int requestCode = 10102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityHomePageBinding = ActivityHomePageBinding.inflate(getLayoutInflater());
        setContentView(activityHomePageBinding.getRoot());

        activityHomePageBinding.callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isPermissionGranted()) {
                    askPermissions();
                } else {
                    redirectToCall();
                }
            }
        });

        activityHomePageBinding.buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // delete FCM token from server
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
                databaseReference.child(Prefs.getUser(HomePage.this).getUsername()).child("fcm_token").setValue(null);

                // delete all saved data of this user
                Prefs.setUserLoggedIn(HomePage.this, false);
                Prefs.setUserData(HomePage.this, null);

                // sign out from firebase auth
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(HomePage.this, AuthenticationActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void redirectToCall() {
        String friendUserName = activityHomePageBinding.friendNameEdit.getText().toString().trim();
        if (friendUserName.isEmpty()) {
            Helper.toast(HomePage.this, "Provide name");
            return;
        }

        Intent intent = new Intent(HomePage.this, CallActivity.class);
        intent.putExtra("user_type", Constants.CALLER);
        intent.putExtra("other_user_id", friendUserName);
        startActivity(intent);
    }

    private void askPermissions() {
        ActivityCompat.requestPermissions(this, permissions, requestCode);
    }

    private Boolean isPermissionGranted() {

        for (String it : permissions) {
            if (ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }
}