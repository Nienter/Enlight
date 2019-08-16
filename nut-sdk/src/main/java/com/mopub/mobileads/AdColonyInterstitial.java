package com.mopub.mobileads;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.adcolony.sdk.AdColony;
import com.adcolony.sdk.AdColonyAppOptions;
import com.adcolony.sdk.AdColonyInterstitialListener;
import com.adcolony.sdk.AdColonyZone;
import com.android.pixel.AppUtil;
import com.mopub.common.MoPub;
import com.mopub.common.privacy.PersonalInfoManager;
import com.mopub.common.util.Json;

import java.util.Arrays;
import java.util.Map;

public class AdColonyInterstitial extends CustomEventInterstitial {
    private static final String TAG = "AdColonyInterstitial";
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
    private CustomEventInterstitialListener mCustomEventInterstitialListener;
    private AdColonyInterstitialListener mAdColonyInterstitialListener;
    private final Handler mHandler = new Handler();
    private com.adcolony.sdk.AdColonyInterstitial mAdColonyInterstitial;
    private static String[] previousAdColonyAllZoneIds;

    public AdColonyInterstitial() {
    }

    protected void loadInterstitial(@NonNull Context context, @NonNull CustomEventInterstitialListener customEventInterstitialListener, @Nullable Map<String, Object> localExtras, @NonNull Map<String, String> serverExtras) {
        if (!(context instanceof Activity)) {
            customEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
        } else {
            String clientOptions = "version=YOUR_APP_VERSION_HERE,store:google";
            String appId = "YOUR_AD_COLONY_APP_ID_HERE";
            String[] allZoneIds = DEFAULT_ALL_ZONE_IDS;
            String zoneId = "YOUR_CURRENT_ZONE_ID";
            this.mCustomEventInterstitialListener = customEventInterstitialListener;
            if (this.extrasAreValid(serverExtras)) {
                clientOptions = (String)serverExtras.get("clientOptions");
                appId = (String)serverExtras.get("appId");
                allZoneIds = this.extractAllZoneIds(serverExtras);
                zoneId = (String)serverExtras.get("zoneId");
            }

            AdColonyAppOptions mAdColonyAppOptions = AdColonyAppOptions.getMoPubAppOptions(clientOptions);
            PersonalInfoManager personalInfoManager = MoPub.getPersonalInformationManager();
            mAdColonyAppOptions = mAdColonyAppOptions == null ? new AdColonyAppOptions() : mAdColonyAppOptions;
            if (personalInfoManager != null && personalInfoManager.gdprApplies() != null && personalInfoManager.gdprApplies()) {
                mAdColonyAppOptions.setOption("explicit_consent_given", true).setOption("consent_response", MoPub.canCollectPersonalInformation());
            }

            this.mAdColonyInterstitialListener = this.getAdColonyInterstitialListener();
            if (!this.isAdColonyConfigured()) {
                AdColony.configure((Activity)context, mAdColonyAppOptions, appId, allZoneIds);
            } else if (shouldReconfigure(previousAdColonyAllZoneIds, allZoneIds)) {
                AdColony.configure((Activity)context, mAdColonyAppOptions, appId, allZoneIds);
                previousAdColonyAllZoneIds = allZoneIds;
            } else {
                AdColony.setAppOptions(mAdColonyAppOptions);
            }

            AdColony.requestInterstitial(zoneId, this.mAdColonyInterstitialListener);
        }
    }

    protected void showInterstitial() {
        if (this.mAdColonyInterstitial != null && !this.mAdColonyInterstitial.isExpired()) {
            this.mAdColonyInterstitial.show();
        } else {
            if(AppUtil.sDebug) {
                Log.e("AdColonyInterstitial", "AdColony interstitial ad is null or has expired");
            }
            this.mHandler.post(new Runnable() {
                public void run() {
                    AdColonyInterstitial.this.mCustomEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.VIDEO_PLAYBACK_ERROR);
                }
            });
        }

    }

    protected void onInvalidate() {
        if (this.mAdColonyInterstitial != null) {
            this.mAdColonyInterstitialListener = null;
            this.mAdColonyInterstitial.setListener((AdColonyInterstitialListener)null);
            this.mAdColonyInterstitial.destroy();
            this.mAdColonyInterstitial = null;
        }

    }

    private boolean isAdColonyConfigured() {
        return !AdColony.getSDKVersion().isEmpty();
    }

    private AdColonyInterstitialListener getAdColonyInterstitialListener() {
        return this.mAdColonyInterstitialListener != null ? this.mAdColonyInterstitialListener : new AdColonyInterstitialListener() {
            public void onRequestFilled(@NonNull com.adcolony.sdk.AdColonyInterstitial adColonyInterstitial) {
                AdColonyInterstitial.this.mAdColonyInterstitial = adColonyInterstitial;
                if(AppUtil.sDebug) {
                    Log.d("AdColonyInterstitial", "AdColony interstitial ad has been successfully loaded.");
                }
                AdColonyInterstitial.this.mHandler.post(new Runnable() {
                    public void run() {
                        AdColonyInterstitial.this.mCustomEventInterstitialListener.onInterstitialLoaded();
                    }
                });
            }

            public void onRequestNotFilled(@NonNull AdColonyZone zone) {
                if(AppUtil.sDebug) {
                    Log.d("AdColonyInterstitial", "AdColony interstitial ad has no fill.");
                }
                AdColonyInterstitial.this.mHandler.post(new Runnable() {
                    public void run() {
                        AdColonyInterstitial.this.mCustomEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.NETWORK_NO_FILL);
                    }
                });
            }

            public void onClosed(@NonNull com.adcolony.sdk.AdColonyInterstitial ad) {
                if(AppUtil.sDebug) {
                    Log.d("AdColonyInterstitial", "AdColony interstitial ad has been dismissed.");
                }
                AdColonyInterstitial.this.mHandler.post(new Runnable() {
                    public void run() {
                        AdColonyInterstitial.this.mCustomEventInterstitialListener.onInterstitialDismissed();
                    }
                });
            }

            public void onOpened(@NonNull com.adcolony.sdk.AdColonyInterstitial ad) {
                if(AppUtil.sDebug) {
                    Log.d("AdColonyInterstitial", "AdColony interstitial ad shown: " + ad.getZoneID());
                }
                AdColonyInterstitial.this.mHandler.post(new Runnable() {
                    public void run() {
                        AdColonyInterstitial.this.mCustomEventInterstitialListener.onInterstitialShown();
                    }
                });
            }

            public void onExpiring(@NonNull com.adcolony.sdk.AdColonyInterstitial ad) {
                if(AppUtil.sDebug) {
                    Log.d("AdColonyInterstitial", "AdColony interstitial ad is expiring; requesting new ad");
                }
                AdColony.requestInterstitial(ad.getZoneID(), AdColonyInterstitial.this.mAdColonyInterstitialListener);
            }

            public void onClicked(@NonNull com.adcolony.sdk.AdColonyInterstitial ad) {
                AdColonyInterstitial.this.mCustomEventInterstitialListener.onInterstitialClicked();
            }
        };
    }

    private boolean extrasAreValid(Map<String, String> extras) {
        return extras != null && extras.containsKey("clientOptions") && extras.containsKey("appId") && extras.containsKey("allZoneIds") && extras.containsKey("zoneId");
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

    private String[] extractAllZoneIds(Map<String, String> serverExtras) {
        String[] result = Json.jsonArrayToStringArray((String)serverExtras.get("allZoneIds"));
        if (result.length == 0) {
            result = new String[]{""};
        }

        return result;
    }
}
