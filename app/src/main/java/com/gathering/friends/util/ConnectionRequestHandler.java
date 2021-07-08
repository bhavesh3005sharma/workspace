package com.gathering.friends.util;

import com.gathering.friends.models.Room;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class ConnectionRequestHandler {

    public static void sendConnectionRequest(String from, String to) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // make request available in connection-requests-received section of receiver
        databaseReference.child(to).child("connection-requests-received").child(from).setValue(true);

        // mark request as sent in sender side
        databaseReference.child(from).child("connection-requests-sent").child(to).setValue(true);
    }

    public static void cancelRequest(String to, String from) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");

        databaseReference.child(to).child("connection-requests-received").child(from).removeValue();

        databaseReference.child(from).child("connection-requests-sent").child(to).removeValue();
    }

    public static void acceptRequest(String to, String from) {
        // remove the request from sent and received section of these users
        cancelRequest(to, from);

        DatabaseReference databaseReferenceUser = FirebaseDatabase.getInstance().getReference("users");
        DatabaseReference databaseReferenceRoom = FirebaseDatabase.getInstance().getReference().child("rooms");

        // get a room id where they can connect
        String roomId = databaseReferenceRoom.push().getKey();

        // make user available in their connections and set their room id
        databaseReferenceUser.child(to).child("connections").child(from).setValue(roomId);
        databaseReferenceUser.child(from).child("connections").child(to).setValue(roomId);

        // create a duo room and add these 2 participants
        HashMap<String, String> data = new HashMap<>();
        data.put(from, roomId);
        data.put(to, roomId);

        Room room = new Room(Constants.DUO_ROOM, data);
        databaseReferenceRoom.child(roomId).child("details").setValue(room);
    }
}
