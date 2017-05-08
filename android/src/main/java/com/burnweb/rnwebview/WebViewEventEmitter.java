package com.burnweb.rnwebview;

import android.support.annotation.StringDef;
import android.view.View;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

class WebViewEventEmitter {

    private final RCTEventEmitter eventEmitter;

    private int viewId = View.NO_ID;

    WebViewEventEmitter(ReactContext reactContext) {
        this.eventEmitter = reactContext.getJSModule(RCTEventEmitter.class);
    }

    private static final String EVENT_FULL_SCREEN = "onFullScreen";

    static final String[] Events = {
            EVENT_FULL_SCREEN,
    };

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            EVENT_FULL_SCREEN,
    })
    @interface WebViewEvents {
    }

    private static final String EVENT_PROP_ISFULLSCREEN = "isFullScreen";

    void setViewId(int viewId) {
        this.viewId = viewId;
    }


    void onFullScreen(boolean isFullScreen) {
        WritableMap event = Arguments.createMap();
        event.putBoolean(EVENT_PROP_ISFULLSCREEN, isFullScreen);
        receiveEvent(EVENT_FULL_SCREEN, event);
    }



    private void receiveEvent(@WebViewEvents String type, WritableMap event) {
        eventEmitter.receiveEvent(viewId, type, event);
    }
}
