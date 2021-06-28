package com.gathering.friends.util;

import com.gathering.friends.activities.CallActivity;

public class JavascriptInterface {
    CallActivity callActivity;

    public JavascriptInterface(CallActivity callActivity) {
        this.callActivity = callActivity;
    }

    @android.webkit.JavascriptInterface
    public void onPeerConnected() {
        callActivity.onPeerConnected();
    }
}
