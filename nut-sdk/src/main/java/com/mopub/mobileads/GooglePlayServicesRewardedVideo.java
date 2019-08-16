package com.mopub.mobileads;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.android.pixel.AppUtil;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.mopub.common.BaseLifecycleListener;
import com.mopub.common.LifecycleListener;
import com.mopub.common.MoPubReward;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class GooglePlayServicesRewardedVideo extends CustomEventRewardedVideo implements RewardedVideoAdListener {
    private static final String TAG = "MoPubToAdMobRewarded";
    private static final String ADAPTER_VERSION = "0.1.0";
    private static final String KEY_EXTRA_APPLICATION_ID = "appid";
    private static final String KEY_EXTRA_AD_UNIT_ID = "adunit";
    private static AtomicBoolean sIsInitialized;
    private String mAdUnitId;
    private RewardedVideoAd mRewardedVideoAd;
    private LifecycleListener mLifecycleListener = new BaseLifecycleListener() {
        public void onPause(@NonNull Activity activity) {
            super.onPause(activity);
            if (GooglePlayServicesRewardedVideo.this.mRewardedVideoAd != null) {
                GooglePlayServicesRewardedVideo.this.mRewardedVideoAd.pause(activity);
            }

        }

        public void onResume(@NonNull Activity activity) {
            super.onResume(activity);
            if (GooglePlayServicesRewardedVideo.this.mRewardedVideoAd != null) {
                GooglePlayServicesRewardedVideo.this.mRewardedVideoAd.resume(activity);
            }

        }
    };

    public GooglePlayServicesRewardedVideo() {
        sIsInitialized = new AtomicBoolean(false);
    }

    @Nullable
    protected LifecycleListener getLifecycleListener() {
        return this.mLifecycleListener;
    }

    @NonNull
    protected String getAdNetworkId() {
        return this.mAdUnitId;
    }

    protected void onInvalidate() {
        if (this.mRewardedVideoAd != null) {
            this.mRewardedVideoAd.setRewardedVideoAdListener((RewardedVideoAdListener)null);
            this.mRewardedVideoAd = null;
        }

    }

    protected boolean checkAndInitializeSdk(@NonNull Activity launcherActivity, @NonNull Map<String, Object> localExtras, @NonNull Map<String, String> serverExtras) throws Exception {
        if (!sIsInitialized.getAndSet(true)) {
            if(AppUtil.sDebug)
                Log.i("MoPubToAdMobRewarded", "Adapter version - 0.1.0");
            if (TextUtils.isEmpty((CharSequence)serverExtras.get("appid"))) {
                MobileAds.initialize(launcherActivity);
            } else {
                MobileAds.initialize(launcherActivity, (String)serverExtras.get("appid"));
            }

            if (TextUtils.isEmpty((CharSequence)serverExtras.get("adunit"))) {
                MoPubRewardedVideoManager.onRewardedVideoLoadFailure(GooglePlayServicesRewardedVideo.class, GooglePlayServicesRewardedVideo.class.getSimpleName(), MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
                return false;
            } else {
                this.mAdUnitId = (String)serverExtras.get("adunit");
                this.mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(launcherActivity);
                this.mRewardedVideoAd.setRewardedVideoAdListener(this);
                return true;
            }
        } else {
            return false;
        }
    }

    protected void loadWithSdkInitialized(@NonNull Activity activity, @NonNull Map<String, Object> localExtras, @NonNull Map<String, String> serverExtras) throws Exception {
        if (TextUtils.isEmpty((CharSequence)serverExtras.get("adunit"))) {
            MoPubRewardedVideoManager.onRewardedVideoLoadFailure(GooglePlayServicesRewardedVideo.class, GooglePlayServicesRewardedVideo.class.getSimpleName(), MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
        } else {
            this.mAdUnitId = (String)serverExtras.get("adunit");
            if (this.mRewardedVideoAd == null) {
                this.mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(activity);
                this.mRewardedVideoAd.setRewardedVideoAdListener(this);
            }

            if (this.mRewardedVideoAd.isLoaded()) {
                MoPubRewardedVideoManager.onRewardedVideoLoadSuccess(GooglePlayServicesRewardedVideo.class, this.mAdUnitId);
            } else {
                this.mRewardedVideoAd.loadAd(this.mAdUnitId, (new AdRequest.Builder()).setRequestAgent("MoPub").build());
            }

        }
    }

    protected boolean hasVideoAvailable() {
        return this.mRewardedVideoAd != null && this.mRewardedVideoAd.isLoaded();
    }

    protected void showVideo() {
        if (this.hasVideoAvailable()) {
            this.mRewardedVideoAd.show();
        } else {
            MoPubRewardedVideoManager.onRewardedVideoPlaybackError(GooglePlayServicesRewardedVideo.class, this.mAdUnitId, this.getMoPubErrorCode(0));
        }

    }

    public void onRewardedVideoAdLoaded() {
        MoPubRewardedVideoManager.onRewardedVideoLoadSuccess(GooglePlayServicesRewardedVideo.class, this.mAdUnitId);
    }

    public void onRewardedVideoAdOpened() {
    }

    public void onRewardedVideoStarted() {
        MoPubRewardedVideoManager.onRewardedVideoStarted(GooglePlayServicesRewardedVideo.class, this.mAdUnitId);
    }

    public void onRewardedVideoAdClosed() {
        MoPubRewardedVideoManager.onRewardedVideoClosed(GooglePlayServicesRewardedVideo.class, this.mAdUnitId);
    }

    public void onRewardedVideoCompleted() {
    }

    public void onRewarded(RewardItem rewardItem) {
        MoPubRewardedVideoManager.onRewardedVideoCompleted(GooglePlayServicesRewardedVideo.class, this.mAdUnitId, MoPubReward.success(rewardItem.getType(), rewardItem.getAmount()));
    }

    public void onRewardedVideoAdLeftApplication() {
        MoPubRewardedVideoManager.onRewardedVideoClicked(GooglePlayServicesRewardedVideo.class, this.mAdUnitId);
    }

    public void onRewardedVideoAdFailedToLoad(int error) {
        MoPubRewardedVideoManager.onRewardedVideoLoadFailure(GooglePlayServicesRewardedVideo.class, this.mAdUnitId, this.getMoPubErrorCode(error));
    }

    private MoPubErrorCode getMoPubErrorCode(int error) {
        MoPubErrorCode errorCode;
        switch(error) {
            case 0:
                errorCode = MoPubErrorCode.INTERNAL_ERROR;
                break;
            case 1:
                errorCode = MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR;
                break;
            case 2:
                errorCode = MoPubErrorCode.NO_CONNECTION;
                break;
            case 3:
                errorCode = MoPubErrorCode.NO_FILL;
                break;
            default:
                errorCode = MoPubErrorCode.UNSPECIFIED;
        }

        return errorCode;
    }
}

