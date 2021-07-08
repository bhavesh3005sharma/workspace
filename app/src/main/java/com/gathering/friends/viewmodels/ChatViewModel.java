package com.gathering.friends.viewmodels;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.gathering.friends.database.Prefs;
import com.gathering.friends.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatViewModel extends ViewModel {

    MutableLiveData<List<User>> chatConnectedUsers = new MutableLiveData<>();

    public MutableLiveData<List<User>> chatConnectedUsers() {
        return chatConnectedUsers;
    }

    public void syncUserConnections(Context context) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(Prefs.getUser(context).getUsername()).child("connections");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loadDataOfUsers(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadDataOfUsers(DataSnapshot sna) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        List<User> usersData = new ArrayList<>();

        final int[] count = {0};
        for (DataSnapshot ds : sna.getChildren()) {
            databaseReference.child(ds.getKey()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = getUserModelFromDS(snapshot);
                    user.setRoomId(ds.getValue().toString());
                    usersData.add(user);

                    // check have we fetched the data for all users?
                    if (++count[0] >= sna.getChildrenCount()) {
                        chatConnectedUsers.postValue(usersData);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private User getUserModelFromDS(DataSnapshot ds) {
        return new User((String) ds.child("username").getValue(), (String) ds.child("email").getValue(),
                (String) ds.child("displayName").getValue(), (String) ds.child("profileUri").getValue(), (String) ds.child("uid").getValue());
    }
}
