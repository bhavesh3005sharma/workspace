package com.gathering.friends.viewmodels;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.gathering.friends.database.Prefs;
import com.gathering.friends.models.User;
import com.gathering.friends.util.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ConnectionsViewModel extends ViewModel {
    private static final String TAG = "ConnectionsViewModel";
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
    MutableLiveData<List<User>> connectionRequestReceived = new MutableLiveData<>();
    MutableLiveData<List<User>> suggestionsList = new MutableLiveData<>();
    MutableLiveData<List<User>> connectionRequestSent = new MutableLiveData<>();

    public LiveData<List<User>> connectionRequestReceived() {
        return connectionRequestReceived;
    }

    public LiveData<List<User>> connectionRequestSent() {
        return connectionRequestSent;
    }

    public LiveData<List<User>> suggestionsList() {
        return suggestionsList;
    }

    public void loadData(Context context) {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<User> allUsers = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren())
                    allUsers.add(getUserModelFromDS(ds));

                databaseReference.orderByChild("username").equalTo(Prefs.getUser(context).getUsername()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            User user = getUserModelFromDS(ds);
                            // update user to Prefs
                            Prefs.setUserData(context, user);

                            ArrayList<String> connections = new ArrayList<>();
                            for (DataSnapshot d : ds.child("connections").getChildren()) {
                                connections.add(d.getKey());
                            }

                            ArrayList<String> requestReceived = new ArrayList<>();
                            for (DataSnapshot d : ds.child("connection-requests-received").getChildren()) {
                                requestReceived.add(d.getKey());
                            }

                            ArrayList<String> requestSent = new ArrayList<>();
                            for (DataSnapshot d : ds.child("connection-requests-sent").getChildren()) {
                                requestSent.add(d.getKey());
                            }

                            ArrayList<User> usersSuggestionsList = new ArrayList<>(),
                                    usersRequestsReceivedList = new ArrayList<>(),
                                    usersRequestsSentList = new ArrayList<>();

                            for (User u : allUsers) {
                                if (u.getUsername().equals(Prefs.getUser(context).getUsername()))
                                    continue;

                                if (requestReceived.contains(u.getUsername())) {
                                    // user u has sent a connections request to me
                                    u.setConnectionStatus(Constants.REQUEST_RECEIVED);
                                    usersRequestsReceivedList.add(u);
                                } else if (requestSent.contains(u.getUsername())) {
                                    // logged in user has sent the connection request to u
                                    // and this request is still pending
                                    u.setConnectionStatus(Constants.REQUEST_SENT);
                                    usersRequestsSentList.add(u);
                                } else {
                                    if (connections.contains(u.getUsername())) {
                                        // user u is in my connections
                                        u.setConnectionStatus(Constants.CONNECTED);
                                    } else {
                                        // user u is unknown to me
                                        u.setConnectionStatus(Constants.UNKNOWN);
                                    }
                                    usersSuggestionsList.add(u);
                                }
                            }

                            // update data to ui
                            suggestionsList.setValue(usersSuggestionsList);
                            connectionRequestReceived.setValue(usersRequestsReceivedList);
                            connectionRequestSent.setValue(usersRequestsSentList);

                            break;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private User getUserModelFromDS(DataSnapshot ds) {
        return new User((String) ds.child("username").getValue(), (String) ds.child("email").getValue(),
                (String) ds.child("displayName").getValue(), (String) ds.child("profileUri").getValue(), (String) ds.child("uid").getValue());
    }
}
