package com.mopub.mobileads;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import com.applovin.adview.AppLovinIncentivizedInterstitial;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdClickListener;
import com.applovin.sdk.AppLovinAdDisplayListener;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinAdRewardListener;
import com.applovin.sdk.AppLovinAdVideoPlaybackListener;
import com.applovin.sdk.AppLovinPrivacySettings;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkSettings;
import com.mopub.common.LifecycleListener;
import com.mopub.common.MoPub;
import com.mopub.common.MoPubReward;
import com.mopub.common.logging.MoPubLog;
import java.util.HashMap;
import java.util.Map;

public class AppLovinRewardedVideo extends CustomEventRewardedVideo implements AppLovinAdLoadListener, AppLovinAdDisplayListener, AppLovinAdClickListener, AppLovinAdVideoPlaybackListener, AppLovinAdRewardListener {
    private static final String DEFAULT_ZONE = "";
    private static final String DEFAULT_TOKEN_ZONE = "token";
    private static final String ZONE_ID_SERVER_EXTRAS_KEY = "zone_id";
    private static final Map<String, AppLovinIncentivizedInterstitial> GLOBAL_INCENTIVIZED_INTERSTITIAL_ADS = new HashMap();
    private boolean initialized;
    private AppLovinSdk sdk;
    private AppLovinIncentivizedInterstitial incentivizedInterstitial;
    private Activity parentActivity;
    private boolean fullyWatched;
    private MoPubReward reward;
    private boolean isTokenEvent;
    private AppLovinAd tokenAd;
    private String serverExtrasZoneId = "";

    public AppLovinRewardedVideo() {
    }

    protected boolean checkAndInitializeSdk(@NonNull Activity activity, @NonNull Map<String, Object> localExtras, @NonNull Map<String, String> serverExtras) throws Exception {
        boolean canCollectPersonalInfo = MoPub.canCollectPersonalInformation();
        AppLovinPrivacySettings.setHasUserConsent(canCollectPersonalInfo, activity.getApplicationContext());
        MoPubLog.d("Initializing AppLovin rewarded video...");
        if (!this.initialized) {
            this.sdk = retrieveSdk(serverExtras, activity);
            this.sdk.setPluginVersion("MoPub-3.1.0");
            this.sdk.setMediationProvider("mopub");
            this.initialized = true;
            return true;
        } else {
            return false;
        }
    }

    protected void loadWithSdkInitialized(@NonNull Activity activity, @NonNull Map<String, Object> localExtras, @NonNull Map<String, String> serverExtras) throws Exception {
        this.parentActivity = activity;
        String adMarkup = (String)serverExtras.get("adm");
        boolean hasAdMarkup = !TextUtils.isEmpty(adMarkup);
        MoPubLog.d("Requesting AppLovin banner with serverExtras: " + serverExtras + ", localExtras: " + localExtras + " and has ad markup: " + hasAdMarkup);
        String zoneId;
        if (hasAdMarkup) {
            zoneId = "token";
        } else if (!TextUtils.isEmpty((CharSequence)serverExtras.get("zone_id"))) {
            this.serverExtrasZoneId = (String)serverExtras.get("zone_id");
            zoneId = this.serverExtrasZoneId;
        } else {
            zoneId = "";
        }

        this.incentivizedInterstitial = createIncentivizedInterstitialAd(zoneId, activity, this.sdk);
        if (hasAdMarkup) {
            this.isTokenEvent = true;
            this.sdk.getAdService().loadNextAdForAdToken(adMarkup, this);
        } else {
            this.incentivizedInterstitial.preload(this);
        }

    }

    protected void showVideo() {
        if (this.hasVideoAvailable()) {
            this.fullyWatched = false;
            this.reward = null;
            if (this.isTokenEvent) {
                this.incentivizedInterstitial.show(this.tokenAd, this.parentActivity, this, this, this, this);
            } else {
                this.incentivizedInterstitial.show(this.parentActivity, (String)null, this, this, this, this);
            }
        } else {
            MoPubLog.d("Failed to show an AppLovin rewarded video before one was loaded");
            MoPubRewardedVideoManager.onRewardedVideoPlaybackError(this.getClass(), this.getAdNetworkId(), MoPubErrorCode.VIDEO_PLAYBACK_ERROR);
        }

    }

    protected boolean hasVideoAvailable() {
        if (this.isTokenEvent) {
            return this.tokenAd != null;
        } else {
            return this.incentivizedInterstitial != null && this.incentivizedInterstitial.isAdReadyToDisplay();
        }
    }

    @Nullable
    protected LifecycleListener getLifecycleListener() {
        return null;
    }

    @NonNull
    protected String getAdNetworkId() {
        return this.serverExtrasZoneId;
    }

