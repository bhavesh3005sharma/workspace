package com.gathering.friends.viewmodels;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.gathering.friends.database.Prefs;
import com.gathering.friends.models.Room;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WorkspaceViewModel extends ViewModel {
    MutableLiveData<List<Room>> workspaceConnected = new MutableLiveData<>();

    public MutableLiveData<List<Room>> workspaceConnected() {
        return workspaceConnected;
    }

    public void syncUserWorkspace(Context context) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(Prefs.getUser(context).getUsername()).child("workspace");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.i("TAG", "syncUserWorkspace onDataChange: " + snapshot);
                loadDataOfWorkspace(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadDataOfWorkspace(DataSnapshot snap) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("rooms");
        List<Room> roomsData = new ArrayList<>();
        Log.i("TAG", "loadDataOfWorkspace: " + snap);
        final int[] count = {0};
        for (DataSnapshot ds : snap.getChildren()) {
            databaseReference.child(ds.getKey()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.i("TAG", "loadDataOfWorkspace onDataChange: " + count[0] + " " + snapshot);
                    Room room = getRoomModelFromDS(snapshot.child("details"));
                    roomsData.add(room);

                    // check have we fetched the data for all users?
                    if (++count[0] >= snap.getChildrenCount()) {
                        Log.i("TAG", "loadDataOfWorkspace onDataChange: posted value " + count[0]);
                        workspaceConnected.postValue(roomsData);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private Room getRoomModelFromDS(DataSnapshot ds) {
        return new Room((String) ds.child("roomId").getValue(), (String) ds.child("roomName").getValue(), (String) ds.child("roomDescription").getValue(),
                (String) ds.child("photoUri").getValue(), (String) ds.child("roomType").getValue(), (HashMap<String, String>) ds.child("participants").getValue());
    }
}
