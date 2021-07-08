package com.gathering.friends.models;

import java.io.Serializable;

public class User implements Serializable {
    String username;
    String email;
    String displayName;
    String profileUri;
    String uid;
    String fcm_token;
    boolean isLoggedIn = false;
    String connectionStatus;
    String description;

    // this will be used for local use only :
    // when this user is visible in some other users connection chat list then this field stores their common room id
    String roomId;

    public String getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(String connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public User(String username) {
        this.username = username;
    }

    public User(String username, String email, String displayName, String profileUri, String uid, String description) {
        this.username = username;
        this.email = email;
        this.displayName = displayName;
        this.profileUri = profileUri;
        this.uid = uid;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }

    public String getFcm_token() {
        return fcm_token;
    }

    public void setFcm_token(String fcm_token) {
        this.fcm_token = fcm_token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getProfileUri() {
        return profileUri;
    }

    public void setProfileUri(String profileUri) {
        this.profileUri = profileUri;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
