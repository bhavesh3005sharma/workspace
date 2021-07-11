package com.gathering.friends.util;

import com.gathering.friends.activities.CallActivity;

public class DuoCallJavascriptInterface {
    CallActivity callActivity;

    public DuoCallJavascriptInterface(CallActivity callActivity) {
        this.callActivity = callActivity;
    }

    @android.webkit.JavascriptInterface
    public void onPeerConnected() {
        callActivity.onPeerConnected();
    }
}
