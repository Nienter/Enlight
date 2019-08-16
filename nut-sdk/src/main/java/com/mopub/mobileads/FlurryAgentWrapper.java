package com.mopub.mobileads;

import android.content.Context;
import android.os.Build.VERSION;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.android.pixel.AppUtil;
import com.flurry.android.FlurryAgent;
import com.flurry.android.FlurryAgentListener;
import com.flurry.android.FlurryAgent.Builder;

public final class FlurryAgentWrapper {
    public static final String PARAM_API_KEY = "apiKey";
    public static final String PARAM_AD_SPACE_NAME = "adSpaceName";
    private static final String ORIGIN_IDENTIFIER = "Flurry_Mopub_Android";
    private static final String ORIGIN_VERSION = "6.5.0";
    private Builder mAgentBuilder;

    public static FlurryAgentWrapper getInstance() {
        return FlurryAgentLoader.INSTANCE;
    }

    private FlurryAgentWrapper() {
        this.mAgentBuilder = (new Builder()).withLogEnabled(AppUtil.sDebug).withLogLevel(Log.VERBOSE);
        FlurryAgent.addOrigin("Flurry_Mopub_Android", "6.5.0");
    }

    public synchronized void startSession(@NonNull Context context, String apiKey, @Nullable FlurryAgentListener flurryAgentListener) {
        if (!TextUtils.isEmpty(apiKey)) {
            if (!FlurryAgent.isSessionActive()) {
                this.mAgentBuilder.withListener(flurryAgentListener).build(context, apiKey);
                if (VERSION.SDK_INT >= 14) {
                    return;
                }

                FlurryAgent.onStartSession(context);
            }

        }
    }

    public synchronized void endSession(Context context) {
        if (context != null) {
            if (FlurryAgent.isSessionActive()) {
                if (VERSION.SDK_INT >= 14) {
                    return;
                }

                FlurryAgent.onEndSession(context);
            }

        }
    }

    public synchronized boolean isSessionActive() {
        return FlurryAgent.isSessionActive();
    }

    private static class FlurryAgentLoader {
        private static final FlurryAgentWrapper INSTANCE = new FlurryAgentWrapper();

        private FlurryAgentLoader() {
        }
    }
}