    protected void onInvalidate() {
    }

    public void adReceived(AppLovinAd ad) {
        MoPubLog.d("Rewarded video did load ad: " + ad.getAdIdNumber());
        if (this.isTokenEvent) {
            this.tokenAd = ad;
        }

        this.parentActivity.runOnUiThread(new Runnable() {
            public void run() {
                try {
                    MoPubRewardedVideoManager.onRewardedVideoLoadSuccess(AppLovinRewardedVideo.this.getClass(), AppLovinRewardedVideo.this.getAdNetworkId());
                } catch (Throwable var2) {
                    MoPubLog.e("Unable to notify listener of successful ad load.", var2);
                }

            }
        });
    }

    public void failedToReceiveAd(final int errorCode) {
        MoPubLog.d("Rewarded video failed to load with error: " + errorCode);
        this.parentActivity.runOnUiThread(new Runnable() {
            public void run() {
                try {
                    MoPubRewardedVideoManager.onRewardedVideoLoadFailure(AppLovinRewardedVideo.this.getClass(), AppLovinRewardedVideo.this.getAdNetworkId(), AppLovinRewardedVideo.toMoPubErrorCode(errorCode));
                } catch (Throwable var2) {
                    MoPubLog.e("Unable to notify listener of failure to receive ad.", var2);
                }

            }
        });
    }

    public void adDisplayed(AppLovinAd ad) {
        MoPubLog.d("Rewarded video displayed");
        MoPubRewardedVideoManager.onRewardedVideoStarted(this.getClass(), this.getAdNetworkId());
    }

    public void adHidden(AppLovinAd ad) {
        MoPubLog.d("Rewarded video dismissed");
        if (this.fullyWatched && this.reward != null) {
            MoPubLog.d("Rewarded" + this.reward.getAmount() + " " + this.reward.getLabel());
            MoPubRewardedVideoManager.onRewardedVideoCompleted(this.getClass(), this.getAdNetworkId(), this.reward);
        }

        MoPubRewardedVideoManager.onRewardedVideoClosed(this.getClass(), this.getAdNetworkId());
    }

    public void adClicked(AppLovinAd ad) {
        MoPubLog.d("Rewarded video clicked");
        MoPubRewardedVideoManager.onRewardedVideoClicked(this.getClass(), this.getAdNetworkId());
    }

    public void videoPlaybackBegan(AppLovinAd ad) {
        MoPubLog.d("Rewarded video playback began");
    }

    public void videoPlaybackEnded(AppLovinAd ad, double percentViewed, boolean fullyWatched) {
        MoPubLog.d("Rewarded video playback ended at playback percent: " + percentViewed);
        this.fullyWatched = fullyWatched;
    }

    public void userOverQuota(AppLovinAd appLovinAd, Map map) {
        MoPubLog.d("Rewarded video validation request for ad did exceed quota with response: " + map);
    }

    public void validationRequestFailed(AppLovinAd appLovinAd, int errorCode) {
        MoPubLog.d("Rewarded video validation request for ad failed with error code: " + errorCode);
    }

    public void userRewardRejected(AppLovinAd appLovinAd, Map map) {
        MoPubLog.d("Rewarded video validation request was rejected with response: " + map);
    }

    public void userDeclinedToViewAd(AppLovinAd appLovinAd) {
        MoPubLog.d("User declined to view rewarded video");
        MoPubRewardedVideoManager.onRewardedVideoClosed(this.getClass(), this.getAdNetworkId());
    }

    public void userRewardVerified(AppLovinAd appLovinAd, Map map) {
        String currency = (String)map.get("currency");
        int amount = (int)Double.parseDouble((String)map.get("amount"));
        MoPubLog.d("Verified " + amount + " " + currency);
        this.reward = MoPubReward.success(currency, amount);
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

    private static AppLovinIncentivizedInterstitial createIncentivizedInterstitialAd(String zoneId, Activity activity, AppLovinSdk sdk) {
        AppLovinIncentivizedInterstitial incent;
        if (GLOBAL_INCENTIVIZED_INTERSTITIAL_ADS.containsKey(zoneId)) {
            incent = (AppLovinIncentivizedInterstitial)GLOBAL_INCENTIVIZED_INTERSTITIAL_ADS.get(zoneId);
        } else {
            if (!"".equals(zoneId) && !"token".equals(zoneId)) {
                incent = AppLovinIncentivizedInterstitial.create(zoneId, sdk);
            } else {
                incent = AppLovinIncentivizedInterstitial.create(activity);
            }

            GLOBAL_INCENTIVIZED_INTERSTITIAL_ADS.put(zoneId, incent);
        }

        return incent;
    }
}
