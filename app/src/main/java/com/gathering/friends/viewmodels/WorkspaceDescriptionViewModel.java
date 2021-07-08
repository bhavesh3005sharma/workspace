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

public class WorkspaceDescriptionViewModel extends ViewModel {
    DatabaseReference databaseReferenceRoom = FirebaseDatabase.getInstance().getReference().child("rooms");
    DatabaseReference databaseReferenceUser = FirebaseDatabase.getInstance().getReference().child("users");

    public LiveData<Room> checkRoomUpdates(String roomId) {
        MutableLiveData<Room> mutableLiveData = new MutableLiveData();
        databaseReferenceRoom.child(roomId).child("details").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Room room = getRoomModelFromDS(snapshot);
                mutableLiveData.setValue(room);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return mutableLiveData;
    }

    private Room getRoomModelFromDS(DataSnapshot ds) {
        return new Room((String) ds.child("roomId").getValue(), (String) ds.child("roomName").getValue(), (String) ds.child("roomDescription").getValue(),
                (String) ds.child("photoUri").getValue(), (String) ds.child("roomType").getValue(), (HashMap<String, String>) ds.child("participants").getValue());
    }

    public void updateRoomName(String new_text, String roomId) {
        databaseReferenceRoom.child(roomId).child("details").child("roomName").setValue(new_text);
    }

    public void updateRoomDescription(String new_text, String roomId) {
        databaseReferenceRoom.child(roomId).child("details").child("roomDescription").setValue(new_text);
    }

    public LiveData<List<String>> getUserConnections(Context context) {
        MutableLiveData<List<String>> mutableLiveData = new MutableLiveData<>();
        databaseReferenceUser.child(Prefs.getUser(context).getUsername()).child("connections")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<String> list = new ArrayList<>();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            list.add(ds.getKey());
                        }
                        mutableLiveData.setValue(list);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        mutableLiveData.setValue(null);
                    }
                });
        return mutableLiveData;
    }

    public LiveData<String> addParticipant(String roomId, String[] selectedParticipant) {
        MutableLiveData<String> result = new MutableLiveData<>();
        databaseReferenceRoom.child(roomId).child("details").child("participants").child(selectedParticipant[0]).setValue(roomId)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        databaseReferenceUser.child(selectedParticipant[0]).child("workspace").child(roomId).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) result.setValue(Constants.SUCCESS);
                                else result.setValue(Constants.Error);
                            }
                        });
                    }
                });
        return result;
    }

    public LiveData<String> leftWorkspace(String roomId, Context context) {
        MutableLiveData<String> mutableLiveData = new MutableLiveData<>();
        String username = Prefs.getUser(context).getUsername();
        databaseReferenceRoom.child(roomId).child("details").child("participants").child(username).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        databaseReferenceUser.child(username).child("workspace").child(roomId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful())
                                    mutableLiveData.setValue(Constants.SUCCESS);
                                else mutableLiveData.setValue(Constants.Error);
                            }
                        });
                    }
                });
        return mutableLiveData;
    }
}
