package com.gathering.friends.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gathering.friends.R;
import com.gathering.friends.adapters.ChatMessageAdapter;
import com.gathering.friends.databinding.ActivityDirectMessageBinding;
import com.gathering.friends.models.ChatMessage;
import com.gathering.friends.models.Room;
import com.gathering.friends.util.Constants;
import com.gathering.friends.util.Helper;
import com.gathering.friends.viewmodels.ChatMessagesViewModel;

import java.util.ArrayList;

public class DirectMessageActivity extends AppCompatActivity implements View.OnClickListener {
    ActivityDirectMessageBinding activityDirectMessageBinding;
    private final String[] permissions = new String[]{(Manifest.permission.CAMERA), (Manifest.permission.RECORD_AUDIO)};
    ChatMessagesViewModel viewModel;
    ChatMessageAdapter adapter;
    ArrayList<ChatMessage> list = new ArrayList<>();
    String roomID = null;
    String roomType = null;
    Room roomDetails;

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String msg = activityDirectMessageBinding.editTextMessage.getText().toString().trim();
            if (msg.isEmpty()) activityDirectMessageBinding.send.setVisibility(View.GONE);
            else activityDirectMessageBinding.send.setVisibility(View.VISIBLE);
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityDirectMessageBinding = ActivityDirectMessageBinding.inflate(getLayoutInflater());
        setContentView(activityDirectMessageBinding.getRoot());
        viewModel = ViewModelProviders.of(this).get(ChatMessagesViewModel.class);

        activityDirectMessageBinding.editTextMessage.addTextChangedListener(textWatcher);

        // get the room details whose chat messages need to be fetched
        roomID = getIntent().getStringExtra("room_id");
        roomType = getIntent().getStringExtra("room_type");

        viewModel.roomDetails(roomID, DirectMessageActivity.this, roomType).observe(this, new Observer<Room>() {
            @Override
            public void onChanged(Room room) {
                roomDetails = room;
                activityDirectMessageBinding.include.setRoom(room);

                if (!room.getRoomType().equals(Constants.DUO_ROOM)) {
                    activityDirectMessageBinding.include.roomProfileImage.setVisibility(View.GONE);
                }
            }
        });

        /*
        viewModel.chatMessages(roomID).observe(this, new Observer<List<ChatMessage>>() {
            @Override
            public void onChanged(List<ChatMessage> chatMessages) {
                list.clear();
                list.addAll(chatMessages);
                adapter.notifyDataSetChanged();
                activityDirectMessageBinding.recyclerViewChats.smoothScrollToPosition(list.size());
            }
        });
        */

        list.clear();
        viewModel.listenForNeMessages(roomID, roomType).observe(this, new Observer<ChatMessage>() {
            @Override
            public void onChanged(ChatMessage chatMessage) {
                list.add(chatMessage);
                adapter.notifyDataSetChanged();
                activityDirectMessageBinding.recyclerViewChats.smoothScrollToPosition(list.size());
            }
        });
        activityDirectMessageBinding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = activityDirectMessageBinding.editTextMessage.getText().toString().trim();
                activityDirectMessageBinding.editTextMessage.setText(null);
                viewModel.sendMessage(DirectMessageActivity.this, message, roomID, roomType);
            }
        });

        initRecyclerView();
        activityDirectMessageBinding.include.imageViewArrowBack.setOnClickListener(this);
        activityDirectMessageBinding.include.videoCallImg.setOnClickListener(this);
        activityDirectMessageBinding.include.roomProfileImage.setOnClickListener(this);
        activityDirectMessageBinding.include.roomDescription.setOnClickListener(this);
    }

    private void initRecyclerView() {
        adapter = new ChatMessageAdapter(list, DirectMessageActivity.this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(DirectMessageActivity.this, RecyclerView.VERTICAL, false);
        activityDirectMessageBinding.recyclerViewChats.setLayoutManager(linearLayoutManager);
        activityDirectMessageBinding.recyclerViewChats.setAdapter(adapter);
        activityDirectMessageBinding.recyclerViewChats.setHasFixedSize(true);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageViewArrowBack:
                onBackPressed();
                break;
            case R.id.videoCallImg:
                if (roomType.equals(Constants.DUO_ROOM)) {
                    // For duo participants we use peer js call
                    if (!isPermissionGranted()) {
                        askPermissions();
                    } else {
                        redirectToCall();
                    }
                } else {
                    // for workspace a room is already created use that and for meets also this id is available
                    // so redirect to multi participants video call activity
                    Intent intent = new Intent(this, GroupCallActivity.class);
                    intent.putExtra(Constants.MEET_TYPE, Constants.JOIN_MEET);
                    intent.putExtra(Constants.ROOM_ID, roomID);
                    intent.putExtra("room_type", roomType);
                    // only for meeting room allow to create or join room
                    if (roomType.equals(Constants.MEETING_ROOM))
                        intent.putExtra("openOrJoinMeet", true);
                    startActivity(intent);
                }
                break;
            case R.id.roomDescription:
                if (roomDetails != null && roomDetails.getRoomType().equals(Constants.GROUP_ROOM)) {
                    Intent intent = new Intent(this, WorkspaceDescriptionActivity.class);
                    intent.putExtra("room", roomDetails);
                    startActivity(intent);
                }
                break;
        }
    }

    private void redirectToCall() {
        if (roomDetails == null) {
            Helper.toast(DirectMessageActivity.this, "System is not ready. Connect to internet!");
            return;
        }

        String friendUserName = roomDetails.getRoomName();

        // because for due type room, room name contains other persons username with `@`
        friendUserName = friendUserName.substring(friendUserName.lastIndexOf('@') + 1);
        if (friendUserName.isEmpty()) {
            Helper.toast(DirectMessageActivity.this, "Room Id not found");
            return;
        }

        Intent intent = new Intent(DirectMessageActivity.this, CallActivity.class);
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
}