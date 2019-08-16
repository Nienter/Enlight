package com.mopub.mobileads;

import android.content.Context;
import android.util.Log;

import com.android.pixel.AppUtil;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.util.Map;

public class GooglePlayServicesInterstitial extends CustomEventInterstitial {
    public static final String AD_UNIT_ID_KEY = "adUnitID";
    public static final String LOCATION_KEY = "location";
    private CustomEventInterstitialListener mInterstitialListener;
    private InterstitialAd mGoogleInterstitialAd;

    public GooglePlayServicesInterstitial() {
    }

    protected void loadInterstitial(Context context, CustomEventInterstitialListener customEventInterstitialListener, Map<String, Object> localExtras, Map<String, String> serverExtras) {
        this.mInterstitialListener = customEventInterstitialListener;
        if (this.extrasAreValid(serverExtras)) {
            String adUnitId = (String)serverExtras.get("adUnitID");
            this.mGoogleInterstitialAd = new InterstitialAd(context);
            this.mGoogleInterstitialAd.setAdListener(new InterstitialAdListener());
            this.mGoogleInterstitialAd.setAdUnitId(adUnitId);
            AdRequest adRequest = (new AdRequest.Builder()).setRequestAgent("MoPub").build();

            try {
                this.mGoogleInterstitialAd.loadAd(adRequest);
            } catch (NoClassDefFoundError var8) {
                this.mInterstitialListener.onInterstitialFailed(MoPubErrorCode.NETWORK_NO_FILL);
            }

        } else {
            this.mInterstitialListener.onInterstitialFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
        }
    }

    protected void showInterstitial() {
        if (this.mGoogleInterstitialAd.isLoaded()) {
            this.mGoogleInterstitialAd.show();
        } else {
            if(AppUtil.sDebug)
                Log.d("MoPub", "Tried to show a Google Play Services interstitial ad before it finished loading. Please try again.");
        }

    }

    protected void onInvalidate() {
        if (this.mGoogleInterstitialAd != null) {
            this.mGoogleInterstitialAd.setAdListener((AdListener)null);
        }

    }

    private boolean extrasAreValid(Map<String, String> serverExtras) {
        return serverExtras.containsKey("adUnitID");
    }

    /** @deprecated */
    @Deprecated
    InterstitialAd getGoogleInterstitialAd() {
        return this.mGoogleInterstitialAd;
    }

    private class InterstitialAdListener extends AdListener {
        private InterstitialAdListener() {
        }

        public void onAdClosed() {
            if(AppUtil.sDebug)
                Log.d("MoPub", "Google Play Services interstitial ad dismissed.");
            if (GooglePlayServicesInterstitial.this.mInterstitialListener != null) {
                GooglePlayServicesInterstitial.this.mInterstitialListener.onInterstitialDismissed();
            }

        }

        public void onAdFailedToLoad(int errorCode) {
            if(AppUtil.sDebug)
                Log.d("MoPub", "Google Play Services interstitial ad failed to load.");
            if (GooglePlayServicesInterstitial.this.mInterstitialListener != null) {
                GooglePlayServicesInterstitial.this.mInterstitialListener.onInterstitialFailed(this.getMoPubErrorCode(errorCode));
            }

        }

        public void onAdLeftApplication() {
            if(AppUtil.sDebug)
                Log.d("MoPub", "Google Play Services interstitial ad clicked.");
            if (GooglePlayServicesInterstitial.this.mInterstitialListener != null) {
                GooglePlayServicesInterstitial.this.mInterstitialListener.onInterstitialClicked();
            }

        }

        public void onAdLoaded() {
            if(AppUtil.sDebug)
                Log.d("MoPub", "Google Play Services interstitial ad loaded successfully.");
            if (GooglePlayServicesInterstitial.this.mInterstitialListener != null) {
                GooglePlayServicesInterstitial.this.mInterstitialListener.onInterstitialLoaded();
            }

        }

        public void onAdOpened() {
            if(AppUtil.sDebug)
                Log.d("MoPub", "Showing Google Play Services interstitial ad.");
            if (GooglePlayServicesInterstitial.this.mInterstitialListener != null) {
                GooglePlayServicesInterstitial.this.mInterstitialListener.onInterstitialShown();
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