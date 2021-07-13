package com.gathering.friends.viewmodels;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.gathering.friends.database.Prefs;
import com.gathering.friends.models.Room;
import com.gathering.friends.util.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MeetsViewModel extends ViewModel {
    MutableLiveData<List<Room>> roomsConnected = new MutableLiveData<>();

    public MutableLiveData<List<Room>> roomsConnected() {
        return roomsConnected;
    }

    public void syncUserMeets(Context context) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(Prefs.getUser(context).getUsername()).child("meetings");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loadDataOfRooms(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadDataOfRooms(DataSnapshot snap) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("meetings");
        List<Room> roomsData = new ArrayList<>();
        if (snap.getChildrenCount() == 0) roomsConnected.postValue(null);
        final int[] count = {0};
        for (DataSnapshot ds : snap.getChildren()) {
            databaseReference.child(ds.getKey()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Room room = getRoomModelFromDS(snapshot.child("details"));
                    if (room.getRoomId() == null) {
                        // check have we fetched the data for all users?
                        if (++count[0] >= snap.getChildrenCount())
                            roomsConnected.postValue(roomsData);

                        return;
                    }

                    // remove old object from list
                    for (Room r : roomsData)
                        if (r.getRoomId().equals(room.getRoomId()))
                            roomsData.remove(r);

                    roomsData.add(room);

                    // check have we fetched the data for all users?
                    if (++count[0] >= snap.getChildrenCount()) {
                        roomsConnected.postValue(roomsData);
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

    public LiveData<String> createNewMeet(String roomName, Context context) {
        MutableLiveData<String> mutableLiveData = new MutableLiveData<>();

        // create group room with given details
        DatabaseReference databaseReferenceRoom = FirebaseDatabase.getInstance().getReference();
        String roomId = databaseReferenceRoom.child("meetings").push().getKey();

        Room room = new Room(roomId, roomName, null, null);
        room.setRoomType(Constants.MEETING_ROOM);

        assert roomId != null;
        databaseReferenceRoom.child("meetings").child(roomId).child("details").setValue(room).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // room has been created save its id in user db
                if (task.isSuccessful()) {
                    databaseReferenceRoom.child("users").child(Prefs.getUser(context).getUsername())
                            .child("meetings").child(roomId).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                                mutableLiveData.setValue(roomId);
                            else mutableLiveData.setValue(null);
                        }
                    });
                } else {
                    mutableLiveData.setValue(null);
                }
            }
        });
        return mutableLiveData;
    }

    public void saveMeetHistory(String meetId, Context context) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("users").child(Prefs.getUser(context).getUsername()).child("meetings").child(meetId).setValue(true);
    }
}
