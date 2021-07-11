package com.gathering.friends.activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.gathering.friends.R;
import com.gathering.friends.database.Prefs;
import com.gathering.friends.databinding.ActivityUserAccountBinding;
import com.gathering.friends.models.User;
import com.gathering.friends.util.Helper;
import com.gathering.friends.viewmodels.UserViewModel;

public class UserAccountActivity extends AppCompatActivity implements View.OnClickListener {
    ActivityUserAccountBinding activityUserAccountBinding;
    UserViewModel viewModel;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityUserAccountBinding = ActivityUserAccountBinding.inflate(getLayoutInflater());
        setContentView(activityUserAccountBinding.getRoot());
        viewModel = ViewModelProviders.of(this).get(UserViewModel.class);

        // set toolbar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.account_section));

        // set user from Prefs
        user = Prefs.getUser(this);
        activityUserAccountBinding.setUser(Prefs.getUser(this));

        activityUserAccountBinding.nameEditIcon.setOnClickListener(this);
        activityUserAccountBinding.descriptionEditIcon.setOnClickListener(this);
        activityUserAccountBinding.nameCheckedIcon.setOnClickListener(this);
        activityUserAccountBinding.descriptionCheckedIcon.setOnClickListener(this);

        // listen fo updated from user
        viewModel.getUser(user.getUsername()).observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                if (user != null && user.getUsername() != null) {
                    activityUserAccountBinding.setUser(user);
                    // update Prefs
                    Prefs.setUserData(UserAccountActivity.this, user);
                }
            }
        });

        activityUserAccountBinding.logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helper.signOut(UserAccountActivity.this);
                finish();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.name_edit_icon:
                activityUserAccountBinding.nameEditIcon.setVisibility(View.GONE);
                activityUserAccountBinding.displayNameText.setVisibility(View.GONE);
                activityUserAccountBinding.displayNameEditText.setVisibility(View.VISIBLE);
                activityUserAccountBinding.nameCheckedIcon.setVisibility(View.VISIBLE);
                break;
            case R.id.name_checked_icon:
                String new_text = activityUserAccountBinding.displayNameEditText.getText().toString().trim();
                if (new_text.isEmpty()) {
                    Helper.toast(this, "Name is mandatory");
                    return;
                }
                activityUserAccountBinding.nameEditIcon.setVisibility(View.VISIBLE);
                activityUserAccountBinding.displayNameText.setVisibility(View.VISIBLE);
                activityUserAccountBinding.displayNameEditText.setVisibility(View.GONE);
                activityUserAccountBinding.nameCheckedIcon.setVisibility(View.GONE);
                viewModel.updateDisplayName(new_text, user.getUsername());
                break;
            case R.id.description_edit_icon:
                activityUserAccountBinding.descriptionEditIcon.setVisibility(View.GONE);
                activityUserAccountBinding.descriptionText.setVisibility(View.GONE);
                activityUserAccountBinding.descriptionEditText.setVisibility(View.VISIBLE);
                activityUserAccountBinding.descriptionCheckedIcon.setVisibility(View.VISIBLE);
                break;
            case R.id.description_checked_icon:
                String new_text1 = activityUserAccountBinding.descriptionEditText.getText().toString().trim();
                if (new_text1.isEmpty()) {
                    Helper.toast(this, "Description is mandatory");
                    return;
                }
                activityUserAccountBinding.descriptionEditIcon.setVisibility(View.VISIBLE);
                activityUserAccountBinding.descriptionText.setVisibility(View.VISIBLE);
                activityUserAccountBinding.descriptionEditText.setVisibility(View.GONE);
                activityUserAccountBinding.descriptionCheckedIcon.setVisibility(View.GONE);
                viewModel.updateUserDescription(new_text1, user.getUsername());
                break;

        }
    }
}