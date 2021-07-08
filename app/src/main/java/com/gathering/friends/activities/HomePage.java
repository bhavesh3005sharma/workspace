package com.gathering.friends.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.gathering.friends.R;
import com.gathering.friends.database.Prefs;
import com.gathering.friends.databinding.ActivityHomePageBinding;
import com.gathering.friends.fragments.ChatFragment;
import com.gathering.friends.fragments.ConnectionsFragment;
import com.gathering.friends.fragments.WorkspaceFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class HomePage extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    ActivityHomePageBinding activityHomePageBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityHomePageBinding = ActivityHomePageBinding.inflate(getLayoutInflater());
        setContentView(activityHomePageBinding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).setTitle("Workspace");

        activityHomePageBinding.navigation.setOnNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            loadFragment(new WorkspaceFragment());
        }

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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.workspace:
                Objects.requireNonNull(getSupportActionBar()).setTitle("Workspace");
                loadFragment(new WorkspaceFragment());
                return true;
            case R.id.chats:
                Objects.requireNonNull(getSupportActionBar()).setTitle("Chats");
                loadFragment(new ChatFragment());
                return true;
            case R.id.connection_requests:
                Objects.requireNonNull(getSupportActionBar()).setTitle("Connections");
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