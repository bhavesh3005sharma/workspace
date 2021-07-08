package com.gathering.friends.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.gathering.friends.R;
import com.gathering.friends.databinding.ActivityCreateWorkspaceBinding;
import com.gathering.friends.util.Constants;
import com.gathering.friends.util.Helper;
import com.gathering.friends.viewmodels.CreateWorkspaceViewModel;

import java.util.Objects;

public class CreateWorkspaceActivity extends AppCompatActivity {
    ActivityCreateWorkspaceBinding activityCreateWorkspaceBinding;
    CreateWorkspaceViewModel viewModel;

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            checkForButtonEnable();
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityCreateWorkspaceBinding = ActivityCreateWorkspaceBinding.inflate(getLayoutInflater());
        setContentView(activityCreateWorkspaceBinding.getRoot());
        viewModel = ViewModelProviders.of(this).get(CreateWorkspaceViewModel.class);

        // set text watcher on edit texts;
        activityCreateWorkspaceBinding.textInputTitle.getEditText().addTextChangedListener(textWatcher);
        activityCreateWorkspaceBinding.textInputBody.getEditText().addTextChangedListener(textWatcher);

        activityCreateWorkspaceBinding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createWorkspace();
            }
        });

    }

    private void checkForButtonEnable() {
        String name = Objects.requireNonNull(activityCreateWorkspaceBinding.textInputTitle.getEditText()).getText().toString().trim();
        String description = Objects.requireNonNull(activityCreateWorkspaceBinding.textInputBody.getEditText()).getText().toString().trim();

        if (name.isEmpty() || description.isEmpty()) {
            activityCreateWorkspaceBinding.fab.setEnabled(false);
            activityCreateWorkspaceBinding.fab.setBackgroundColor(getColor(R.color.placeholder_bg));
        } else {
            activityCreateWorkspaceBinding.fab.setEnabled(true);
            activityCreateWorkspaceBinding.fab.setBackgroundColor(getColor(R.color.colorPrimary));
        }
    }

    public void createWorkspace() {
        String name = Objects.requireNonNull(activityCreateWorkspaceBinding.textInputTitle.getEditText()).getText().toString().trim();
        String description = Objects.requireNonNull(activityCreateWorkspaceBinding.textInputBody.getEditText()).getText().toString().trim();

        viewModel.createWorkspace(name, description, this).observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (s.equals(Constants.SUCCESS)) {
                    Helper.toast(CreateWorkspaceActivity.this, "Workspace created successfully");
                    Intent intent = new Intent(CreateWorkspaceActivity.this, HomePage.class);
                    startActivity(intent);
                } else {
                    Helper.toast(CreateWorkspaceActivity.this, "Error Occurred. Try Again");
                }
            }
        });
    }
}