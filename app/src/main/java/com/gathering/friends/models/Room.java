package com.gathering.friends.models;

import java.util.HashMap;

public class Room {
    String roomId;
    String roomName;
    String roomDescription;
    String photoUri;
    String roomType;
    HashMap<String, String> participants;

    public Room(String roomId, String roomName, String roomDescription, String photoUri, String roomType, HashMap<String, String> participants) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.roomDescription = roomDescription;
        this.photoUri = photoUri;
        this.roomType = roomType;
        this.participants = participants;
    }

    public Room(String roomId, String roomName, String roomDescription, String photoUri) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.roomDescription = roomDescription;
        this.photoUri = photoUri;
    }

    public Room(String roomType, HashMap<String, String> participants) {
        this.roomType = roomType;
        this.participants = participants;
    }

    public HashMap<String, String> getParticipants() {
        return participants;
    }

    public void setParticipants(HashMap<String, String> participants) {
        this.participants = participants;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomDescription() {
        return roomDescription;
    }

    public void setRoomDescription(String roomDescription) {
        this.roomDescription = roomDescription;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }
}
