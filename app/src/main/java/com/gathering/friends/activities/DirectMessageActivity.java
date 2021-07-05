package com.gathering.friends.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gathering.friends.R;
import com.gathering.friends.adapters.ChatMessageAdapter;
import com.gathering.friends.databinding.ActivityDirectMessageBinding;
import com.gathering.friends.models.ChatMessage;
import com.gathering.friends.models.Room;
import com.gathering.friends.viewmodels.ChatMessagesViewModel;

import java.util.ArrayList;
import java.util.List;

public class DirectMessageActivity extends AppCompatActivity implements View.OnClickListener {
    ActivityDirectMessageBinding activityDirectMessageBinding;
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
    ChatMessagesViewModel viewModel;
    ChatMessageAdapter adapter;
    ArrayList<ChatMessage> list = new ArrayList<>();
    String roomID = null;
    Room roomDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityDirectMessageBinding = ActivityDirectMessageBinding.inflate(getLayoutInflater());
        setContentView(activityDirectMessageBinding.getRoot());
        viewModel = ViewModelProviders.of(this).get(ChatMessagesViewModel.class);

        activityDirectMessageBinding.editTextMessage.addTextChangedListener(textWatcher);

        // get the room details whose chat messages need to be fetched
        roomID = getIntent().getStringExtra("room_id");

        viewModel.roomDetails(roomID, DirectMessageActivity.this).observe(this, new Observer<Room>() {
            @Override
            public void onChanged(Room room) {
                roomDetails = room;
                activityDirectMessageBinding.include.setRoom(room);
            }
        });
        viewModel.chatMessages(roomID).observe(this, new Observer<List<ChatMessage>>() {
            @Override
            public void onChanged(List<ChatMessage> chatMessages) {
                list.clear();
                list.addAll(chatMessages);
                adapter.notifyDataSetChanged();
                activityDirectMessageBinding.recyclerViewChats.smoothScrollToPosition(list.size());
            }
        });
        viewModel.listenForNeMessages(roomID).observe(this, new Observer<ChatMessage>() {
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
                viewModel.sendMessage(DirectMessageActivity.this, message, roomID);
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
        }
    }
}