package com.mopub.mobileads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.pixel.AppUtil;
import com.flurry.android.FlurryAgentListener;
import com.flurry.android.ads.FlurryAdErrorType;
import com.flurry.android.ads.FlurryAdInterstitial;
import com.flurry.android.ads.FlurryAdInterstitialListener;

import java.util.Map;

class FlurryCustomEventInterstitial extends CustomEventInterstitial {
    private static final String LOG_TAG = FlurryCustomEventInterstitial.class.getSimpleName();
    private Context mContext;
    private CustomEventInterstitialListener mListener;
    private String mAdSpaceName;
    private FlurryAdInterstitial mInterstitial;

    FlurryCustomEventInterstitial() {
    }

    protected void loadInterstitial(Context context, CustomEventInterstitialListener listener, Map<String, Object> localExtras, Map<String, String> serverExtras) {
        if (context == null) {
            if(AppUtil.sDebug)
                Log.e(LOG_TAG, "Context cannot be null.");
            listener.onInterstitialFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
        } else if (listener == null) {
            if(AppUtil.sDebug)
                Log.e(LOG_TAG, "CustomEventInterstitialListener cannot be null.");
        } else if (!(context instanceof Activity)) {
            if(AppUtil.sDebug)
                Log.e(LOG_TAG, "Ad can be rendered only in Activity context.");
            listener.onInterstitialFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
        } else if (!this.validateExtras(serverExtras)) {
            if(AppUtil.sDebug)
                Log.e(LOG_TAG, "Failed interstitial ad fetch: Missing required server extras [FLURRY_APIKEY and/or FLURRY_ADSPACE].");
            listener.onInterstitialFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
        } else {
            this.setAutomaticImpressionAndClickTracking(false);
            this.mContext = context;
            this.mListener = listener;
            String apiKey = (String)serverExtras.get("apiKey");
            this.mAdSpaceName = (String)serverExtras.get("adSpaceName");
            FlurryAgentWrapper.getInstance().startSession(context, apiKey, (FlurryAgentListener)null);
            if(AppUtil.sDebug)
                Log.d(LOG_TAG, "Fetching Flurry ad, ad unit name:" + this.mAdSpaceName);
            this.mInterstitial = new FlurryAdInterstitial(this.mContext, this.mAdSpaceName);
            this.mInterstitial.setListener(new FlurryMopubInterstitialListener());
            this.mInterstitial.fetchAd();
        }
    }

    protected void onInvalidate() {
        if (this.mContext != null) {
            if(AppUtil.sDebug)
                Log.d(LOG_TAG, "MoPub issued onInvalidate (" + this.mAdSpaceName + ")");
            if (this.mInterstitial != null) {
                this.mInterstitial.destroy();
                this.mInterstitial = null;
            }

            FlurryAgentWrapper.getInstance().endSession(this.mContext);
            this.mContext = null;
            this.mListener = null;
        }
    }

    protected void showInterstitial() {
        if(AppUtil.sDebug)
            Log.d(LOG_TAG, "MoPub issued showInterstitial (" + this.mAdSpaceName + ")");
        if (this.mInterstitial != null) {
            this.mInterstitial.displayAd();
        }

    }

    private boolean validateExtras(Map<String, String> serverExtras) {
        if (serverExtras == null) {
            return false;
        } else {
            String flurryApiKey = (String)serverExtras.get("apiKey");
            String flurryAdSpace = (String)serverExtras.get("adSpaceName");
            if(AppUtil.sDebug)
                Log.i(LOG_TAG, "ServerInfo fetched from Mopub apiKey : " + flurryApiKey + " and " + "adSpaceName" + " :" + flurryAdSpace);
            return !TextUtils.isEmpty(flurryApiKey) && !TextUtils.isEmpty(flurryAdSpace);
        }
    }

    private class FlurryMopubInterstitialListener implements FlurryAdInterstitialListener {
        private final String LOG_TAG;

        private FlurryMopubInterstitialListener() {
            this.LOG_TAG = this.getClass().getSimpleName();
        }

        public void onFetched(FlurryAdInterstitial adInterstitial) {
            if(AppUtil.sDebug)
                Log.d(this.LOG_TAG, "onFetched: Flurry interstitial ad fetched successfully!");
            if (FlurryCustomEventInterstitial.this.mListener != null) {
                FlurryCustomEventInterstitial.this.mListener.onInterstitialLoaded();
            }

        }

        public void onRendered(FlurryAdInterstitial adInterstitial) {
            if(AppUtil.sDebug)
                Log.d(this.LOG_TAG, "onRendered: Flurry interstitial ad rendered");
            if (FlurryCustomEventInterstitial.this.mListener != null) {
                FlurryCustomEventInterstitial.this.mListener.onInterstitialShown();
            }

        }

        public void onDisplay(FlurryAdInterstitial adInterstitial) {
            if(AppUtil.sDebug)
                Log.d(this.LOG_TAG, "onDisplay: Flurry interstitial ad displayed");
            if (FlurryCustomEventInterstitial.this.mListener != null) {
                FlurryCustomEventInterstitial.this.mListener.onInterstitialImpression();
            }

        }

        public void onClose(FlurryAdInterstitial adInterstitial) {
            if(AppUtil.sDebug)
                Log.d(this.LOG_TAG, "onClose: Flurry interstitial ad closed");
            if (FlurryCustomEventInterstitial.this.mListener != null) {
                FlurryCustomEventInterstitial.this.mListener.onInterstitialDismissed();
            }

        }

        public void onAppExit(FlurryAdInterstitial adInterstitial) {
            if(AppUtil.sDebug)
                Log.d(this.LOG_TAG, "onAppExit: Flurry interstitial ad exited app");
        }

        public void onClicked(FlurryAdInterstitial adInterstitial) {
            if(AppUtil.sDebug)
                Log.d(this.LOG_TAG, "onClicked: Flurry interstitial ad clicked");
            if (FlurryCustomEventInterstitial.this.mListener != null) {
                FlurryCustomEventInterstitial.this.mListener.onInterstitialClicked();
            }

        }

        public void onVideoCompleted(FlurryAdInterstitial adInterstitial) {
            if(AppUtil.sDebug)
                Log.d(this.LOG_TAG, "onVideoCompleted: Flurry interstitial ad video completed");
        }

        public void onError(FlurryAdInterstitial adInterstitial, FlurryAdErrorType adErrorType, int errorCode) {
            if(AppUtil.sDebug)
                Log.d(this.LOG_TAG, String.format("onError: Flurry interstitial ad not available. Error type: %s. Error code: %s", adErrorType.toString(), errorCode));
            if (FlurryCustomEventInterstitial.this.mListener != null) {
                switch(adErrorType) {
                    case FETCH:
                        FlurryCustomEventInterstitial.this.mListener.onInterstitialFailed(MoPubErrorCode.NETWORK_NO_FILL);
                        return;
                    case RENDER:
                        FlurryCustomEventInterstitial.this.mListener.onInterstitialFailed(MoPubErrorCode.NETWORK_INVALID_STATE);
                        return;
                    case CLICK:
                        return;
                    default:
                        FlurryCustomEventInterstitial.this.mListener.onInterstitialFailed(MoPubErrorCode.UNSPECIFIED);
                }
            }

        }
    }
}
