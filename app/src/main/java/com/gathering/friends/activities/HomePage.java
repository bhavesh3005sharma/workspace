package com.gathering.friends.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.gathering.friends.Database.Prefs;
import com.gathering.friends.databinding.ActivityHomePageBinding;
import com.gathering.friends.util.Constants;
import com.gathering.friends.util.Helper;
import com.google.firebase.auth.FirebaseAuth;

public class HomePage extends AppCompatActivity {

    ActivityHomePageBinding activityHomePageBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityHomePageBinding = ActivityHomePageBinding.inflate(getLayoutInflater());
        setContentView(activityHomePageBinding.getRoot());

        activityHomePageBinding.callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String friendUserName = activityHomePageBinding.friendNameEdit.getText().toString().trim();
                if (friendUserName == null || friendUserName.isEmpty()) {
                    Helper.toast(HomePage.this, "Provide name");
                    return;
                }

                Intent intent = new Intent(HomePage.this, CallActivity.class);
                intent.putExtra("user_type", Constants.CALLER);
                intent.putExtra("other_user_id", friendUserName);
                startActivity(intent);
            }
        });

        activityHomePageBinding.buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Prefs.setUserLoggedIn(HomePage.this, false);
                Prefs.setUserData(HomePage.this, null);

                Intent intent = new Intent(HomePage.this, AuthenticationActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });


    }
}