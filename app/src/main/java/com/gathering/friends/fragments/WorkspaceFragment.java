package com.gathering.friends.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gathering.friends.activities.CreateWorkspaceActivity;
import com.gathering.friends.activities.DrawingSheet;
import com.gathering.friends.activities.GroupCallActivity;
import com.gathering.friends.adapters.ChatAdapter;
import com.gathering.friends.databinding.FragmentWorkspaceBinding;
import com.gathering.friends.models.Room;
import com.gathering.friends.util.Constants;
import com.gathering.friends.viewmodels.WorkspaceViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class WorkspaceFragment extends Fragment {
    FragmentWorkspaceBinding fragmentWorkspaceBinding;
    ChatAdapter<Room> chatAdapter;
    WorkspaceViewModel viewModel;
    List<Room> workspaceConnections = new ArrayList<>();

    public WorkspaceFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentWorkspaceBinding = FragmentWorkspaceBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(WorkspaceViewModel.class);

        initUI();

        // update the user connections
        viewModel.syncUserWorkspace(getContext());

        // listen for updates
        viewModel.roomsConnected().observe(getActivity(), new Observer<List<Room>>() {
            @Override
            public void onChanged(List<Room> rooms) {
                workspaceConnections.clear();
                workspaceConnections.addAll(rooms);
                chatAdapter.notifyDataSetChanged();
            }
        });

        return fragmentWorkspaceBinding.getRoot();
    }

    private void initUI() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);

        chatAdapter = new ChatAdapter<Room>(workspaceConnections, getContext(), Constants.GROUP_ROOM);
        fragmentWorkspaceBinding.recyclerViewWorkspace.setAdapter(chatAdapter);
        fragmentWorkspaceBinding.recyclerViewWorkspace.setHasFixedSize(true);
        fragmentWorkspaceBinding.recyclerViewWorkspace.setLayoutManager(linearLayoutManager);

        fragmentWorkspaceBinding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), CreateWorkspaceActivity.class);
                startActivity(intent);
            }
        });

        fragmentWorkspaceBinding.fabDrawingSheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), DrawingSheet.class);
                startActivity(intent);
            }
        });
    }
}