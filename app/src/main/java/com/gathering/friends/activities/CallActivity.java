package com.gathering.friends.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.gathering.friends.R;
import com.gathering.friends.databinding.ActivityCallBinding;
import com.gathering.friends.util.Constants;
import com.gathering.friends.util.Helper;
import com.gathering.friends.util.JavascriptInterface;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;

public class CallActivity extends AppCompatActivity implements View.OnClickListener {
    String userType = null;

    ActivityCallBinding activityCallBinding;
    boolean isPeerConnected = false, isAudio = true, isVideo = true;
    String otherUserId = null;

    DatabaseReference firebaseRef = FirebaseDatabase.getInstance().getReference("users");
    CountDownTimer timer;
    String userId = "12111";
    ValueEventListener listenForReceiverResponse = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            String status = (String) snapshot.getValue();
            if (status.equals(Constants.BUSY)) {
                // receiver is busy on another call.
                activityCallBinding.callStatus.setText("Busy on another call...");
            } else if (status.equals(Constants.REJECTED)) {
                // stop the timer
                stopTimer();

                // Make the status of receiver and caller as free to listen other calls
                changeUserStatus(otherUserId, null);
                changeUserStatus(userId, null);

                // receiver has rejected the call
                Helper.toast(CallActivity.this, "Call Rejected");
                abortGoBack();
            } else if (status != null) {
                // stop the timer
                stopTimer();

                // Make the status of receiver as busy
                changeUserStatus(otherUserId, Constants.BUSY);

                // user has accepted the call and in response provided his connection id in snapshot to connect with peer to peer
                setupCallConnectedLayout();
                callJavascriptFunction("javascript:startCall(\"${snapshot.value}\")");
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Helper.toast(CallActivity.this, "Error Occurred in Connection");
            abortGoBack();
        }
    };
    private String uniqueId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityCallBinding = ActivityCallBinding.inflate(getLayoutInflater());
        setContentView(activityCallBinding.getRoot());

        userType = getIntent().getStringExtra("user_type");
        otherUserId = getIntent().getStringExtra("other_user_id");

        initUI();
        setUpClickListeners();

        setupWebView();
    }

    private void setUpClickListeners() {
        activityCallBinding.toggleAudioBtn.setOnClickListener(this);
        activityCallBinding.toggleVideoBtn.setOnClickListener(this);
        activityCallBinding.acceptBtn.setOnClickListener(this);
        activityCallBinding.endCallBtn.setOnClickListener(this);
    }

    private void initUI() {
        if (userType.equals(Constants.CALLER)) {
            // set up the view as this user is making a call to ${otherUserId};
            activityCallBinding.textViewUsername.setText(otherUserId);
            activityCallBinding.callStatus.setText("Dialing...");
        } else if (userType.equals(Constants.CALL_RECEIVER)) {
            // set up the view as this user is receiving a call from ${otherUserId};
            activityCallBinding.textViewUsername.setText(otherUserId);
            activityCallBinding.callStatus.setText("Incoming Call...");
            activityCallBinding.acceptBtn.setVisibility(View.VISIBLE);
        } else {
            // There must be a error
            abortGoBack();
        }
    }

    private void setupWebView() {

        activityCallBinding.webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                super.onPermissionRequest(request);
                if (request != null) request.grant(request.getResources());
            }
        });

        activityCallBinding.webView.getSettings().setJavaScriptEnabled(true);
        activityCallBinding.webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        activityCallBinding.webView.addJavascriptInterface(new JavascriptInterface(this), "Android");

        loadVideoCall();
    }

    private void loadVideoCall() {
        String filePath = "file:android_asset/call.html";
        activityCallBinding.webView.loadUrl(filePath);

        activityCallBinding.webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                super.onPermissionRequest(request);
                initializePeer();
            }
        });
    }

    private String getUniqueID() {
        return UUID.randomUUID().toString();
    }

    private void initializePeer() {
        uniqueId = getUniqueID();

        callJavascriptFunction("javascript:init(\"${uniqueId}\")");

        if (!isPeerConnected) {
            Helper.toast(this, "You're not connected. Check your internet");
            abortGoBack();
            return;
        }

        if (userType.equals(Constants.CALLER) && otherUserId != null) {
            sendCallRequest();

            // make user status as busy on call
            changeUserStatus(userId, Constants.BUSY);

        }
    }

    private void changeUserStatus(String id, String status) {
        firebaseRef.child("users").child(id).child("connectionId").setValue(status);
    }

    private void removeListenerFromReceiverEnd() {
        if (listenForReceiverResponse != null)
            firebaseRef.child("users").child(otherUserId).child("connectionId").removeEventListener(listenForReceiverResponse);
    }

    private void setupCallConnectedLayout() {
        activityCallBinding.textViewUsername.setVisibility(View.GONE);
        activityCallBinding.callStatus.setVisibility(View.GONE);
    }

    private void stopTimer() {
        if (timer != null) timer.cancel();
        // stop listening for response
        removeListenerFromReceiverEnd();
    }

    private void abortGoBack() {
        Intent intent = new Intent(CallActivity.this, HomePage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void sendCallRequest() {

        // TODO : Make call request to receiver by FCM

        // listen for users response
        firebaseRef.child("users").child(otherUserId).child("connectionId").addValueEventListener(listenForReceiverResponse);

        // set up the timer to listener for response for 30 seconds to marks the call as NOT_ANSWERED
        timer = new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {
                // TODO: Add a beeping sound to the call
            }

            public void onFinish() {
                // Receiver has not responded the call
                Helper.toast(CallActivity.this, "Not Answered");
                removeListenerFromReceiverEnd();
                changeUserStatus(userId, null);
                abortGoBack();
            }
        }.start();
    }

    private void callJavascriptFunction(String s) {
        activityCallBinding.webView.post(new Runnable() {
            @Override
            public void run() {
                activityCallBinding.webView.evaluateJavascript(s, null);
            }
        });
    }

    public void onPeerConnected() {
        isPeerConnected = true;
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        activityCallBinding.webView.loadUrl("about:blank");
        changeUserStatus(userId, null);
        changeUserStatus(otherUserId, null);
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.toggleAudioBtn:
                isAudio = !isAudio;
                callJavascriptFunction("javascript:toggleAudio(\"${isAudio}\")");
                activityCallBinding.toggleAudioBtn.setImageResource(isAudio ? R.drawable.ic_baseline_mic_24 : R.drawable.ic_baseline_mic_off_24);
                break;
            case R.id.toggleVideoBtn:
                isVideo = !isVideo;
                callJavascriptFunction("javascript:toggleAudio(\"${isAudio}\")");
                activityCallBinding.toggleVideoBtn.setImageResource(isVideo ? R.drawable.ic_baseline_videocam_24 : R.drawable.ic_baseline_videocam_off_24);
                break;
            case R.id.acceptBtn:
                if (userType.equals(Constants.CALL_RECEIVER)) {
                    if (!isPeerConnected) {
                        // peer is not connected till now
                        Helper.toast(this, "Please wait your connection is not ready");
                        return;
                    }

                    // if connected and accepting the call provide connection id to another peer
                    changeUserStatus(userId, uniqueId);

                    // set up calling ui / hide call picking button
                    activityCallBinding.acceptBtn.setVisibility(View.GONE);

                    // notify the user
                    Helper.toast(CallActivity.this, "Call Accepted");
                }
                break;
            case R.id.endCallBtn:
                // change status to notify the another peer
                changeUserStatus(userId, Constants.REJECTED);

                // end the connection and go back
                abortGoBack();

                // notify the user
                Helper.toast(CallActivity.this, "Call Declined");
                break;
        }
    }
}