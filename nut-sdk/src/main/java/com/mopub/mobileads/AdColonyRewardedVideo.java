package com.mopub.mobileads;

import android.app.Activity;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.adcolony.sdk.AdColony;
import com.adcolony.sdk.AdColonyAdOptions;
import com.adcolony.sdk.AdColonyAppOptions;
import com.adcolony.sdk.AdColonyInterstitial;
import com.adcolony.sdk.AdColonyInterstitialListener;
import com.adcolony.sdk.AdColonyReward;
import com.adcolony.sdk.AdColonyRewardListener;
import com.adcolony.sdk.AdColonyZone;
import com.android.pixel.AppUtil;
import com.mopub.common.BaseLifecycleListener;
import com.mopub.common.LifecycleListener;
import com.mopub.common.MediationSettings;
import com.mopub.common.MoPub;
import com.mopub.common.MoPubReward;
import com.mopub.common.privacy.PersonalInfoManager;
import com.mopub.common.util.Json;

import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AdColonyRewardedVideo extends CustomEventRewardedVideo {
    private static final String TAG = "AdColonyRewardedVideo";
    private static final String DEFAULT_CLIENT_OPTIONS = "version=YOUR_APP_VERSION_HERE,store:google";
    private static final String DEFAULT_APP_ID = "YOUR_AD_COLONY_APP_ID_HERE";
    private static final String[] DEFAULT_ALL_ZONE_IDS = new String[]{"ZONE_ID_1", "ZONE_ID_2", "..."};
    private static final String DEFAULT_ZONE_ID = "YOUR_CURRENT_ZONE_ID";
    private static final String CONSENT_RESPONSE = "consent_response";
    private static final String CONSENT_GIVEN = "explicit_consent_given";
    public static final String CLIENT_OPTIONS_KEY = "clientOptions";
    public static final String APP_ID_KEY = "appId";
    public static final String ALL_ZONE_IDS_KEY = "allZoneIds";
    public static final String ZONE_ID_KEY = "zoneId";
    private static boolean sInitialized = false;
    private static LifecycleListener sLifecycleListener = new BaseLifecycleListener();
    private static String[] previousAdColonyAllZoneIds;
    AdColonyInterstitial mAd;
    @NonNull
    private String mZoneId = "YOUR_CURRENT_ZONE_ID";
    private AdColonyListener mAdColonyListener;
    private AdColonyAdOptions mAdColonyAdOptions = new AdColonyAdOptions();
    private AdColonyAppOptions mAdColonyAppOptions = new AdColonyAppOptions();
    private static WeakHashMap<String, AdColonyInterstitial> sZoneIdToAdMap = new WeakHashMap();
    @NonNull
    private String mAdUnitId = "";
    private boolean mIsLoading = false;
    private final Handler mHandler = new Handler();
    private final ScheduledThreadPoolExecutor mScheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);

    public AdColonyRewardedVideo() {
    }

    @Nullable
    public CustomEventRewardedVideoListener getVideoListenerForSdk() {
        return this.mAdColonyListener;
    }

    @Nullable
    public LifecycleListener getLifecycleListener() {
        return sLifecycleListener;
    }

    @NonNull
    public String getAdNetworkId() {
        return this.mZoneId;
    }

    protected void onInvalidate() {
        this.mScheduledThreadPoolExecutor.shutdownNow();
        AdColonyInterstitial ad = (AdColonyInterstitial)sZoneIdToAdMap.get(this.mZoneId);
        if (ad != null) {
            ad.setListener((AdColonyInterstitialListener)null);
            ad.destroy();
            sZoneIdToAdMap.remove(this.mZoneId);
            if(AppUtil.sDebug) {
                Log.d("AdColonyRewardedVideo", "AdColony rewarded video destroyed");
            }
        }

    }

    public boolean checkAndInitializeSdk(@NonNull Activity launcherActivity, @NonNull Map<String, Object> localExtras, @NonNull Map<String, String> serverExtras) throws Exception {
        Class var4 = AdColonyRewardedVideo.class;
        synchronized(AdColonyRewardedVideo.class) {
            if (sInitialized) {
                return false;
            } else {
                String adColonyClientOptions = "version=YOUR_APP_VERSION_HERE,store:google";
                String adColonyAppId = "YOUR_AD_COLONY_APP_ID_HERE";
                String[] adColonyAllZoneIds = DEFAULT_ALL_ZONE_IDS;
                if (this.extrasAreValid(serverExtras)) {
                    adColonyClientOptions = (String)serverExtras.get("clientOptions");
                    adColonyAppId = (String)serverExtras.get("appId");
                    adColonyAllZoneIds = this.extractAllZoneIds(serverExtras);
                }

                this.mAdColonyAppOptions = AdColonyAppOptions.getMoPubAppOptions(adColonyClientOptions);
                if (!this.isAdColonyConfigured()) {
                    previousAdColonyAllZoneIds = adColonyAllZoneIds;
                    AdColony.configure(launcherActivity, this.mAdColonyAppOptions, adColonyAppId, adColonyAllZoneIds);
                }

                sInitialized = true;
                return true;
            }
        }
    }

    protected void loadWithSdkInitialized(@NonNull Activity activity, @NonNull Map<String, Object> localExtras, @NonNull Map<String, String> serverExtras) throws Exception {
        if (this.extrasAreValid(serverExtras)) {
            this.mZoneId = (String)serverExtras.get("zoneId");
            String adColonyClientOptions = (String)serverExtras.get("clientOptions");
            String adColonyAppId = (String)serverExtras.get("appId");
            String[] adColonyAllZoneIds = this.extractAllZoneIds(serverExtras);
            PersonalInfoManager personalInfoManager = MoPub.getPersonalInformationManager();
            this.mAdColonyAppOptions = AdColonyAppOptions.getMoPubAppOptions(adColonyClientOptions);
            this.mAdColonyAppOptions = this.mAdColonyAppOptions == null ? new AdColonyAppOptions() : this.mAdColonyAppOptions;
            if (personalInfoManager != null && personalInfoManager.gdprApplies() != null && personalInfoManager.gdprApplies()) {
                this.mAdColonyAppOptions.setOption("explicit_consent_given", true).setOption("consent_response", MoPub.canCollectPersonalInformation());
            }

            this.setUpGlobalSettings();
            if (shouldReconfigure(previousAdColonyAllZoneIds, adColonyAllZoneIds)) {
                AdColony.configure(activity, this.mAdColonyAppOptions, adColonyAppId, adColonyAllZoneIds);
                previousAdColonyAllZoneIds = adColonyAllZoneIds;
            } else {
                AdColony.setAppOptions(this.mAdColonyAppOptions);
            }
        }

        Object adUnitObject = localExtras.get("com_mopub_ad_unit_id");
        if (adUnitObject != null && adUnitObject instanceof String) {
            this.mAdUnitId = (String)adUnitObject;
        }

        sZoneIdToAdMap.put(this.mZoneId, (AdColonyInterstitial)null);
        this.setUpAdOptions();
        this.mAdColonyListener = new AdColonyListener(this.mAdColonyAdOptions);
        AdColony.setRewardListener(this.mAdColonyListener);
        AdColony.requestInterstitial(this.mZoneId, this.mAdColonyListener, this.mAdColonyAdOptions);
        this.scheduleOnVideoReady();
    }

    private static boolean shouldReconfigure(String[] previousZones, String[] newZones) {
        if (previousZones == null) {
            return true;
        } else if (newZones == null) {
            return false;
        } else if (previousZones.length != newZones.length) {
            return true;
        } else {
            Arrays.sort(previousZones);
            Arrays.sort(newZones);
            return !Arrays.equals(previousZones, newZones);
        }
    }

    private void setUpAdOptions() {
        this.mAdColonyAdOptions.enableConfirmationDialog(this.getConfirmationDialogFromSettings());
        this.mAdColonyAdOptions.enableResultsDialog(this.getResultsDialogFromSettings());
    }

    private boolean isAdColonyConfigured() {
        return !AdColony.getSDKVersion().isEmpty();
    }

    public boolean hasVideoAvailable() {
        return this.mAd != null && !this.mAd.isExpired();
    }

    public void showVideo() {
        if (this.hasVideoAvailable()) {
            this.mAd.show();
        } else {
            MoPubRewardedVideoManager.onRewardedVideoPlaybackError(AdColonyRewardedVideo.class, this.mZoneId, MoPubErrorCode.VIDEO_PLAYBACK_ERROR);
        }

    }

    private boolean extrasAreValid(Map<String, String> extras) {
        return extras != null && extras.containsKey("clientOptions") && extras.containsKey("appId") && extras.containsKey("allZoneIds") && extras.containsKey("zoneId");
    }

    private String[] extractAllZoneIds(Map<String, String> serverExtras) {
        String[] result = Json.jsonArrayToStringArray((String)serverExtras.get("allZoneIds"));
        if (result.length == 0) {
            result = new String[]{""};
        }

        return result;
    }

    private void setUpGlobalSettings() {
        AdColonyGlobalMediationSettings globalMediationSettings = (AdColonyGlobalMediationSettings)MoPubRewardedVideoManager.getGlobalMediationSettings(AdColonyGlobalMediationSettings.class);
        if (globalMediationSettings != null && globalMediationSettings.getUserId() != null) {
            this.mAdColonyAppOptions.setUserID(globalMediationSettings.getUserId());
        }

    }

    private boolean getConfirmationDialogFromSettings() {
        AdColonyInstanceMediationSettings settings = (AdColonyInstanceMediationSettings)MoPubRewardedVideoManager.getInstanceMediationSettings(AdColonyInstanceMediationSettings.class, this.mAdUnitId);
        return settings != null && settings.withConfirmationDialog();
    }

    private boolean getResultsDialogFromSettings() {
        AdColonyInstanceMediationSettings settings = (AdColonyInstanceMediationSettings)MoPubRewardedVideoManager.getInstanceMediationSettings(AdColonyInstanceMediationSettings.class, this.mAdUnitId);
        return settings != null && settings.withResultsDialog();
    }

    private void scheduleOnVideoReady() {
        Runnable runnable = new Runnable() {
            public void run() {
                if (AdColonyRewardedVideo.this.isAdAvailable(AdColonyRewardedVideo.this.mZoneId)) {
                    AdColonyRewardedVideo.this.mAd = (AdColonyInterstitial)AdColonyRewardedVideo.sZoneIdToAdMap.get(AdColonyRewardedVideo.this.mZoneId);
                    AdColonyRewardedVideo.this.mIsLoading = false;
                    AdColonyRewardedVideo.this.mScheduledThreadPoolExecutor.shutdownNow();
                    AdColonyRewardedVideo.this.mHandler.post(new Runnable() {
                        public void run() {
                            if (AdColonyRewardedVideo.this.hasVideoAvailable()) {
                                Log.d("AdColonyRewardedVideo", "AdColony rewarded ad has been successfully loaded.");
                                MoPubRewardedVideoManager.onRewardedVideoLoadSuccess(AdColonyRewardedVideo.class, AdColonyRewardedVideo.this.mZoneId);
                            } else {
                                MoPubRewardedVideoManager.onRewardedVideoLoadFailure(AdColonyRewardedVideo.class, AdColonyRewardedVideo.this.mZoneId, MoPubErrorCode.NETWORK_NO_FILL);
                            }

                        }
                    });
                }

            }
        };
        if (!this.mIsLoading) {
            this.mScheduledThreadPoolExecutor.scheduleAtFixedRate(runnable, 1L, 1L, TimeUnit.SECONDS);
            this.mIsLoading = true;
        }

    }

    private boolean isAdAvailable(String zoneId) {
        return sZoneIdToAdMap.get(zoneId) != null;
    }

    public static final class AdColonyInstanceMediationSettings implements MediationSettings {
        private final boolean mWithConfirmationDialog;
        private final boolean mWithResultsDialog;

        public AdColonyInstanceMediationSettings(boolean withConfirmationDialog, boolean withResultsDialog) {
            this.mWithConfirmationDialog = withConfirmationDialog;
            this.mWithResultsDialog = withResultsDialog;
        }

        public boolean withConfirmationDialog() {
            return this.mWithConfirmationDialog;
        }

        public boolean withResultsDialog() {
            return this.mWithResultsDialog;
        }
    }

    public static final class AdColonyGlobalMediationSettings implements MediationSettings {
        @Nullable
        private final String mUserId;

        public AdColonyGlobalMediationSettings(@Nullable String userId) {
            this.mUserId = userId;
        }

        @Nullable
        public String getUserId() {
            return this.mUserId;
        }
    }

    private static class AdColonyListener extends AdColonyInterstitialListener implements AdColonyRewardListener, CustomEventRewardedVideoListener {
        private static final String TAG = "AdColonyListener";
        private AdColonyAdOptions mAdOptions;

        AdColonyListener(AdColonyAdOptions adOptions) {
            this.mAdOptions = adOptions;
        }

        public void onReward(@NonNull AdColonyReward a) {
            MoPubReward reward;
            if (a.success()) {
                if(AppUtil.sDebug) {
                    Log.d("AdColonyListener", "AdColonyReward name: " + a.getRewardName());
                    Log.d("AdColonyListener", "AdColonyReward amount: " + a.getRewardAmount());
                }
                reward = MoPubReward.success(a.getRewardName(), a.getRewardAmount());
            } else {
                if(AppUtil.sDebug) {
                    Log.d("AdColonyListener", "AdColonyReward failed");
                }
                reward = MoPubReward.failure();
            }

            MoPubRewardedVideoManager.onRewardedVideoCompleted(AdColonyRewardedVideo.class, a.getZoneID(), reward);
        }

        public void onRequestFilled(@NonNull AdColonyInterstitial adColonyInterstitial) {
            AdColonyRewardedVideo.sZoneIdToAdMap.put(adColonyInterstitial.getZoneID(), adColonyInterstitial);
        }

        public void onRequestNotFilled(@NonNull AdColonyZone zone) {
            if(AppUtil.sDebug) {
                Log.d("AdColonyListener", "AdColony rewarded ad has no fill.");
            }
            MoPubRewardedVideoManager.onRewardedVideoLoadFailure(AdColonyRewardedVideo.class, zone.getZoneID(), MoPubErrorCode.NETWORK_NO_FILL);
        }

        public void onClosed(@NonNull AdColonyInterstitial ad) {
            if(AppUtil.sDebug) {
                Log.d("AdColonyListener", "AdColony rewarded ad has been dismissed.");
            }
            MoPubRewardedVideoManager.onRewardedVideoClosed(AdColonyRewardedVideo.class, ad.getZoneID());
        }

        public void onOpened(@NonNull AdColonyInterstitial ad) {
            if(AppUtil.sDebug) {
                Log.d("AdColonyListener", "AdColony rewarded ad shown: " + ad.getZoneID());
            }
            MoPubRewardedVideoManager.onRewardedVideoStarted(AdColonyRewardedVideo.class, ad.getZoneID());
        }

        public void onExpiring(@NonNull AdColonyInterstitial ad) {
            if(AppUtil.sDebug) {
                Log.d("AdColonyListener", "AdColony rewarded ad is expiring; requesting new ad");
            }
            AdColony.requestInterstitial(ad.getZoneID(), ad.getListener(), this.mAdOptions);
        }

        public void onClicked(@NonNull AdColonyInterstitial ad) {
            MoPubRewardedVideoManager.onRewardedVideoClicked(AdColonyRewardedVideo.class, ad.getZoneID());
        }
    }
}
