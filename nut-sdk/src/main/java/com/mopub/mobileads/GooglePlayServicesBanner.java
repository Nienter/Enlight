package com.mopub.mobileads;

import android.content.Context;
import android.util.Log;

import com.android.pixel.AppUtil;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.mopub.common.util.Views;

import java.util.Map;

public class GooglePlayServicesBanner extends CustomEventBanner {
    public static final String AD_UNIT_ID_KEY = "adUnitID";
    public static final String AD_WIDTH_KEY = "adWidth";
    public static final String AD_HEIGHT_KEY = "adHeight";
    public static final String LOCATION_KEY = "location";
    private CustomEventBannerListener mBannerListener;
    private AdView mGoogleAdView;

    public GooglePlayServicesBanner() {
    }

    protected void loadBanner(Context context, CustomEventBannerListener customEventBannerListener, Map<String, Object> localExtras, Map<String, String> serverExtras) {
        this.mBannerListener = customEventBannerListener;
        if (this.extrasAreValid(serverExtras)) {
            String adUnitId = (String)serverExtras.get("adUnitID");
            int adWidth = Integer.parseInt((String)serverExtras.get("adWidth"));
            int adHeight = Integer.parseInt((String)serverExtras.get("adHeight"));
            this.mGoogleAdView = new AdView(context);
            this.mGoogleAdView.setAdListener(new AdViewListener());
            this.mGoogleAdView.setAdUnitId(adUnitId);
            AdSize adSize = this.calculateAdSize(adWidth, adHeight);
            if (adSize == null) {
                this.mBannerListener.onBannerFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
            } else {
                this.mGoogleAdView.setAdSize(adSize);
                AdRequest adRequest = (new AdRequest.Builder()).setRequestAgent("MoPub").build();

                try {
                    this.mGoogleAdView.loadAd(adRequest);
                } catch (NoClassDefFoundError var11) {
                    this.mBannerListener.onBannerFailed(MoPubErrorCode.NETWORK_NO_FILL);
                }

            }
        } else {
            this.mBannerListener.onBannerFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
        }
    }

    protected void onInvalidate() {
        Views.removeFromParent(this.mGoogleAdView);
        if (this.mGoogleAdView != null) {
            this.mGoogleAdView.setAdListener((AdListener)null);
            this.mGoogleAdView.destroy();
        }

    }

    private boolean extrasAreValid(Map<String, String> serverExtras) {
        try {
            Integer.parseInt((String)serverExtras.get("adWidth"));
            Integer.parseInt((String)serverExtras.get("adHeight"));
        } catch (NumberFormatException var3) {
            return false;
        }

        return serverExtras.containsKey("adUnitID");
    }

    private AdSize calculateAdSize(int width, int height) {
        if (width <= AdSize.BANNER.getWidth() && height <= AdSize.BANNER.getHeight()) {
            return AdSize.BANNER;
        } else if (width <= AdSize.MEDIUM_RECTANGLE.getWidth() && height <= AdSize.MEDIUM_RECTANGLE.getHeight()) {
            return AdSize.MEDIUM_RECTANGLE;
        } else if (width <= AdSize.FULL_BANNER.getWidth() && height <= AdSize.FULL_BANNER.getHeight()) {
            return AdSize.FULL_BANNER;
        } else {
            return width <= AdSize.LEADERBOARD.getWidth() && height <= AdSize.LEADERBOARD.getHeight() ? AdSize.LEADERBOARD : null;
        }
    }

    /** @deprecated */
    @Deprecated
    AdView getGoogleAdView() {
        return this.mGoogleAdView;
    }

    private class AdViewListener extends AdListener {
        private AdViewListener() {
        }

        public void onAdClosed() {
        }

        public void onAdFailedToLoad(int errorCode) {
            if(AppUtil.sDebug)
                Log.d("MoPub", "Google Play Services banner ad failed to load.");
            if (GooglePlayServicesBanner.this.mBannerListener != null) {
                GooglePlayServicesBanner.this.mBannerListener.onBannerFailed(this.getMoPubErrorCode(errorCode));
            }

        }

        public void onAdLeftApplication() {
        }

        public void onAdLoaded() {
            if(AppUtil.sDebug)
                Log.d("MoPub", "Google Play Services banner ad loaded successfully. Showing ad...");
            if (GooglePlayServicesBanner.this.mBannerListener != null) {
                GooglePlayServicesBanner.this.mBannerListener.onBannerLoaded(GooglePlayServicesBanner.this.mGoogleAdView);
            }

        }

        public void onAdOpened() {
            if(AppUtil.sDebug)
                Log.d("MoPub", "Google Play Services banner ad clicked.");
            if (GooglePlayServicesBanner.this.mBannerListener != null) {
                GooglePlayServicesBanner.this.mBannerListener.onBannerClicked();
            }

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
}