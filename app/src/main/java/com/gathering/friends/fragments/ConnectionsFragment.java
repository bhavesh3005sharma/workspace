package com.gathering.friends.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gathering.friends.R;
import com.gathering.friends.adapters.ConnectionRequestAdapter;
import com.gathering.friends.adapters.ConnectionSuggestionsAdapter;
import com.gathering.friends.databinding.FragmentConnectionsBinding;
import com.gathering.friends.models.User;
import com.gathering.friends.util.Constants;
import com.gathering.friends.viewmodels.ConnectionsViewModel;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class ConnectionsFragment extends Fragment {

    FragmentConnectionsBinding fragmentConnectionsBinding;
    ConnectionsViewModel viewModel;
    ConnectionRequestAdapter connectionRequestReceivedAdapter, connectionRequestSentAdapter;
    ConnectionSuggestionsAdapter connectionSuggestionsAdapter;
    ArrayList<User> connectionRequestsReceived = new ArrayList<>(),
            connectionRequestsSent = new ArrayList<>(),
            connectionSuggestions = new ArrayList<>();


    public ConnectionsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentConnectionsBinding = FragmentConnectionsBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(ConnectionsViewModel.class);

        initUI();
        viewModel.loadData(getContext());

        viewModel.connectionRequestReceived().observe(getActivity(), new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                if (users.isEmpty())
                    fragmentConnectionsBinding.textViewNoRequestInfo.setVisibility(View.VISIBLE);
                else fragmentConnectionsBinding.textViewNoRequestInfo.setVisibility(View.GONE);

                connectionRequestsReceived.clear();
                connectionRequestsReceived.addAll(users);
                connectionRequestReceivedAdapter.notifyDataSetChanged();
            }
        });

        viewModel.connectionRequestSent().observe(getActivity(), new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                if (users.isEmpty())
                    fragmentConnectionsBinding.textViewNoRequestInfo.setVisibility(View.VISIBLE);
                else fragmentConnectionsBinding.textViewNoRequestInfo.setVisibility(View.GONE);

                connectionRequestsSent.clear();
                connectionRequestsSent.addAll(users);
                connectionRequestSentAdapter.notifyDataSetChanged();
            }
        });

        viewModel.suggestionsList().observe(getActivity(), new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                connectionSuggestions.clear();
                connectionSuggestions.addAll(users);
                connectionSuggestionsAdapter.notifyDataSetChanged();
            }
        });

        fragmentConnectionsBinding.chipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                fragmentConnectionsBinding.cardViewConnectionSuggestions.setVisibility(View.GONE);
                fragmentConnectionsBinding.cardViewConnectionRequestsReceived.setVisibility(View.GONE);
                fragmentConnectionsBinding.cardViewConnectionRequestsSent.setVisibility(View.GONE);

                switch (checkedId) {
                    case R.id.chip_suggestions:
                        fragmentConnectionsBinding.cardViewConnectionSuggestions.setVisibility(View.VISIBLE);
                        break;
                    case R.id.chip_request_received:
                        fragmentConnectionsBinding.cardViewConnectionRequestsReceived.setVisibility(View.VISIBLE);
                        break;
                    case R.id.chip_requests_sent:
                        fragmentConnectionsBinding.cardViewConnectionRequestsSent.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });

        return fragmentConnectionsBinding.getRoot();
    }

    private void initUI() {
        LinearLayoutManager linearLayoutManagerRR = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        LinearLayoutManager linearLayoutManagerRS = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);

        // init request received Recycler view
        connectionRequestReceivedAdapter = new ConnectionRequestAdapter(connectionRequestsReceived, getContext(), Constants.REQUEST_RECEIVED);
        fragmentConnectionsBinding.rvConnectionRequestReceived.setAdapter(connectionRequestReceivedAdapter);
        fragmentConnectionsBinding.rvConnectionRequestReceived.setHasFixedSize(true);
        fragmentConnectionsBinding.rvConnectionRequestReceived.setLayoutManager(linearLayoutManagerRR);

        // init request sent Recycler view
        connectionRequestSentAdapter = new ConnectionRequestAdapter(connectionRequestsSent, getContext(), Constants.REQUEST_SENT);
        fragmentConnectionsBinding.rvConnectionRequestSent.setAdapter(connectionRequestSentAdapter);
        fragmentConnectionsBinding.rvConnectionRequestSent.setHasFixedSize(true);
        fragmentConnectionsBinding.rvConnectionRequestSent.setLayoutManager(linearLayoutManagerRS);

        // init connection suggestions Recycler view
        connectionSuggestionsAdapter = new ConnectionSuggestionsAdapter(connectionSuggestions, getContext());
        fragmentConnectionsBinding.rvSuggestions.setAdapter(connectionSuggestionsAdapter);
        fragmentConnectionsBinding.rvSuggestions.setHasFixedSize(true);
        fragmentConnectionsBinding.rvSuggestions.setLayoutManager(gridLayoutManager);
    }
}