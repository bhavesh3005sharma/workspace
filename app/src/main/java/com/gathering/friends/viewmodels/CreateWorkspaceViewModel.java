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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class CreateWorkspaceViewModel extends ViewModel {
    public LiveData<String> createWorkspace(String name, String description, Context context) {
        MutableLiveData<String> mutableLiveData = new MutableLiveData<>();

        // create group room with given details
        DatabaseReference databaseReferenceRoom = FirebaseDatabase.getInstance().getReference();
        String roomId = databaseReferenceRoom.child("rooms").push().getKey();

        Room room = new Room(roomId, name, description, null);
        room.setRoomType(Constants.GROUP_ROOM);

        HashMap<String, String> data = new HashMap<>();
        data.put(Prefs.getUser(context).getUsername(), roomId);
        room.setParticipants(data);

        assert roomId != null;
        databaseReferenceRoom.child("rooms").child(roomId).child("details").setValue(room).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // room has been created save its id in user db
                if (task.isSuccessful()) {
                    databaseReferenceRoom.child("users").child(Prefs.getUser(context).getUsername())
                            .child("workspace").child(roomId).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                                mutableLiveData.setValue(Constants.SUCCESS);
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
}
