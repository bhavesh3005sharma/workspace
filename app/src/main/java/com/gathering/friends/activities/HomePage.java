package com.gathering.friends.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.gathering.friends.models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomePage extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    ActivityHomePageBinding activityHomePageBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityHomePageBinding = ActivityHomePageBinding.inflate(getLayoutInflater());
        setContentView(activityHomePageBinding.getRoot());

        activityHomePageBinding.navigation.setOnNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            loadFragment(new WorkspaceFragment());
        }

        User user = Prefs.getUser(this);
        // setup UI
        activityHomePageBinding.toolbar.setTitle("Workspace");
        activityHomePageBinding.toolbar.setInitial(String.valueOf(user.getUsername().charAt(0)));
        activityHomePageBinding.toolbar.setPhotoUri(user.getProfileUri());
        if (user.getProfileUri() == null || user.getProfileUri().isEmpty())
            activityHomePageBinding.toolbar.cardPhoto.setVisibility(View.GONE);
        else
            activityHomePageBinding.toolbar.cardPhoto.setVisibility(View.VISIBLE);

        activityHomePageBinding.toolbar.cardViewPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("TAG", "onClick: ");
                startActivity(new Intent(HomePage.this, UserAccountActivity.class));
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.workspace:
                activityHomePageBinding.toolbar.setTitle("Workspace");
                loadFragment(new WorkspaceFragment());
                return true;
            case R.id.chats:
                activityHomePageBinding.toolbar.setTitle("Chats");
                loadFragment(new ChatFragment());
                return true;
            case R.id.connection_requests:
                activityHomePageBinding.toolbar.setTitle("Connections");
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