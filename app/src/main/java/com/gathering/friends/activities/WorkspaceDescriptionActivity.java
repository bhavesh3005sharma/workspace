package com.gathering.friends.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gathering.friends.R;
import com.gathering.friends.adapters.ChatAdapter;
import com.gathering.friends.databinding.ActivityWorkspaceDescriptionBinding;
import com.gathering.friends.databinding.DialogueAddParticipantsBinding;
import com.gathering.friends.models.Room;
import com.gathering.friends.models.User;
import com.gathering.friends.util.Constants;
import com.gathering.friends.util.Helper;
import com.gathering.friends.viewmodels.WorkspaceDescriptionViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WorkspaceDescriptionActivity extends AppCompatActivity implements View.OnClickListener {
    ActivityWorkspaceDescriptionBinding activityWorkspaceDescriptionBinding;
    WorkspaceDescriptionViewModel viewModel;
    Room room;
    ChatAdapter<User> chatAdapter;
    List<User> chatConnections = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityWorkspaceDescriptionBinding = ActivityWorkspaceDescriptionBinding.inflate(getLayoutInflater());
        setContentView(activityWorkspaceDescriptionBinding.getRoot());
        viewModel = ViewModelProviders.of(this).get(WorkspaceDescriptionViewModel.class);

        // set tool bar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.workspace_description));

        room = (Room) getIntent().getSerializableExtra("room");
        activityWorkspaceDescriptionBinding.setRoom(room);

        initUI();

        if (room.getRoomId() != null)
            viewModel.checkRoomUpdates(room.getRoomId()).observe(this, new Observer<Room>() {
                @Override
                public void onChanged(Room room) {
                    activityWorkspaceDescriptionBinding.setRoom(room);
                    ArrayList<User> list = new ArrayList<>();
                    if (room != null && room.getParticipants() != null) {
                        for (Map.Entry<String, String> entry : (room.getParticipants()).entrySet()) {
                            String key = entry.getKey();
                            User user = new User(key);
                            list.add(user);
                        }
                    }
                    chatConnections.clear();
                    chatConnections.addAll(list);
                    chatAdapter.notifyDataSetChanged();
                }
            });

        activityWorkspaceDescriptionBinding.nameEditIcon.setOnClickListener(this);
        activityWorkspaceDescriptionBinding.descriptionEditIcon.setOnClickListener(this);
        activityWorkspaceDescriptionBinding.nameCheckedIcon.setOnClickListener(this);
        activityWorkspaceDescriptionBinding.descriptionCheckedIcon.setOnClickListener(this);
        activityWorkspaceDescriptionBinding.buttonLeftWorkspace.setOnClickListener(this);
        activityWorkspaceDescriptionBinding.addParticipants.setOnClickListener(this);
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
                activityWorkspaceDescriptionBinding.nameEditIcon.setVisibility(View.GONE);
                activityWorkspaceDescriptionBinding.displayNameText.setVisibility(View.GONE);
                activityWorkspaceDescriptionBinding.displayNameEditText.setVisibility(View.VISIBLE);
                activityWorkspaceDescriptionBinding.nameCheckedIcon.setVisibility(View.VISIBLE);
                break;
            case R.id.name_checked_icon:
                String new_text = activityWorkspaceDescriptionBinding.displayNameEditText.getText().toString().trim();
                if (new_text.isEmpty()) {
                    Helper.toast(this, "Name is mandatory");
                    return;
                }
                activityWorkspaceDescriptionBinding.nameEditIcon.setVisibility(View.VISIBLE);
                activityWorkspaceDescriptionBinding.displayNameText.setVisibility(View.VISIBLE);
                activityWorkspaceDescriptionBinding.displayNameEditText.setVisibility(View.GONE);
                activityWorkspaceDescriptionBinding.nameCheckedIcon.setVisibility(View.GONE);
                viewModel.updateRoomName(new_text, room.getRoomId());
                break;
            case R.id.description_edit_icon:
                activityWorkspaceDescriptionBinding.descriptionEditIcon.setVisibility(View.GONE);
                activityWorkspaceDescriptionBinding.descriptionText.setVisibility(View.GONE);
                activityWorkspaceDescriptionBinding.descriptionEditText.setVisibility(View.VISIBLE);
                activityWorkspaceDescriptionBinding.descriptionCheckedIcon.setVisibility(View.VISIBLE);
                break;
            case R.id.description_checked_icon:
                String new_text1 = activityWorkspaceDescriptionBinding.descriptionEditText.getText().toString().trim();
                if (new_text1.isEmpty()) {
                    Helper.toast(this, "Name is mandatory");
                    return;
                }
                activityWorkspaceDescriptionBinding.descriptionEditIcon.setVisibility(View.VISIBLE);
                activityWorkspaceDescriptionBinding.descriptionText.setVisibility(View.VISIBLE);
                activityWorkspaceDescriptionBinding.descriptionEditText.setVisibility(View.GONE);
                activityWorkspaceDescriptionBinding.descriptionCheckedIcon.setVisibility(View.GONE);
                viewModel.updateRoomDescription(new_text1, room.getRoomId());
                break;
            case R.id.add_participants:
                showAddParticipantDialogue();
                break;
            case R.id.buttonLeftWorkspace:
                viewModel.leftWorkspace(room.getRoomId(), WorkspaceDescriptionActivity.this).observe(this, new Observer<String>() {
                    @Override
                    public void onChanged(String s) {
                        if (s.equals(Constants.SUCCESS)) {
                            Helper.toast(WorkspaceDescriptionActivity.this, "Workspace Left Successfully");
                            Intent intent = new Intent(WorkspaceDescriptionActivity.this, HomePage.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Helper.toast(WorkspaceDescriptionActivity.this, "Error occurred");
                        }
                    }
                });
                break;

        }
    }

    private void showAddParticipantDialogue() {
        AlertDialog.Builder builder = new AlertDialog.Builder(WorkspaceDescriptionActivity.this);
        DialogueAddParticipantsBinding dialogueAddParticipantsBinding = DialogueAddParticipantsBinding.inflate(getLayoutInflater());
        builder.setView(dialogueAddParticipantsBinding.getRoot());

        dialogueAddParticipantsBinding.textInfo.setText("Please wait, we are fetching details...");
        dialogueAddParticipantsBinding.submitButton.setVisibility(View.GONE);
        dialogueAddParticipantsBinding.progressBar.setVisibility(View.VISIBLE);

        viewModel.getUserConnections(WorkspaceDescriptionActivity.this).observe(this, new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> strings) {
                dialogueAddParticipantsBinding.chipGroupParticipants.removeAllViews();
                for (String s : strings) {
                    // add chip 
                    Chip chip1 = new Chip(WorkspaceDescriptionActivity.this);
                    chip1.setText(s);
                    chip1.setChipBackgroundColorResource(R.color.stroke_tint);
                    chip1.setTextColor(getResources().getColor(R.color.chip_text_tint));
                    chip1.setCheckable(true);
                    chip1.setFocusable(true);
                    chip1.setClickable(true);
                    dialogueAddParticipantsBinding.chipGroupParticipants.addView(chip1);
                }
                dialogueAddParticipantsBinding.progressBar.setVisibility(View.GONE);
                dialogueAddParticipantsBinding.submitButton.setVisibility(View.VISIBLE);
                dialogueAddParticipantsBinding.textInfo.setText(getString(R.string.select_participants_info));
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();

        alertDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        alertDialog.getWindow().setBackgroundDrawable(null);
        alertDialog.getWindow().setGravity(Gravity.BOTTOM);

        final String[] selectedParticipant = {null};
        dialogueAddParticipantsBinding.chipGroupParticipants.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                Chip chip = dialogueAddParticipantsBinding.chipGroupParticipants.findViewById(checkedId);
                selectedParticipant[0] = chip.getText().toString();
            }
        });

        dialogueAddParticipantsBinding.submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedParticipant[0] == null) {
                    Helper.toast(WorkspaceDescriptionActivity.this, "Please Select Participant");
                    return;
                }

                alertDialog.setCancelable(false);
                dialogueAddParticipantsBinding.textInfo.setText("Please wait, adding participant to this workspace...");
                dialogueAddParticipantsBinding.submitButton.setVisibility(View.GONE);
                dialogueAddParticipantsBinding.progressBar.setVisibility(View.VISIBLE);
                viewModel.addParticipant(room.getRoomId(), selectedParticipant).observe(WorkspaceDescriptionActivity.this, new Observer<String>() {
                    @Override
                    public void onChanged(String s) {
                        alertDialog.dismiss();
                        Helper.toast(WorkspaceDescriptionActivity.this, s);
                    }
                });
            }
        });
    }

    private void initUI() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(WorkspaceDescriptionActivity.this, RecyclerView.VERTICAL, false);

        chatAdapter = new ChatAdapter<User>(chatConnections, WorkspaceDescriptionActivity.this, Constants.DUO_ROOM);
        activityWorkspaceDescriptionBinding.recyclerView.setAdapter(chatAdapter);
        activityWorkspaceDescriptionBinding.recyclerView.setHasFixedSize(true);
        activityWorkspaceDescriptionBinding.recyclerView.setLayoutManager(linearLayoutManager);
    }
}