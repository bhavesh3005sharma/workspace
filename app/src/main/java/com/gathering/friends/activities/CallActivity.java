package com.gathering.friends.activities;

import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.gathering.friends.R;
import com.gathering.friends.database.Prefs;
import com.gathering.friends.databinding.ActivityCallBinding;
import com.gathering.friends.util.CallService;
import com.gathering.friends.util.Constants;
import com.gathering.friends.util.Helper;
import com.gathering.friends.util.JavascriptInterface;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class CallActivity extends AppCompatActivity implements View.OnClickListener {
    String userType = null;
    private static final String TAG = "CallActivity";

    ActivityCallBinding activityCallBinding;
    boolean isPeerConnected = false, isAudio = true, isVideo = true, isCallConnected = false;
    String otherUserId = null;

    DatabaseReference firebaseRef = FirebaseDatabase.getInstance().getReference();
    CountDownTimer timer;
    String userId;
    String initialCallStatus = null;
    ValueEventListener listenForReceiverResponse = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            String status = (String) snapshot.getValue();
            if (null != status) {
                if (status.equals(Constants.BUSY)) {
                    // receiver is busy on another call.
                    activityCallBinding.callStatus.setText("Busy on another call...");
                } else if (status.equals(Constants.REJECTED)) {
                    // stop the timer
                    stopTimer();
                    // stop listening for response
                    removeListenerFromReceiverEnd();

                    // Make the status of receiver and caller as free to listen other calls
                    changeUserStatus(otherUserId, null);
                    changeUserStatus(userId, null);

                    // receiver has rejected the call
                    Helper.toast(CallActivity.this, "Call Rejected");
                    abortGoBack();
                } else {
                    // stop the timer
                    stopTimer();

                    // Make the status of receiver as busy
                    changeUserStatus(otherUserId, Constants.BUSY);

                    isCallConnected = true;

                    // user has accepted the call and in response provided his connection id in snapshot to connect with peer to peer
                    setupCallConnectedLayout();
                    callJavascriptFunction("javascript:startCall(\"" + snapshot.getValue() + "\")");
                }
            } else if (isCallConnected) {
                // user has disconnected the call after having conversation with this person

                // free this person
                changeUserStatus(userId, null);

                // stop listening for response
                removeListenerFromReceiverEnd();

                // end call from this side as well
                callJavascriptFunction("javascript:cancelCall()");

                abortGoBack();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Helper.toast(CallActivity.this, "Error Occurred in Connection");
            abortGoBack();
        }
    };
    ValueEventListener listenerForCallerResponse = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (!snapshot.exists() || snapshot.getValue() == null) {
                // call has ended
                callJavascriptFunction("javascript:cancelCall()");

                changeUserStatus(userId, null);
                abortGoBack();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };
    private String uniqueId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityCallBinding = ActivityCallBinding.inflate(getLayoutInflater());
        setContentView(activityCallBinding.getRoot());

        userId = Prefs.getUser(CallActivity.this).getUsername();
        userType = getIntent().getStringExtra("user_type");
        otherUserId = getIntent().getStringExtra("other_user_id");
        initialCallStatus = getIntent().getStringExtra("call_status");

        // stop call service and remove ongoing call notification
        if (userType.equals(Constants.CALL_RECEIVER)) {
            stopService(new Intent(this, CallService.class));
            // set callConnected true
            isCallConnected = true;
        }

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
            activityCallBinding.callStatus.setText("Dialing");
            activityCallBinding.acceptBtn.setVisibility(View.GONE);
        } else if (userType.equals(Constants.CALL_RECEIVER)) {
            // set up the view as this user is receiving a call from ${otherUserId};
            if (initialCallStatus != null && initialCallStatus.equals(Constants.CALL_PICKED)) {
                activityCallBinding.textViewUsername.setText(otherUserId);
                activityCallBinding.callStatus.setText("Incoming Call...\n Please wait call is getting connected...");
                activityCallBinding.acceptBtn.setVisibility(View.GONE);
            } else {
                activityCallBinding.textViewUsername.setText(otherUserId);
                activityCallBinding.callStatus.setText("Incoming Call");
                activityCallBinding.acceptBtn.setVisibility(View.VISIBLE);
            }
        } else {
            // There must be a error
            abortGoBack();
        }
    }

    private void setupWebView() {
        final int[] flag = {0};
        activityCallBinding.webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                if (++flag[0] == 1) {
                    if (request != null) request.grant(request.getResources());
                }
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

        final int[] flag = {0};
        activityCallBinding.webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (++flag[0] != 1) return;
                initializePeer();
            }
        });
    }

    private String getUniqueID() {
        return UUID.randomUUID().toString();
    }

    private void initializePeer() {
        uniqueId = getUniqueID();

        callJavascriptFunction("javascript:init(\"" + uniqueId + "\")");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isPeerConnected) {
                    Helper.toast(CallActivity.this, "You're not connected. Check your internet");
                    abortGoBack();
                    return;
                }

                if (otherUserId == null) {
                    Helper.toast(CallActivity.this, "Unable to connect with other Peer Id not Found");
                    abortGoBack();
                    return;
                }

                if (userType.equals(Constants.CALLER)) {
                    sendCallRequest();

                    // make user status as busy on call
                    changeUserStatus(userId, Constants.BUSY);
                } else if (userType.equals(Constants.CALL_RECEIVER) && initialCallStatus.equals(Constants.CALL_PICKED)) {
                    // accept the call and notify to other user
                    changeUserStatus(userId, uniqueId);

                    // change UI and wait for other person's stream
                    setupCallConnectedLayout();

                    // listen for the opposite person's response on call
                    firebaseRef.child("users").child(otherUserId).child("connectionId").addValueEventListener(listenerForCallerResponse);
                }
            }
        }, 5000);

    }

    private void changeUserStatus(String id, String status) {
        firebaseRef.child("users").child(id).child("connectionId").setValue(status);
    }

    private void removeListenerFromReceiverEnd() {
        if (listenForReceiverResponse != null)
            firebaseRef.child("users").child(otherUserId).child("connectionId").removeEventListener(listenForReceiverResponse);
    }

    private void removeListenerFromCallerEnd() {
        if (listenerForCallerResponse != null)
            firebaseRef.child("users").child(otherUserId).child("connectionId").removeEventListener(listenerForCallerResponse);
    }

    private void setupCallConnectedLayout() {
        activityCallBinding.textViewUsername.setVisibility(View.GONE);
        activityCallBinding.callStatus.setVisibility(View.GONE);
    }

    private void stopTimer() {
        if (timer != null) timer.cancel();
    }

    private void abortGoBack() {
        Intent intent = new Intent(CallActivity.this, HomePage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void sendCallRequest() {

        //  Make call request to receiver & send by FCM
        JSONObject callRequest = new JSONObject();
        try {
            callRequest.put("isCallingNotification", true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            callRequest.put("caller_user_name", Prefs.getUser(this).getUsername());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            callRequest.put("photoUri", Prefs.getUser(this).getProfileUri());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Helper.sendNotificationToUser(otherUserId, callRequest);


        // listen for users response
        firebaseRef.child("users").child(otherUserId).child("connectionId").addValueEventListener(listenForReceiverResponse);

        // set up the timer to listener for response for 30 seconds to marks the call as NOT_ANSWERED
        ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_RING, 50);
        timer = new CountDownTimer(40000, 1000) {
            public void onTick(long millisUntilFinished) {
                activityCallBinding.callStatus.setText("Ringing");
                toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200); // 200 is duration in ms
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
        changeUserStatus(userId, null);

        // disconnect peer
        callJavascriptFunction("javascript:disconnectPeer()");
        activityCallBinding.webView.loadUrl("about:blank");

        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.toggleAudioBtn:
                isAudio = !isAudio;
                callJavascriptFunction("javascript:toggleAudio(\"" + isAudio + "\")");
                activityCallBinding.toggleAudioBtn.setImageResource(isAudio ? R.drawable.ic_baseline_mic_24 : R.drawable.ic_baseline_mic_off_24);
                break;
            case R.id.toggleVideoBtn:
                isVideo = !isVideo;
                callJavascriptFunction("javascript:toggleVideo(\"" + isVideo + "\")");
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
                    // change UI and wait for other person's stream
                    setupCallConnectedLayout();

                    // notify the user
                    Helper.toast(CallActivity.this, "Call Accepted");
                }
                break;
            case R.id.endCallBtn:
                if (!isCallConnected) {
                    if (userType.equals(Constants.CALL_RECEIVER)) {
                        // change status to notify the another peer
                        changeUserStatus(userId, Constants.REJECTED);

                        // notify the user
                        Helper.toast(CallActivity.this, "Call Declined");
                    } else {
                        changeUserStatus(userId, null);
                    }
                } else {
                    // cancel the call
                    callJavascriptFunction("javascript:cancelCall()");

                    // change status to notify the another peer
                    changeUserStatus(userId, null);

                    if (userType.equals(Constants.CALLER))
                        removeListenerFromReceiverEnd();
                    else if (userType.equals(Constants.CALL_RECEIVER))
                        removeListenerFromCallerEnd();
                }

                // end the connection and go back
                abortGoBack();
                break;
        }
    }
}