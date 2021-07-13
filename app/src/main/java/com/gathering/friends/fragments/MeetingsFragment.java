package com.gathering.friends.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gathering.friends.R;
import com.gathering.friends.activities.GroupCallActivity;
import com.gathering.friends.adapters.ChatAdapter;
import com.gathering.friends.databinding.DialogueMeetActionBinding;
import com.gathering.friends.databinding.FragmentsMeetingsBinding;
import com.gathering.friends.models.Room;
import com.gathering.friends.util.Constants;
import com.gathering.friends.viewmodels.MeetsViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MeetingsFragment extends Fragment {

    FragmentsMeetingsBinding fragmentsMeetingsBinding;
    DialogueMeetActionBinding dialogueMeetActionBinding;
    AlertDialog alertDialog;
    ChatAdapter<Room> chatAdapter;
    MeetsViewModel viewModel;
    List<Room> meetingRooms = new ArrayList<>();

    public MeetingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentsMeetingsBinding = FragmentsMeetingsBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(MeetsViewModel.class);

        initUI();

        // update the user connections
        viewModel.syncUserMeets(getContext());

        // listen for updates
        viewModel.roomsConnected().observe(getActivity(), new Observer<List<Room>>() {
            @Override
            public void onChanged(List<Room> rooms) {
                if (rooms == null) return;
                meetingRooms.clear();
                meetingRooms.addAll(rooms);
                chatAdapter.notifyDataSetChanged();
            }
        });

        return fragmentsMeetingsBinding.getRoot();
    }

    private void takeMeetAction(String action) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        dialogueMeetActionBinding = DialogueMeetActionBinding.inflate(getLayoutInflater());
        builder.setView(dialogueMeetActionBinding.getRoot());

        alertDialog = builder.create();
        alertDialog.show();

        alertDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        alertDialog.getWindow().setBackgroundDrawable(null);
        alertDialog.getWindow().setGravity(Gravity.BOTTOM);

        if (action.equals(Constants.JOIN_MEET)) {
            dialogueMeetActionBinding.textViewTitle.setText(getString(R.string.join_the_meeting));
            dialogueMeetActionBinding.textInputData.setHint(getString(R.string.enter_the_meeting_room_id));
        } else {
            dialogueMeetActionBinding.textViewTitle.setText(getString(R.string.create_a_new_meeting));
            dialogueMeetActionBinding.textInputData.setHint(getString(R.string.new_meeting_room_name));
        }

        dialogueMeetActionBinding.submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String data = dialogueMeetActionBinding.textInputData.getEditText().getText().toString().trim();

                if (data.isEmpty()) {
                    dialogueMeetActionBinding.textInputData.setError("Field is Required");
                    dialogueMeetActionBinding.textInputData.requestFocus();
                    return;
                } else dialogueMeetActionBinding.textInputData.setError(null);

                if (action.equals(Constants.CREATE_MEET)) {
                    dialogueMeetActionBinding.progressLoader.setVisibility(View.VISIBLE);
                    viewModel.createNewMeet(data, getContext()).observe(getActivity(), new Observer<String>() {
                        @Override
                        public void onChanged(String s) {
                            dialogueMeetActionBinding.progressLoader.setVisibility(View.VISIBLE);
                            if (s != null) {
                                redirectToCallActivity(action, s);
                            }
                        }
                    });
                } else {
                    redirectToCallActivity(action, data);
                }
            }
        });
    }

    private void redirectToCallActivity(String action, String room_id) {
        Intent intent = new Intent(getContext(), GroupCallActivity.class);
        intent.putExtra(Constants.MEET_TYPE, action);
        intent.putExtra(Constants.ROOM_ID, room_id);
        intent.putExtra("room_type", Constants.MEETING_ROOM);
        startActivity(intent);
    }

    private void initUI() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);

        chatAdapter = new ChatAdapter<Room>(meetingRooms, getContext(), Constants.MEETING_ROOM);
        fragmentsMeetingsBinding.recyclerViewWorkspace.setAdapter(chatAdapter);
        fragmentsMeetingsBinding.recyclerViewWorkspace.setHasFixedSize(true);
        fragmentsMeetingsBinding.recyclerViewWorkspace.setLayoutManager(linearLayoutManager);

        fragmentsMeetingsBinding.joinMeeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takeMeetAction(Constants.JOIN_MEET);
            }
        });

        fragmentsMeetingsBinding.newMeeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takeMeetAction(Constants.CREATE_MEET);
            }
        });
    }
}