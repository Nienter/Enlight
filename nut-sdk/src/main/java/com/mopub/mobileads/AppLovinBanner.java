package com.mopub.mobileads;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.applovin.adview.AppLovinAdView;
import com.applovin.adview.AppLovinAdViewDisplayErrorCode;
import com.applovin.adview.AppLovinAdViewEventListener;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdClickListener;
import com.applovin.sdk.AppLovinAdDisplayListener;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinAdSize;
import com.applovin.sdk.AppLovinPrivacySettings;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkSettings;
import com.mopub.common.MoPub;
import com.mopub.common.logging.MoPubLog;
import com.mopub.common.util.Views;

import java.util.Map;


public class AppLovinBanner extends CustomEventBanner {
    private static final Handler UI_HANDLER = new Handler(Looper.getMainLooper());
    private static final int BANNER_STANDARD_HEIGHT = 50;
    private static final int BANNER_HEIGHT_OFFSET_TOLERANCE = 10;
    private static final int LEADER_STANDARD_HEIGHT = 90;
    private static final int LEADER_HEIGHT_OFFSET_TOLERANCE = 16;
    private static final String AD_WIDTH_KEY = "com_mopub_ad_width";
    private static final String AD_HEIGHT_KEY = "com_mopub_ad_height";
    private static final String ZONE_ID_SERVER_EXTRAS_KEY = "zone_id";

    private CustomEventBannerListener mBannerListener;
    private AppLovinAdView mApplovinAdView;
    public AppLovinBanner() {
    }

    protected void loadBanner(Context context, final CustomEventBannerListener customEventBannerListener, Map<String, Object> localExtras, Map<String, String> serverExtras) {
        mBannerListener = customEventBannerListener;

        boolean canCollectPersonalInfo = MoPub.canCollectPersonalInformation();
        AppLovinPrivacySettings.setHasUserConsent(canCollectPersonalInfo, context);
        AppLovinAdSize adSize = this.appLovinAdSizeFromLocalExtras(localExtras);
        if (adSize != null) {
            String adMarkup = (String)serverExtras.get("adm");
            boolean hasAdMarkup = !TextUtils.isEmpty(adMarkup);
            MoPubLog.d("Requesting AppLovin banner with serverExtras: " + serverExtras + ", localExtras: " + localExtras + " and has ad markup: " + hasAdMarkup);
            AppLovinSdk sdk = retrieveSdk(serverExtras, context);
            sdk.setPluginVersion("MoPub-3.1.0");
            sdk.setMediationProvider("mopub");

            if (hasAdMarkup) {
                //sdk.getAdService().loadNextAdForAdToken(adMarkup, adLoadListener);
                MoPubLog.d("mopub mediation for Applovin doesn't support adMarkup now");
                if (mBannerListener != null) {
                    mBannerListener.onBannerFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
                }
                return;
            } else {
                String zoneId = (String)serverExtras.get("zone_id");
                if(adSize.equals(AppLovinAdSize.MREC)){//WARNIGN! the zone feature is not provided for mrec
                    zoneId=null;
                }
                if (!TextUtils.isEmpty(zoneId)) {
                    mApplovinAdView = new AppLovinAdView(adSize, zoneId, context);
                } else {
                    mApplovinAdView = new AppLovinAdView(adSize, context);
                }
            }

            mApplovinAdView.setAdLoadListener(new AppLovinAdLoadListener() {
                public void adReceived(final AppLovinAd ad) {
                    //AppLovinBanner.runOnUiThread(new Runnable() {
                    //    public void run() {
                            //mApplovinAdView.renderAd(ad);
                            MoPubLog.d("Successfully loaded banner ad");

                            try {
                                if (mBannerListener != null) {
                                    mBannerListener.onBannerLoaded(mApplovinAdView);
                                }
                            } catch (Throwable var2) {
                                MoPubLog.e("Unable to notify listener of successful ad load.", var2);
                            }

                //        }
                //    });
                }

                public void failedToReceiveAd(final int errorCode) {
                    //AppLovinBanner.runOnUiThread(new Runnable() {
                    //    public void run() {
                            MoPubLog.d("Failed to load banner ad with code: " + errorCode);

                            try {
                                if (mBannerListener != null) {
                                    mBannerListener.onBannerFailed(AppLovinBanner.toMoPubErrorCode(errorCode));
                                }
                            } catch (Throwable var2) {
                                MoPubLog.e("Unable to notify listener of failure to receive ad.", var2);
                            }

                    //    }
                   // });
                }
            });


            mApplovinAdView.setAdDisplayListener(new AppLovinAdDisplayListener() {
                public void adDisplayed(AppLovinAd ad) {
                    MoPubLog.d("Banner displayed");
                }

                public void adHidden(AppLovinAd ad) {
                    MoPubLog.d("Banner dismissed");
                }
            });

            mApplovinAdView.setAdClickListener(new AppLovinAdClickListener() {
                public void adClicked(AppLovinAd ad) {
                    MoPubLog.d("Banner clicked");
                    if (mBannerListener != null) {
                        mBannerListener.onBannerClicked();
                    }

                }
            });

            mApplovinAdView.setAdViewEventListener(new AppLovinAdViewEventListener() {
                public void adOpenedFullscreen(AppLovinAd appLovinAd, AppLovinAdView appLovinAdView) {
                    MoPubLog.d("Banner opened fullscreen");
                    if (mBannerListener != null) {
                        mBannerListener.onBannerExpanded();
                    }

                }

                public void adClosedFullscreen(AppLovinAd appLovinAd, AppLovinAdView appLovinAdView) {
                    MoPubLog.d("Banner closed fullscreen");
                    if (mBannerListener != null) {
                        mBannerListener.onBannerCollapsed();
                    }

                }

                public void adLeftApplication(AppLovinAd appLovinAd, AppLovinAdView appLovinAdView) {
                    MoPubLog.d("Banner left application");
                }

                public void adFailedToDisplay(AppLovinAd appLovinAd, AppLovinAdView appLovinAdView, AppLovinAdViewDisplayErrorCode appLovinAdViewDisplayErrorCode) {
                }
            });

            mApplovinAdView.loadNextAd();

        } else {
            MoPubLog.d("Unable to request AppLovin banner");
            if (mBannerListener != null) {
                mBannerListener.onBannerFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
            }
        }

    }

