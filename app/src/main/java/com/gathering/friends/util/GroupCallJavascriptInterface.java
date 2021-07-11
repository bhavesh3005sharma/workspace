package com.gathering.friends.util;

import com.gathering.friends.activities.GroupCallActivity;

public class GroupCallJavascriptInterface {
    GroupCallActivity groupCallActivity;

    public GroupCallJavascriptInterface(GroupCallActivity groupCallActivity) {
        this.groupCallActivity = groupCallActivity;
    }

    @android.webkit.JavascriptInterface
    public void onRoomJoined() {
        groupCallActivity.onRoomJoined();
    }

    @android.webkit.JavascriptInterface
    public void onCallSetUpError(String error) {
        groupCallActivity.onCallSetUpError(error);
    }
}
