package com.gathering.friends.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gathering.friends.adapters.ChatAdapter;
import com.gathering.friends.databinding.FragmentChatBinding;
import com.gathering.friends.models.User;
import com.gathering.friends.util.Constants;
import com.gathering.friends.util.Helper;
import com.gathering.friends.viewmodels.ChatViewModel;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {
    FragmentChatBinding fragmentChatBinding;
    ChatViewModel viewModel;
    ChatAdapter<User> chatAdapter;
    List<User> chatConnections = new ArrayList<>();

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentChatBinding = FragmentChatBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(ChatViewModel.class);

        initUI();

        // update the user connections
        viewModel.syncUserConnections(getContext());

        // listen for updates
        viewModel.chatConnectedUsers().observe(getActivity(), new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                Log.i("TAG", "onChanged: size " + users.size());
                chatConnections.clear();
                chatConnections.addAll(users);
                chatAdapter.notifyDataSetChanged();
            }
        });

        return fragmentChatBinding.getRoot();
    }

    private void initUI() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);

        chatAdapter = new ChatAdapter<User>(chatConnections, getContext(), Constants.DUO_ROOM);
        fragmentChatBinding.recyclerViewChatConnections.setAdapter(chatAdapter);
        fragmentChatBinding.recyclerViewChatConnections.setHasFixedSize(true);
        fragmentChatBinding.recyclerViewChatConnections.setLayoutManager(linearLayoutManager);

        fragmentChatBinding.logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helper.signOut(getContext());
                getActivity().finish();
            }
        });
    }
}