package com.gathering.friends.util;

import com.squareup.okhttp.MediaType;

public class Constants {
    public static final String MY_PREFS_NAME = "PREFS_FOR_WORKSPACE-2021";
    public static final String LEGACY_SERVER_KEY = "AAAA9v4fZSY:APA91bE3gv-a_das291k-FC_Lmosr6RFcWdZ48iUJn3aSe-MiRXVbb4b2ILRcebKdo4stvHy0gz69utB6ZOlAHEyFAxQV7kgbNp-PzMytqmZe-n3Eqtq1KWC_CaGvkcSScBzkhmhGAm8";

    public static String SUCCESS = "SUCCESS";
    public static final String GROUP_ROOM = "GROUP_ROOM";
    public static String DUPLICATE = "DUPLICATE";

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    // user type as caller or receiver
    public static String CALLER = "CALLER";
    // notification channel details
    public static final String WORKSPACE_CHANNEL_ID = "WORKSPACE-NOTIFICATION-CHANNEL-2021";

    // call status strings
    public static final String CALL_PICKED = "CALL_PICKED";
    public static String CALL_RECEIVER = "CALL_RECEIVER";
    public static final String MEETING_ROOM = "MEETING_ROOM";

    // types of room
    public static final String DUO_ROOM = "DUO_ROOM";
    public static String Error = "Error";
    public static String REJECTED = "REJECTED";

    // users connection status
    public static String CONNECTED = "CONNECTED";
    public static String REQUEST_RECEIVED = "REQUEST_RECEIVED";
    public static String REQUEST_SENT = "REQUEST_SENT";
    public static String UNKNOWN = "UNKNOWN";
    public static String BUSY = "BUSY";
    public static final String WORKSPACE_CHANNEL_NAME = "WORKSPACE-NOTIFICATION-CHANNEL";
    public static final String WORKSPACE_CHANNEL_DESCRIPTION = "This channel handle all notifications coming from Workspace.";
    // meeting joining or creating keys
    public static String MEET_TYPE = "MEET_TYPE";
    public static String JOIN_MEET = "JOIN_MEET";
    public static String CREATE_MEET = "CREATE_MEET";
    public static String ROOM_ID = "ROOM_ID";
}
