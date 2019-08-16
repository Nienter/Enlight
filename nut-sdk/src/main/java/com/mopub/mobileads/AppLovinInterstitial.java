package com.mopub.mobileads;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import com.applovin.adview.AppLovinInterstitialAd;
import com.applovin.adview.AppLovinInterstitialAdDialog;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdClickListener;
import com.applovin.sdk.AppLovinAdDisplayListener;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinAdSize;
import com.applovin.sdk.AppLovinAdVideoPlaybackListener;
import com.applovin.sdk.AppLovinPrivacySettings;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkSettings;
import com.mopub.common.MoPub;
import com.mopub.common.logging.MoPubLog;
import com.mopub.mobileads.CustomEventInterstitial.CustomEventInterstitialListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class AppLovinInterstitial extends CustomEventInterstitial implements AppLovinAdLoadListener, AppLovinAdDisplayListener, AppLovinAdClickListener, AppLovinAdVideoPlaybackListener {
    private static final String DEFAULT_ZONE = "";
    private static final String ZONE_ID_SERVER_EXTRAS_KEY = "zone_id";
    private static final Handler UI_HANDLER = new Handler(Looper.getMainLooper());
    private AppLovinSdk sdk;
    private CustomEventInterstitialListener listener;
    private Context context;
    private static final Map<String, Queue<AppLovinAd>> GLOBAL_INTERSTITIAL_ADS = new HashMap();
    private static final Object GLOBAL_INTERSTITIAL_ADS_LOCK = new Object();
    private String zoneId;
    private boolean isTokenEvent;
    private AppLovinAd tokenAd;

    public AppLovinInterstitial() {
    }

    public void loadInterstitial(Context context, CustomEventInterstitialListener listener, Map<String, Object> localExtras, Map<String, String> serverExtras) {
        boolean canCollectPersonalInfo = MoPub.canCollectPersonalInformation();
        AppLovinPrivacySettings.setHasUserConsent(canCollectPersonalInfo, context);
        this.listener = listener;
        this.context = context;
        this.sdk = retrieveSdk(serverExtras, context);
        this.sdk.setPluginVersion("MoPub-3.1.0");
        this.sdk.setMediationProvider("mopub");
        String adMarkup = (String)serverExtras.get("adm");
        boolean hasAdMarkup = !TextUtils.isEmpty(adMarkup);
        MoPubLog.d("Requesting AppLovin interstitial with serverExtras: " + serverExtras + ", localExtras: " + localExtras + " and has adMarkup: " + hasAdMarkup);
        if (hasAdMarkup) {
            this.isTokenEvent = true;
            this.sdk.getAdService().loadNextAdForAdToken(adMarkup, this);
        } else {
            String serverExtrasZoneId = (String)serverExtras.get("zone_id");
            this.zoneId = !TextUtils.isEmpty(serverExtrasZoneId) ? serverExtrasZoneId : "";
            AppLovinAd preloadedAd = dequeueAd(this.zoneId);
            if (preloadedAd != null) {
                MoPubLog.d("Found preloaded ad for zone: {" + this.zoneId + "}");
                this.adReceived(preloadedAd);
            } else if (!TextUtils.isEmpty(this.zoneId)) {
                this.sdk.getAdService().loadNextAdForZoneId(this.zoneId, this);
            } else {
                this.sdk.getAdService().loadNextAd(AppLovinAdSize.INTERSTITIAL, this);
            }
        }

    }

    public void showInterstitial() {
        AppLovinAd preloadedAd;
        if (this.isTokenEvent && this.tokenAd != null) {
            preloadedAd = this.tokenAd;
        } else {
            preloadedAd = dequeueAd(this.zoneId);
        }

        if (preloadedAd != null) {
            AppLovinInterstitialAdDialog interstitialAd = AppLovinInterstitialAd.create(this.sdk, this.context);
            interstitialAd.setAdDisplayListener(this);
            interstitialAd.setAdClickListener(this);
            interstitialAd.setAdVideoPlaybackListener(this);
            interstitialAd.showAndRender(preloadedAd);
        } else {
            MoPubLog.d("Failed to show an AppLovin interstitial before one was loaded");
            if (this.listener != null) {
                this.listener.onInterstitialFailed(MoPubErrorCode.NETWORK_INVALID_STATE);
            }
        }

    }

    public void onInvalidate() {
    }

    public void adReceived(AppLovinAd ad) {
        MoPubLog.d("Interstitial did load ad: " + ad.getAdIdNumber());
        if (this.isTokenEvent) {
            this.tokenAd = ad;
        } else {
            enqueueAd(ad, this.zoneId);
        }

        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    if (AppLovinInterstitial.this.listener != null) {
                        AppLovinInterstitial.this.listener.onInterstitialLoaded();
                    }
                } catch (Throwable var2) {
                    MoPubLog.e("Unable to notify listener of successful ad load.", var2);
                }

            }
        });
    }

    public void failedToReceiveAd(final int errorCode) {
        MoPubLog.d("Interstitial failed to load with error: " + errorCode);
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    if (AppLovinInterstitial.this.listener != null) {
                        AppLovinInterstitial.this.listener.onInterstitialFailed(AppLovinInterstitial.toMoPubErrorCode(errorCode));
                    }
                } catch (Throwable var2) {
                    MoPubLog.e("Unable to notify listener of failure to receive ad.", var2);
                }

            }
        });
    }

    public void adDisplayed(AppLovinAd appLovinAd) {
        MoPubLog.d("Interstitial displayed");
        if (this.listener != null) {
            this.listener.onInterstitialShown();
        }

    }

    public void adHidden(AppLovinAd appLovinAd) {
        MoPubLog.d("Interstitial dismissed");
        if (this.listener != null) {
            this.listener.onInterstitialDismissed();
        }

    }

    public void adClicked(AppLovinAd appLovinAd) {
        MoPubLog.d("Interstitial clicked");
        if (this.listener != null) {
            this.listener.onLeaveApplication();
        }

    }

    public void videoPlaybackBegan(AppLovinAd ad) {
        MoPubLog.d("Interstitial video playback began");
    }

    public void videoPlaybackEnded(AppLovinAd ad, double percentViewed, boolean fullyWatched) {
        MoPubLog.d("Interstitial video playback ended at playback percent: " + percentViewed);
    }

    private static AppLovinAd dequeueAd(String zoneId) {
        Object var1 = GLOBAL_INTERSTITIAL_ADS_LOCK;
        synchronized(GLOBAL_INTERSTITIAL_ADS_LOCK) {
            AppLovinAd preloadedAd = null;
            Queue<AppLovinAd> preloadedAds = (Queue)GLOBAL_INTERSTITIAL_ADS.get(zoneId);
            if (preloadedAds != null && !preloadedAds.isEmpty()) {
                preloadedAd = (AppLovinAd)preloadedAds.poll();
            }

            return preloadedAd;
        }
    }

    private static void enqueueAd(AppLovinAd ad, String zoneId) {
        Object var2 = GLOBAL_INTERSTITIAL_ADS_LOCK;
        synchronized(GLOBAL_INTERSTITIAL_ADS_LOCK) {
            Queue<AppLovinAd> preloadedAds = (Queue)GLOBAL_INTERSTITIAL_ADS.get(zoneId);
            if (preloadedAds == null) {
                preloadedAds = new LinkedList();
                GLOBAL_INTERSTITIAL_ADS.put(zoneId, preloadedAds);
            }

            ((Queue)preloadedAds).offer(ad);
        }
    }

    private static MoPubErrorCode toMoPubErrorCode(int applovinErrorCode) {
        if (applovinErrorCode == 204) {
            return MoPubErrorCode.NETWORK_NO_FILL;
        } else if (applovinErrorCode == -1) {
            return MoPubErrorCode.UNSPECIFIED;
        } else if (applovinErrorCode == -103) {
            return MoPubErrorCode.NO_CONNECTION;
        } else {
            return applovinErrorCode == -102 ? MoPubErrorCode.NETWORK_TIMEOUT : MoPubErrorCode.UNSPECIFIED;
        }
    }

    private static AppLovinSdk retrieveSdk(Map<String, String> serverExtras, Context context) {
        String sdkKey = serverExtras != null ? (String)serverExtras.get("sdk_key") : null;
        AppLovinSdk sdk;
        if (!TextUtils.isEmpty(sdkKey)) {
            sdk = AppLovinSdk.getInstance(sdkKey, new AppLovinSdkSettings(), context);
        } else {
            sdk = AppLovinSdk.getInstance(context);
        }

        return sdk;
    }

    private static void runOnUiThread(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            UI_HANDLER.post(runnable);
        }

    }
}
