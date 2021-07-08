package com.gathering.friends.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.gathering.friends.R;
import com.gathering.friends.database.Prefs;
import com.gathering.friends.databinding.ActivityAuthenticationBinding;
import com.gathering.friends.models.LoginRequest;
import com.gathering.friends.models.User;
import com.gathering.friends.util.Constants;
import com.gathering.friends.util.Helper;
import com.gathering.friends.viewmodels.AuthViewModel;

public class AuthenticationActivity extends AppCompatActivity implements View.OnClickListener {
    ActivityAuthenticationBinding activityAuthenticationBinding;
    AuthViewModel authViewModel;
    private boolean wantLogin = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityAuthenticationBinding = ActivityAuthenticationBinding.inflate(getLayoutInflater());
        setContentView(activityAuthenticationBinding.getRoot());
        authViewModel = ViewModelProviders.of(this).get(AuthViewModel.class);

        // setUp click Listeners to buttons
        activityAuthenticationBinding.btnSignIn.setOnClickListener(this);
        activityAuthenticationBinding.btnSignUp.setOnClickListener(this);
        activityAuthenticationBinding.inputLayout.btnSubmit.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        if (Prefs.isUserLoggedIn(this)) {
            Intent intent = new Intent(AuthenticationActivity.this, HomePage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
        super.onStart();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSignIn:
                if (!wantLogin) {
                    wantLogin = true;
                    setActive(activityAuthenticationBinding.btnSignIn);
                    setInActive(activityAuthenticationBinding.btnSignUp);
                    activityAuthenticationBinding.inputLayout.btnSubmit.setText("Sign-In");
                    activityAuthenticationBinding.inputLayout.textForgotPassword.setVisibility(View.VISIBLE);
                    activityAuthenticationBinding.inputLayout.textInputUsername.setVisibility(View.GONE);
                    activityAuthenticationBinding.inputLayout.textInputDisplayName.setVisibility(View.GONE);
                }
                break;
            case R.id.btnSignUp:
                if (wantLogin) {
                    wantLogin = false;
                    setInActive(activityAuthenticationBinding.btnSignIn);
                    setActive(activityAuthenticationBinding.btnSignUp);
                    activityAuthenticationBinding.inputLayout.btnSubmit.setText("Sign-Up");
                    activityAuthenticationBinding.inputLayout.textForgotPassword.setVisibility(View.GONE);
                    activityAuthenticationBinding.inputLayout.textInputUsername.setVisibility(View.VISIBLE);
                    activityAuthenticationBinding.inputLayout.textInputDisplayName.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.btnSubmit:
                String email = activityAuthenticationBinding.inputLayout.textInputEmail.getEditText().getText().toString();
                String password = activityAuthenticationBinding.inputLayout.textInputPassword.getEditText().getText().toString();
                String username = activityAuthenticationBinding.inputLayout.textInputUsername.getEditText().getText().toString();
                String displayName = activityAuthenticationBinding.inputLayout.textInputDisplayName.getEditText().getText().toString();

                LoginRequest loginRequest = new LoginRequest(email, password);
                if (wantLogin) {
                    if (!valid(email, password)) return;
                    showProgress();

                    authViewModel.isCorrectUser(AuthenticationActivity.this, loginRequest).observe(this, new Observer<String>() {
                        @Override
                        public void onChanged(String s) {
                            hideProgress();

                            if (s != null && s.equals(Constants.SUCCESS)) {
                                s = "Login Success!";
                                Intent intent = new Intent(AuthenticationActivity.this, HomePage.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                            Helper.toast(AuthenticationActivity.this, s);
                        }
                    });
                } else {
                    if (!valid(email, password, username, displayName)) return;
                    showProgress();

                    User user = new User(username, email, displayName, null, null, null);
                    authViewModel.registerUser(user, password).observe(this, new Observer<String>() {
                        @Override
                        public void onChanged(String s) {
                            hideProgress();

                            if (s != null && s.equals(Constants.SUCCESS)) {
                                s = "Registered Successfully\n Verification Email Sent, Verify to Login";
                            }

                            if (s != null && s.equals(Constants.DUPLICATE)) {
                                s = "This username is already in use. Please choose another one";
                            }

                            Helper.toast(AuthenticationActivity.this, s);
                        }
                    });
                }

                break;
        }
    }

    private boolean valid(String email, String password) {
        if (email.isEmpty()) {
            activityAuthenticationBinding.inputLayout.textInputEmail.setError("Email is Required");
            activityAuthenticationBinding.inputLayout.textInputEmail.requestFocus();
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            activityAuthenticationBinding.inputLayout.textInputEmail.setError("Please Enter The Valid Email.");
            activityAuthenticationBinding.inputLayout.textInputEmail.requestFocus();
            return false;
        } else activityAuthenticationBinding.inputLayout.textInputEmail.setError(null);

        if (password.isEmpty() || password.length() < 6) {
            activityAuthenticationBinding.inputLayout.textInputPassword.setError("At least 6 Character Password is Required.");
            activityAuthenticationBinding.inputLayout.textInputPassword.requestFocus();
            return false;
        } else activityAuthenticationBinding.inputLayout.textInputPassword.setError(null);

        return true;
    }

    private boolean valid(String email, String password, String username, String displayName) {
        if (!valid(email, password)) return false;

        if (username.isEmpty()) {
            activityAuthenticationBinding.inputLayout.textInputUsername.setError("Username is Required");
            activityAuthenticationBinding.inputLayout.textInputUsername.requestFocus();
            return false;
        } else activityAuthenticationBinding.inputLayout.textInputUsername.setError(null);

        if (displayName.isEmpty()) {
            activityAuthenticationBinding.inputLayout.textInputDisplayName.setError("Display Name is Required");
            activityAuthenticationBinding.inputLayout.textInputDisplayName.requestFocus();
            return false;
        } else activityAuthenticationBinding.inputLayout.textInputDisplayName.setError(null);

        return true;
    }

    private void showProgress() {
        activityAuthenticationBinding.inputLayout.tashieLoader.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        activityAuthenticationBinding.inputLayout.tashieLoader.setVisibility(View.GONE);
    }

    private void setInActive(Button btn) {
        btn.setBackgroundColor(getColor(R.color.darker_gray));
        btn.setTextColor(getColor(R.color.black));
    }

    private void setActive(Button btn) {
        btn.setBackgroundColor(getColor(R.color.orange));
        btn.setTextColor(getColor(R.color.white));
    }
}