package com.gathering.friends.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.gathering.friends.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserViewModel extends ViewModel {
    DatabaseReference databaseReferenceUser = FirebaseDatabase.getInstance().getReference().child("users");

    public LiveData<User> getUser(String userName) {
        MutableLiveData<User> mutableLiveData = new MutableLiveData<>();
        databaseReferenceUser.child(userName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mutableLiveData.setValue(getUserModelFromDS(snapshot));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                mutableLiveData.setValue(null);
            }
        });
        return mutableLiveData;
    }

    private User getUserModelFromDS(DataSnapshot ds) {
        return new User((String) ds.child("username").getValue(), (String) ds.child("email").getValue(),
                (String) ds.child("displayName").getValue(), (String) ds.child("profileUri").getValue(),
                (String) ds.child("uid").getValue(), (String) ds.child("description").getValue());
    }

    public void updateDisplayName(String new_text, String username) {
        databaseReferenceUser.child(username).child("displayName").setValue(new_text);
    }

    public void updateUserDescription(String new_text, String username) {
        databaseReferenceUser.child(username).child("description").setValue(new_text);
    }
}
