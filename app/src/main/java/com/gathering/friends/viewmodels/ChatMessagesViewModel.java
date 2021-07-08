package com.gathering.friends.viewmodels;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.gathering.friends.database.Prefs;
import com.gathering.friends.models.ChatMessage;
import com.gathering.friends.models.Room;
import com.gathering.friends.models.User;
import com.gathering.friends.util.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ChatMessagesViewModel extends ViewModel {

    /*
    public LiveData<List<ChatMessage>> chatMessages(String roomID) {
        MutableLiveData<List<ChatMessage>> chats = new MutableLiveData<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("rooms").child(roomID).child("chats");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<ChatMessage> list = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ChatMessage message = getChatFromDS(ds);
                    list.add(message);
                }
                chats.setValue(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return chats;
    }
     */

    private ChatMessage getChatFromDS(DataSnapshot ds) {
        String replyId = (String) ds.child("reply_id").getValue();
        String sender_username = (String) ds.child("sender_username").getValue();
        String message = (String) ds.child("message").getValue();
        long timeStamp = 0;
        Map<String, Long> map = new HashMap();
        if (ds.child("timeStampMap").child("timeStamp").exists()) {
            timeStamp = (long) ds.child("timeStampMap").child("timeStamp").getValue();
            map.put("timeStamp", timeStamp);
        }
        return new ChatMessage(replyId, sender_username, message, map);
    }

    public LiveData<ChatMessage> listenForNeMessages(String roomID) {
        MutableLiveData<ChatMessage> chatMessage = new MutableLiveData<>();
        Query query = FirebaseDatabase.getInstance().getReference().child("rooms").child(roomID).child("chats");

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot ds, @Nullable String s) {
                chatMessage.setValue(getChatFromDS(ds));
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return chatMessage;
    }

    public void sendMessage(Context context, String message, String roomID) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("rooms").child(roomID).child("chats");
        String replyId = databaseReference.push().getKey();
        Map map = new HashMap();
        map.put("timeStamp", ServerValue.TIMESTAMP);
        ChatMessage reply = new ChatMessage(replyId, Prefs.getUser(context).getUsername(), message, map);
        databaseReference.child(replyId).setValue(reply).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    User user = Prefs.getUser(context);
                    // TODO : send push notification to the user
                } else
                    Log.i("ChatMessageViewModel", "Send Message Not Sent- Error : " + task.getException().getMessage());
            }
        });
    }

    public LiveData<Room> roomDetails(String roomID, Context context) {
        MutableLiveData<Room> roomDetails = new MutableLiveData<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("rooms").child(roomID);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot ds) {
                if (ds.child("details").hasChild("roomType") && Objects.equals(ds.child("details").child("roomType").getValue(), Constants.GROUP_ROOM)) {
                    String roomName = (String) ds.child("details").child("roomName").getValue();
                    String roomDescription = (String) ds.child("details").child("roomDescription").getValue();
                    String roomPhotoUri = (String) ds.child("details").child("photoUri").getValue();
                    String roomType = (String) ds.child("details").child("roomType").getValue();
                    Room room = new Room(roomID, roomName, roomDescription, roomPhotoUri, roomType, null);
                    roomDetails.setValue(room);
                } else if (ds.child("details").hasChild("roomType") && Objects.equals(ds.child("details").child("roomType").getValue(), Constants.DUO_ROOM)) {
                    // for duo room it is a private chat activity opposite user is single participant
                    // room details = opposite person's details
                    String oppositePerson = null;
                    for (DataSnapshot d : ds.child("details").child("participants").getChildren()) {
                        if (d.getKey().equals(Prefs.getUser(context).getUsername())) continue;
                        oppositePerson = d.getKey();
                        break;
                    }

                    FirebaseDatabase.getInstance().getReference().child("users").child(oppositePerson).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String roomName = (String) snapshot.child("displayName").getValue();
                            String roomDescription = "@" + snapshot.child("username").getValue();
                            String roomPhotoUri = (String) snapshot.child("profileUri").getValue();
                            String roomType = Constants.DUO_ROOM;
                            Room room = new Room(roomID, roomName, roomDescription, roomPhotoUri, roomType, null);
                            roomDetails.setValue(room);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return roomDetails;
    }
}
