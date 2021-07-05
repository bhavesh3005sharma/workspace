package com.gathering.friends.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.gathering.friends.R;
import com.gathering.friends.database.Prefs;
import com.gathering.friends.databinding.ActivityHomePageBinding;
import com.gathering.friends.fragments.ChatFragment;
import com.gathering.friends.fragments.ConnectionsFragment;
import com.gathering.friends.fragments.WorkspaceFragment;
import com.gathering.friends.util.Constants;
import com.gathering.friends.util.Helper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HomePage extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    ActivityHomePageBinding activityHomePageBinding;
    private final String[] permissions = new String[]{(Manifest.permission.CAMERA), (Manifest.permission.RECORD_AUDIO)};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityHomePageBinding = ActivityHomePageBinding.inflate(getLayoutInflater());
        setContentView(activityHomePageBinding.getRoot());

        activityHomePageBinding.navigation.setOnNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            loadFragment(new WorkspaceFragment());
        }

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
        int requestCode = 10102;
        ActivityCompat.requestPermissions(this, permissions, requestCode);
    }

    private Boolean isPermissionGranted() {

        for (String it : permissions) {
            if (ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.workspace:
                loadFragment(new WorkspaceFragment());
                return true;
            case R.id.chats:
                loadFragment(new ChatFragment());
                return true;
            case R.id.connection_requests:
                loadFragment(new ConnectionsFragment());
                return true;
        }
        return false;
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

}