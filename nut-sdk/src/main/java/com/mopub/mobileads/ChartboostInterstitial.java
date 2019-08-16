package com.mopub.mobileads;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.android.pixel.AppUtil;
import com.chartboost.sdk.Chartboost;
import com.mopub.common.Preconditions;

import java.util.Map;

class ChartboostInterstitial extends CustomEventInterstitial {
    @NonNull
    private String mLocation = "Default";

    ChartboostInterstitial() {
    }

    protected void loadInterstitial(@NonNull Context context, @NonNull CustomEventInterstitialListener interstitialListener, @NonNull Map<String, Object> localExtras, @NonNull Map<String, String> serverExtras) {
        Preconditions.checkNotNull(context);
        Preconditions.checkNotNull(interstitialListener);
        Preconditions.checkNotNull(localExtras);
        Preconditions.checkNotNull(serverExtras);
        if (!(context instanceof Activity)) {
            interstitialListener.onInterstitialFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
        } else {
            if (serverExtras.containsKey("location")) {
                String location = (String)serverExtras.get("location");
                this.mLocation = TextUtils.isEmpty(location) ? this.mLocation : location;
            }

            if (ChartboostShared.getDelegate().hasInterstitialLocation(this.mLocation) && ChartboostShared.getDelegate().getInterstitialListener(this.mLocation) != interstitialListener) {
                interstitialListener.onInterstitialFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
            } else {
                Activity activity = (Activity)context;

                try {
                    ChartboostShared.initializeSdk(activity, serverExtras);
                    ChartboostShared.getDelegate().registerInterstitialListener(this.mLocation, interstitialListener);
                } catch (NullPointerException var7) {
                    interstitialListener.onInterstitialFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
                    return;
                } catch (IllegalStateException var8) {
                    interstitialListener.onInterstitialFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
                    return;
                }

                Chartboost.onCreate(activity);
                Chartboost.onStart(activity);
                if (Chartboost.hasInterstitial(this.mLocation)) {
                    ChartboostShared.getDelegate().didCacheInterstitial(this.mLocation);
                } else {
                    Chartboost.cacheInterstitial(this.mLocation);
                }

            }
        }
    }

    protected void showInterstitial() {
        if(AppUtil.sDebug) {
            Log.d("MoPub", "Showing Chartboost interstitial ad.");
        }
        Chartboost.showInterstitial(this.mLocation);
    }

    protected void onInvalidate() {
        ChartboostShared.getDelegate().unregisterInterstitialListener(this.mLocation);
    }
}
