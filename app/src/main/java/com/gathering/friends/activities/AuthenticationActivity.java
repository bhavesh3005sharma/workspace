package com.gathering.friends.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.gathering.friends.R;
import com.gathering.friends.database.Prefs;
import com.gathering.friends.databinding.ActivityAuthenticationBinding;
import com.gathering.friends.databinding.DialogueLoadingBinding;
import com.gathering.friends.databinding.DialogueMeetActionBinding;
import com.gathering.friends.models.LoginRequest;
import com.gathering.friends.models.User;
import com.gathering.friends.util.Constants;
import com.gathering.friends.viewmodels.AuthViewModel;

public class AuthenticationActivity extends AppCompatActivity implements View.OnClickListener {
    ActivityAuthenticationBinding activityAuthenticationBinding;
    DialogueLoadingBinding dialogueLoadingBinding;
    AlertDialog alertDialogProgress;
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
        activityAuthenticationBinding.inputLayout.textForgotPassword.setOnClickListener(this);
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
                    showProgressDialogue("Please wait, we are verifying details...");

                    authViewModel.isCorrectUser(AuthenticationActivity.this, loginRequest).observe(this, new Observer<String>() {
                        @Override
                        public void onChanged(String s) {

                            if (s != null && s.equals(Constants.SUCCESS)) {
                                s = "Login Success!";
                                Intent intent = new Intent(AuthenticationActivity.this, HomePage.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                                if (alertDialogProgress != null) alertDialogProgress.dismiss();
                                return;
                            }
                            setProgressResult(s);
                        }
                    });
                } else {
                    if (!valid(email, password, username, displayName)) return;
                    showProgressDialogue("Please wait, we are creating your account...");

                    User user = new User(username, email, displayName, null, null, null);
                    authViewModel.registerUser(user, password).observe(this, new Observer<String>() {
                        @Override
                        public void onChanged(String s) {

                            if (s != null && s.equals(Constants.SUCCESS)) {
                                s = "Registered Successfully\n Verification Email Sent, Verify to Login";
                            }

                            if (s != null && s.equals(Constants.DUPLICATE)) {
                                s = "This username is already in use. Please choose another one";
                            }

                            setProgressResult(s);
                        }
                    });
                }
                break;
            case R.id.textForgotPassword:
                openPasswordResetDialogue();
                break;
        }
    }

    private void openPasswordResetDialogue() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        DialogueMeetActionBinding dialogueMeetActionBinding = DialogueMeetActionBinding.inflate(getLayoutInflater());
        builder.setView(dialogueMeetActionBinding.getRoot());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        alertDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        alertDialog.getWindow().setBackgroundDrawable(null);
        alertDialog.getWindow().setGravity(Gravity.BOTTOM);

        dialogueMeetActionBinding.textViewTitle.setText(getString(R.string.rset_password_link_request));
        dialogueMeetActionBinding.submitButton.setText(getString(R.string.submit));
        dialogueMeetActionBinding.textInputData.setHint(getString(R.string.email));

        dialogueMeetActionBinding.submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = dialogueMeetActionBinding.textInputData.getEditText().getText().toString().trim();

                if (email.isEmpty()) {
                    dialogueMeetActionBinding.textInputData.setError("Field is Required");
                    dialogueMeetActionBinding.textInputData.requestFocus();
                    return;
                } else dialogueMeetActionBinding.textInputData.setError(null);

                alertDialog.dismiss();
                showProgressDialogue("Sending Password Reset Link to Email...");
                authViewModel.sendResetPasswordLink(email).observe(AuthenticationActivity.this, new Observer<String>() {
                    @Override
                    public void onChanged(String s) {
                        if (s.equals(Constants.SUCCESS)) {
                            setProgressResult("Password Reset Link Sent to email");
                        } else {
                            setProgressResult(s);
                        }
                    }
                });
            }
        });
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

    private void setProgressResult(String s) {
        dialogueLoadingBinding.dismissButton.setVisibility(View.VISIBLE);
        dialogueLoadingBinding.progressLoader.setVisibility(View.GONE);
        dialogueLoadingBinding.textViewTitle.setText(s);
    }

    private void setInActive(Button btn) {
        btn.setBackgroundColor(getColor(R.color.darker_gray));
        btn.setTextColor(getColor(R.color.black));
    }

    private void setActive(Button btn) {
        btn.setBackgroundColor(getColor(R.color.orange));
        btn.setTextColor(getColor(R.color.white));
    }

    private void showProgressDialogue(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(AuthenticationActivity.this);
        dialogueLoadingBinding = DialogueLoadingBinding.inflate(getLayoutInflater());
        builder.setView(dialogueLoadingBinding.getRoot());

        dialogueLoadingBinding.textViewTitle.setText(message);

        alertDialogProgress = builder.create();
        alertDialogProgress.setCancelable(false);
        alertDialogProgress.show();

        alertDialogProgress.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        alertDialogProgress.getWindow().setBackgroundDrawable(null);
        alertDialogProgress.getWindow().setGravity(Gravity.BOTTOM);

        dialogueLoadingBinding.dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialogProgress.dismiss();
            }
        });
    }
}