package com.gathering.friends.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gathering.friends.R;
import com.gathering.friends.adapters.ChatMessageAdapter;
import com.gathering.friends.database.Prefs;
import com.gathering.friends.databinding.ActivityGroupCallBinding;
import com.gathering.friends.databinding.DialogueMeetDetailsBinding;
import com.gathering.friends.models.ChatMessage;
import com.gathering.friends.models.Room;
import com.gathering.friends.models.User;
import com.gathering.friends.util.Constants;
import com.gathering.friends.util.GroupCallJavascriptInterface;
import com.gathering.friends.util.Helper;
import com.gathering.friends.viewmodels.ChatMessagesViewModel;
import com.gathering.friends.viewmodels.MeetsViewModel;

import java.util.ArrayList;

public class GroupCallActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int CAPTURE_PERMISSION_REQUEST_CODE = 21632;
    private final String[] permissions = new String[]{(Manifest.permission.CAMERA), (Manifest.permission.RECORD_AUDIO)};
    ActivityGroupCallBinding activityGroupCallBinding;
    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String msg = activityGroupCallBinding.chatSection.editTextMessage.getText().toString().trim();
            if (msg.isEmpty()) activityGroupCallBinding.chatSection.send.setVisibility(View.GONE);
            else activityGroupCallBinding.chatSection.send.setVisibility(View.VISIBLE);
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };
    String meetType = null; // 'JOIN_MEET' or 'CREATE_MEET'
    String roomId = null;
    String rooType = null;
    boolean isAudio = true, isVideo = true, isAccessPanelVisible = false, openOrJoinMeet = false;
    ChatMessagesViewModel viewModel;
    ChatMessageAdapter adapter;
    ArrayList<ChatMessage> list = new ArrayList<>();
    Room roomDetails;
    Boolean isChatsExpanded = true, isEmojiPressed = false, isScreenShareEnabled = false;

    @Override
    protected void onStart() {
        if (!isPermissionGranted()) {
            askPermissions();
        }
        super.onStart();
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityGroupCallBinding = ActivityGroupCallBinding.inflate(getLayoutInflater());
        setContentView(activityGroupCallBinding.getRoot());
        viewModel = ViewModelProviders.of(this).get(ChatMessagesViewModel.class);

        activityGroupCallBinding.chatSection.editTextMessage.addTextChangedListener(textWatcher);

        meetType = getIntent().getStringExtra(Constants.MEET_TYPE);
        roomId = getIntent().getStringExtra(Constants.ROOM_ID);
        openOrJoinMeet = getIntent().getBooleanExtra("openOrJoinMeet", false);
        rooType = getIntent().getStringExtra("room_type");

        if (meetType == null || roomId == null) {
            Helper.toast(this, "Connection Error");
            abortGoBack();
        }

        User user = Prefs.getUser(this);
        // setup UI
        activityGroupCallBinding.toolbar.setTitle("Workspace Meeting Room");
        activityGroupCallBinding.toolbar.setInitial(String.valueOf(user.getUsername().charAt(0)));
        activityGroupCallBinding.toolbar.setPhotoUri(user.getProfileUri());
        if (user.getProfileUri() == null || user.getProfileUri().isEmpty())
            activityGroupCallBinding.toolbar.cardPhoto.setVisibility(View.GONE);
        else
            activityGroupCallBinding.toolbar.cardPhoto.setVisibility(View.VISIBLE);

        final boolean[] isWebViewLoaded = {false};
        viewModel.roomDetails(roomId, GroupCallActivity.this, rooType).observe(this, new Observer<Room>() {
            @Override
            public void onChanged(Room room) {
                if (room == null || room.getRoomId() == null) {
                    // Room does not exists invalid room id
                    Helper.toast(GroupCallActivity.this, "Invalid Room Id");
                    abortGoBack();
                    finish();
                    return;
                }
                // room validity verified

                roomDetails = room;
                activityGroupCallBinding.toolbar.setTitle(room.getRoomName());

                if (!isWebViewLoaded[0])
                    setupWebView();

                isWebViewLoaded[0] = true;
            }
        });

        list.clear();
        viewModel.listenForNeMessages(roomId, rooType).observe(this, new Observer<ChatMessage>() {
            @Override
            public void onChanged(ChatMessage chatMessage) {
                list.add(chatMessage);
                adapter.notifyDataSetChanged();
                activityGroupCallBinding.chatSection.recyclerViewChats.smoothScrollToPosition(list.size());
            }
        });

        initRecyclerView();

        setUpClickListeners();
    }

    private void setUpClickListeners() {
        activityGroupCallBinding.callControlLayout.toggleAudioBtn.setOnClickListener(this);
        activityGroupCallBinding.callControlLayout.toggleVideoBtn.setOnClickListener(this);
        activityGroupCallBinding.callControlLayout.acceptBtn.setOnClickListener(this);
        activityGroupCallBinding.callControlLayout.endCallBtn.setOnClickListener(this);
        activityGroupCallBinding.controlAccess.setOnClickListener(this);
        activityGroupCallBinding.meetInfo.setOnClickListener(this);
        activityGroupCallBinding.chatSection.send.setOnClickListener(this);
        activityGroupCallBinding.emoji.setOnClickListener(this);
        activityGroupCallBinding.emojiEyeHeart.setOnClickListener(this);
        activityGroupCallBinding.emojiThumpsUp.setOnClickListener(this);
        activityGroupCallBinding.emojiLaughing.setOnClickListener(this);
        activityGroupCallBinding.emojiNaughty.setOnClickListener(this);
        activityGroupCallBinding.chats.setOnClickListener(this);
        activityGroupCallBinding.screenShare.setOnClickListener(this);
    }

    private void setupWebView() {
        activityGroupCallBinding.webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                if (request != null) request.grant(request.getResources());
            }
        });

        activityGroupCallBinding.webView.getSettings().setJavaScriptEnabled(true);
        activityGroupCallBinding.webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        activityGroupCallBinding.webView.addJavascriptInterface(new GroupCallJavascriptInterface(this), "Android");

        loadVideoCall();
    }

    private void loadVideoCall() {
        String filePath = "file:android_asset/rtcmulticonnection.html";
        activityGroupCallBinding.webView.loadUrl(filePath);

        final int[] flag = {0};
        activityGroupCallBinding.webView.setWebViewClient(new WebViewClient());
        activityGroupCallBinding.webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (++flag[0] != 1) return;

                // It is mandatory as because once the video get loaded we cant able to make these views visible
                showHiddenUI();

                onRoomJoined();

                // according to meet-type take actions
                if (openOrJoinMeet) {
                    callJavascriptFunction("javascript:openOrJoin(\"" + roomId + "\")");
                    return;
                }

                if (meetType.equals(Constants.CREATE_MEET)) {
                    callJavascriptFunction("javascript:openRoom(\"" + roomId + "\")");
                } else if (meetType.equals(Constants.JOIN_MEET)) {
                    callJavascriptFunction("javascript:joinRoom(\"" + roomId + "\")");
                } else {
                    // problem occurred { Never will Happen , We always pass meet-type whenever start this activity }
                    abortGoBack();
                }
            }
        });
    }

    private void callJavascriptFunction(String s) {
        activityGroupCallBinding.webView.post(new Runnable() {
            @Override
            public void run() {
                activityGroupCallBinding.webView.evaluateJavascript(s, null);
            }
        });
    }

    private void abortGoBack() {
        callJavascriptFunction("javascript:leaveRoom()");
        Intent intent = new Intent(GroupCallActivity.this, HomePage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void initRecyclerView() {
        adapter = new ChatMessageAdapter(list, GroupCallActivity.this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(GroupCallActivity.this, RecyclerView.VERTICAL, false);
        activityGroupCallBinding.chatSection.recyclerViewChats.setLayoutManager(linearLayoutManager);
        activityGroupCallBinding.chatSection.recyclerViewChats.setAdapter(adapter);
        activityGroupCallBinding.chatSection.recyclerViewChats.setHasFixedSize(true);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.toggleAudioBtn:
                if (!isPermissionGranted()) {
                    askPermissions();
                    return;
                }
                isAudio = !isAudio;
                callJavascriptFunction("javascript:toggleAudio(\"" + isAudio + "\")");
                activityGroupCallBinding.callControlLayout.toggleAudioBtn.setImageResource(isAudio ? R.drawable.ic_baseline_mic_24 : R.drawable.ic_baseline_mic_off_24);
                break;
            case R.id.toggleVideoBtn:
                if (!isPermissionGranted()) {
                    askPermissions();
                    return;
                }
                isVideo = !isVideo;
                callJavascriptFunction("javascript:toggleVideo(\"" + isVideo + "\")");
                activityGroupCallBinding.callControlLayout.toggleVideoBtn.setImageResource(isVideo ? R.drawable.ic_baseline_videocam_24 : R.drawable.ic_baseline_videocam_off_24);
                break;
            case R.id.endCallBtn:
                // cancel the call
                callJavascriptFunction("javascript:leaveRoom()");

                // end the connection and go back
                abortGoBack();
                break;
            case R.id.controlAccess:
                if (isAccessPanelVisible) {
                    activityGroupCallBinding.layoutMeetControls.setVisibility(View.GONE);
                    activityGroupCallBinding.controlAccess.setImageResource(R.drawable.ic_arrow_backward);
                    isAccessPanelVisible = false;
                } else {
                    activityGroupCallBinding.layoutMeetControls.setVisibility(View.VISIBLE);
                    activityGroupCallBinding.controlAccess.setImageResource(R.drawable.ic_arrow_forward);
                    isAccessPanelVisible = true;
                }
                break;
            case R.id.meet_info:
                showDetailsInDialogue();
                break;
            case R.id.send:
                String message = activityGroupCallBinding.chatSection.editTextMessage.getText().toString().trim();
                activityGroupCallBinding.chatSection.editTextMessage.setText(null);
                viewModel.sendMessage(GroupCallActivity.this, message, roomId, rooType);
                break;
            case R.id.emoji:
                if (isEmojiPressed) {
                    activityGroupCallBinding.layoutMeetEmoji.setVisibility(View.GONE);
                    activityGroupCallBinding.emoji.setBackgroundColor(Color.WHITE);
                    isEmojiPressed = false;
                } else {
                    activityGroupCallBinding.layoutMeetEmoji.setVisibility(View.VISIBLE);
                    activityGroupCallBinding.emoji.setBackgroundColor(getColor(R.color.placeholder_bg));
                    isEmojiPressed = true;
                }
                break;
            case R.id.screenShare:
                if (isScreenShareEnabled) {
                    activityGroupCallBinding.screenShare.setBackgroundColor(Color.WHITE);
                    isScreenShareEnabled = false;
                } else {
                    startScreenCapture();
                    activityGroupCallBinding.screenShare.setBackgroundColor(getColor(R.color.placeholder_bg));
                    isScreenShareEnabled = true;
                }
                break;
            case R.id.emojiEyeHeart:
                sendEmoji(activityGroupCallBinding.emojiEyeHeart.getText().toString());
                break;
            case R.id.emojiLaughing:
                sendEmoji(activityGroupCallBinding.emojiLaughing.getText().toString());
                break;
            case R.id.emojiNaughty:
                sendEmoji(activityGroupCallBinding.emojiNaughty.getText().toString());
                break;
            case R.id.emojiThumpsUp:
                sendEmoji(activityGroupCallBinding.emojiThumpsUp.getText().toString());
                break;
            case R.id.chats:
                if (isChatsExpanded) {
                    activityGroupCallBinding.chatSection.getRoot().setVisibility(View.GONE);
                    isChatsExpanded = false;
                    activityGroupCallBinding.chats.setBackgroundColor(Color.WHITE);
                } else {
                    activityGroupCallBinding.chatSection.getRoot().setVisibility(View.VISIBLE);
                    isChatsExpanded = true;
                    activityGroupCallBinding.chats.setBackgroundColor(getColor(R.color.placeholder_bg));
                }
                break;
        }
    }

    private void sendEmoji(String emoji) {
        viewModel.sendMessage(this, emoji, roomId, rooType);
        activityGroupCallBinding.layoutMeetEmoji.setVisibility(View.GONE);
        activityGroupCallBinding.emoji.setBackgroundColor(Color.WHITE);
        isEmojiPressed = false;
    }

    @TargetApi(21)
    private void startScreenCapture() {
        MediaProjectionManager mediaProjectionManager =
                (MediaProjectionManager) getApplication().getSystemService(
                        Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(
                mediaProjectionManager.createScreenCaptureIntent(), CAPTURE_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != CAPTURE_PERMISSION_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                // screen share permission given
            }
        }
    }

    private void showDetailsInDialogue() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        DialogueMeetDetailsBinding dialogueMeetDetailsBinding = DialogueMeetDetailsBinding.inflate(getLayoutInflater());
        builder.setView(dialogueMeetDetailsBinding.getRoot());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        alertDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        alertDialog.getWindow().setBackgroundDrawable(null);
        alertDialog.getWindow().setGravity(Gravity.BOTTOM);

        dialogueMeetDetailsBinding.setMeetId(roomId);

        dialogueMeetDetailsBinding.copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String meetInfo = dialogueMeetDetailsBinding.meetDetails.getText().toString().trim();
                final android.content.ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("Workspace Meeting Info", meetInfo);
                clipboardManager.setPrimaryClip(clipData);
                Helper.toast(GroupCallActivity.this, "Copied Successfully");
            }
        });
    }

    public void onRoomJoined() {
        // user has successfully joined meet change UI
        activityGroupCallBinding.progressBar.setVisibility(View.GONE);

        // add this meeting in his history
        MeetsViewModel meetsViewModel = new MeetsViewModel();
        meetsViewModel.saveMeetHistory(roomId, this);
    }

    public void showHiddenUI() {
        activityGroupCallBinding.callControlLayout.callController.setVisibility(View.VISIBLE);
        activityGroupCallBinding.controlAccess.setVisibility(View.VISIBLE);
        activityGroupCallBinding.layoutMeetControls.setVisibility(View.VISIBLE);
        activityGroupCallBinding.chatSection.getRoot().setVisibility(View.VISIBLE);
        isAccessPanelVisible = true;
    }

    public void onCallSetUpError(String error) {
        Helper.toast(this, error);
        abortGoBack();
    }
}