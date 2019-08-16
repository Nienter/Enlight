package com.mopub.mobileads;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.chartboost.sdk.Chartboost;
import com.chartboost.sdk.ChartboostDelegate;
import com.chartboost.sdk.Model.CBError;
import com.mopub.common.MoPub;
import com.mopub.common.MoPubReward;
import com.mopub.common.Preconditions;
import com.mopub.common.VisibleForTesting;
import com.mopub.common.logging.MoPubLog;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class ChartboostShared {
    private static volatile ChartboostSingletonDelegate sDelegate = new ChartboostSingletonDelegate();
    public static final String APP_ID_KEY = "appId";
    public static final String APP_SIGNATURE_KEY = "appSignature";
    public static final String LOCATION_KEY = "location";
    public static final String LOCATION_DEFAULT = "Default";
    @Nullable
    private static String mAppId;
    @Nullable
    private static String mAppSignature;

    public ChartboostShared() {
    }

    public static synchronized boolean initializeSdk(@NonNull Activity launcherActivity, @NonNull Map<String, String> serverExtras) {
        Preconditions.checkNotNull(launcherActivity);
        Preconditions.checkNotNull(serverExtras);
        boolean canCollectPersonalInfo = MoPub.canCollectPersonalInformation();
        Chartboost.setPIDataUseConsent(launcherActivity.getApplicationContext(), canCollectPersonalInfo ? Chartboost.CBPIDataUseConsent.YES_BEHAVIORAL : Chartboost.CBPIDataUseConsent.NO_BEHAVIORAL);
        if (!serverExtras.containsKey("appId")) {
            throw new IllegalStateException("Chartboost rewarded video initialization failed due to missing application ID.");
        } else if (!serverExtras.containsKey("appSignature")) {
            throw new IllegalStateException("Chartboost rewarded video initialization failed due to missing application signature.");
        } else {
            String appId = (String)serverExtras.get("appId");
            String appSignature = (String)serverExtras.get("appSignature");
            if (appId.equals(mAppId) && appSignature.equals(mAppSignature)) {
                return false;
            } else {
                mAppId = appId;
                mAppSignature = appSignature;
                Chartboost.startWithAppId(launcherActivity, mAppId, mAppSignature);
                Chartboost.setMediation(Chartboost.CBMediation.CBMediationMoPub, "5.4.1");
                Chartboost.setDelegate(sDelegate);
                Chartboost.setShouldRequestInterstitialsInFirstSession(true);
                Chartboost.setAutoCacheAds(false);
                Chartboost.setShouldDisplayLoadingViewForMoreApps(false);
                return true;
            }
        }
    }

    @NonNull
    public static ChartboostSingletonDelegate getDelegate() {
        return sDelegate;
    }

    /** @deprecated */
    @Deprecated
    @VisibleForTesting
    static void reset() {
        sDelegate = new ChartboostSingletonDelegate();
        mAppId = null;
        mAppSignature = null;
    }

    public static class ChartboostSingletonDelegate extends ChartboostDelegate implements CustomEventRewardedVideo.CustomEventRewardedVideoListener {
        private static final CustomEventInterstitial.CustomEventInterstitialListener NULL_LISTENER = new CustomEventInterstitial.CustomEventInterstitialListener() {
            public void onInterstitialLoaded() {
            }

            public void onInterstitialFailed(MoPubErrorCode errorCode) {
            }

            public void onInterstitialShown() {
            }

            public void onInterstitialClicked() {
            }

            public void onInterstitialImpression() {
            }

            public void onLeaveApplication() {
            }

            public void onInterstitialDismissed() {
            }
        };
        private Map<String, CustomEventInterstitial.CustomEventInterstitialListener> mInterstitialListenersForLocation = Collections.synchronizedMap(new TreeMap());
        private Set<String> mRewardedVideoLocationsToLoad = Collections.synchronizedSet(new TreeSet());

        public ChartboostSingletonDelegate() {
        }

        public void registerInterstitialListener(@NonNull String location, @NonNull CustomEventInterstitial.CustomEventInterstitialListener interstitialListener) {
            Preconditions.checkNotNull(location);
            Preconditions.checkNotNull(interstitialListener);
            this.mInterstitialListenersForLocation.put(location, interstitialListener);
        }

        public void unregisterInterstitialListener(@NonNull String location) {
            Preconditions.checkNotNull(location);
            this.mInterstitialListenersForLocation.remove(location);
        }

        public void registerRewardedVideoLocation(@NonNull String location) {
            Preconditions.checkNotNull(location);
            this.mRewardedVideoLocationsToLoad.add(location);
        }

        public void unregisterRewardedVideoLocation(@NonNull String location) {
            Preconditions.checkNotNull(location);
            this.mRewardedVideoLocationsToLoad.remove(location);
        }

        @NonNull
        public CustomEventInterstitial.CustomEventInterstitialListener getInterstitialListener(@NonNull String location) {
            CustomEventInterstitial.CustomEventInterstitialListener listener = (CustomEventInterstitial.CustomEventInterstitialListener)this.mInterstitialListenersForLocation.get(location);
            return listener != null ? listener : NULL_LISTENER;
        }

        public boolean hasInterstitialLocation(@NonNull String location) {
            return this.mInterstitialListenersForLocation.containsKey(location);
        }

        public void didCacheInterstitial(String location) {
            MoPubLog.d("Chartboost interstitial loaded successfully.");
            this.getInterstitialListener(location).onInterstitialLoaded();
        }

        public void didFailToLoadInterstitial(String location, CBError.CBImpressionError error) {
            String suffix = error != null ? "Error: " + error.name() : "";
            Log.d("MoPub", "Chartboost interstitial ad failed to load." + suffix);
            this.getInterstitialListener(location).onInterstitialFailed(MoPubErrorCode.NETWORK_NO_FILL);
        }

        public void didDismissInterstitial(String location) {
            MoPubLog.d("Chartboost interstitial ad dismissed.");
            this.getInterstitialListener(location).onInterstitialDismissed();
        }

        public void didCloseInterstitial(String location) {
            MoPubLog.d("Chartboost interstitial ad closed.");
        }

        public void didClickInterstitial(String location) {
            MoPubLog.d("Chartboost interstitial ad clicked.");
            this.getInterstitialListener(location).onInterstitialClicked();
        }

        public void didDisplayInterstitial(String location) {
            MoPubLog.d("Chartboost interstitial ad shown.");
            this.getInterstitialListener(location).onInterstitialShown();
        }

        public void didCacheRewardedVideo(String location) {
            super.didCacheRewardedVideo(location);
            if (this.mRewardedVideoLocationsToLoad.contains(location)) {
                MoPubLog.d("Chartboost rewarded video cached for location " + location + ".");
                MoPubRewardedVideoManager.onRewardedVideoLoadSuccess(ChartboostRewardedVideo.class, location);
                this.mRewardedVideoLocationsToLoad.remove(location);
            }

        }

        public void didFailToLoadRewardedVideo(String location, CBError.CBImpressionError error) {
            super.didFailToLoadRewardedVideo(location, error);
            String suffix = error != null ? " with error: " + error.name() : "";
            if (this.mRewardedVideoLocationsToLoad.contains(location)) {
                MoPubErrorCode errorCode = MoPubErrorCode.VIDEO_DOWNLOAD_ERROR;
                MoPubLog.d("Chartboost rewarded video cache failed for location " + location + suffix);
                if (CBError.CBImpressionError.INVALID_LOCATION.equals(error)) {
                    errorCode = MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR;
                }

                MoPubRewardedVideoManager.onRewardedVideoLoadFailure(ChartboostRewardedVideo.class, location, errorCode);
                this.mRewardedVideoLocationsToLoad.remove(location);
            }

        }

        public void didDismissRewardedVideo(String location) {
            super.didDismissRewardedVideo(location);
            MoPubRewardedVideoManager.onRewardedVideoClosed(ChartboostRewardedVideo.class, location);
            MoPubLog.d("Chartboost rewarded video dismissed for location " + location + ".");
        }

        public void didCloseRewardedVideo(String location) {
            super.didCloseRewardedVideo(location);
            MoPubLog.d("Chartboost rewarded video closed for location " + location + ".");
        }

        public void didClickRewardedVideo(String location) {
            super.didClickRewardedVideo(location);
            MoPubRewardedVideoManager.onRewardedVideoClicked(ChartboostRewardedVideo.class, location);
            MoPubLog.d("Chartboost rewarded video clicked for location " + location + ".");
        }

        public void didCompleteRewardedVideo(String location, int reward) {
            super.didCompleteRewardedVideo(location, reward);
            MoPubLog.d("Chartboost rewarded video completed for location " + location + " with reward amount " + reward);
            MoPubRewardedVideoManager.onRewardedVideoCompleted(ChartboostRewardedVideo.class, location, MoPubReward.success("", reward));
        }

        public void didDisplayRewardedVideo(String location) {
            super.didDisplayRewardedVideo(location);
            MoPubLog.d("Chartboost rewarded video displayed for location " + location + ".");
            MoPubRewardedVideoManager.onRewardedVideoStarted(ChartboostRewardedVideo.class, location);
        }

        public boolean shouldRequestMoreApps(String location) {
            return false;
        }

        public boolean shouldDisplayMoreApps(String location) {
            return false;
        }
    }
}