    protected void onInvalidate() {
        Views.removeFromParent(this.mApplovinAdView);
        if (this.mApplovinAdView != null) {
            this.mApplovinAdView.setAdLoadListener(null);
            this.mApplovinAdView.setAdDisplayListener(null);
            this.mApplovinAdView.setAdClickListener(null);
            this.mApplovinAdView.destroy();
            this.mApplovinAdView=null;
        }
    }

    private AppLovinAdSize appLovinAdSizeFromLocalExtras(Map<String, Object> localExtras) {
        if (localExtras != null && !localExtras.isEmpty()) {
            try {
                int width = (Integer)localExtras.get("com_mopub_ad_width");
                int height = (Integer)localExtras.get("com_mopub_ad_height");
                if (width > 0 && height > 0) {
                    MoPubLog.d("Valid width (" + width + ") and height (" + height + ") provided");
                    int bannerOffset = Math.abs(50 - height);
                    int leaderOffset = Math.abs(90 - height);
                    if (bannerOffset <= 10) {
                        return AppLovinAdSize.BANNER;
                    }

                    if (leaderOffset <= 16) {
                        return AppLovinAdSize.LEADER;
                    }

                    if (height <= AppLovinAdSize.MREC.getHeight()) {
                        return AppLovinAdSize.MREC;
                    }

                    MoPubLog.d("Provided dimensions does not meet the dimensions required of banner or mrec ads");
                } else {
                    MoPubLog.d("Invalid width (" + width + ") and height (" + height + ") provided");
                }
            } catch (Throwable var6) {
                MoPubLog.d("Encountered error while parsing width and height from serverExtras", var6);
            }

            return null;
        } else {
            MoPubLog.d("No serverExtras provided");
            return null;
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
