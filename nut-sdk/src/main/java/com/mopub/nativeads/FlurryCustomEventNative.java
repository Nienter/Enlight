package com.mopub.nativeads;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.android.pixel.AppUtil;
import com.flurry.android.FlurryAgentListener;
import com.flurry.android.ads.FlurryAdErrorType;
import com.flurry.android.ads.FlurryAdNative;
import com.flurry.android.ads.FlurryAdNativeAsset;
import com.flurry.android.ads.FlurryAdNativeListener;
import com.flurry.android.ads.FlurryAdTargeting;
import com.mopub.mobileads.FlurryAgentWrapper;
import com.mopub.nativeads.NativeImageHelper.ImageListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FlurryCustomEventNative extends CustomEventNative {
    public static final String EXTRA_STAR_RATING_IMG = "flurry_starratingimage";
    public static final String EXTRA_APP_CATEGORY = "flurry_appcategorytext";
    public static final String EXTRA_SEC_BRANDING_LOGO = "flurry_brandingimage";
    public static final String EXTRA_SEC_ADVERTISER_NAME = "flurry_advertisername";
    public static final String LOCAL_EXTRA_TEST_MODE = "enableTestMode";
    private static final String sTAG = FlurryCustomEventNative.class.getSimpleName();

    private static final String ASSET_SEC_HQ_RATING_IMG = "secHqRatingImg";

    private static final String ASSET_SEC_RATING_IMG = "secRatingImg";
    private static final String ASSET_APP_RATING = "appRating";
    private static final String ASSET_APP_CATEGORY = "appCategory";


    private static final String ASSET_CALL_TO_ACTION = "callToAction";
    private static final String ASSET_VIDEO = "videoUrl";
    private static final String ASSET_ADVERTISER_NAME = "source";
    private static final double MOPUB_STAR_RATING_SCALE = 5.0D;
    private FlurryAgentListener mFlurryAgentListener;
    private static final List<FlurryAdNative> sFlurryNativeAds = new ArrayList();

    public FlurryCustomEventNative() {
    }

    @Override
    protected void loadNativeAd(@NonNull final Context context, @NonNull final CustomEventNativeListener customEventNativeListener, @NonNull final Map<String, Object> localExtras, @NonNull Map<String, String> serverExtras) {
        if (this.validateExtras(serverExtras)) {
            String flurryApiKey = (String)serverExtras.get("apiKey");
            final String flurryAdSpace = (String)serverExtras.get("adSpaceName");
            if (!FlurryAgentWrapper.getInstance().isSessionActive() && this.mFlurryAgentListener == null) {
                this.mFlurryAgentListener = new FlurryAgentListener() {
                    public void onSessionStarted() {
                        FlurryCustomEventNative.this.fetchFlurryAd(context, flurryAdSpace, localExtras, customEventNativeListener);
                    }
                };
                FlurryAgentWrapper.getInstance().startSession(context, flurryApiKey, this.mFlurryAgentListener);
            } else {
                this.fetchFlurryAd(context, flurryAdSpace, localExtras, customEventNativeListener);
            }
        } else {
            customEventNativeListener.onNativeAdFailed(NativeErrorCode.NATIVE_ADAPTER_CONFIGURATION_ERROR);

            if(AppUtil.sDebug)
                Log.i(sTAG, "Failed Native AdFetch: Missing required server extras [FLURRY_APIKEY and/or FLURRY_ADSPACE].");
        }

    }

    private static synchronized void mapNativeAd(@NonNull FlurryMediationNativeAd mopubSupportedAd, @NonNull FlurryAdNative flurryAdNative) {
        //Main Image
        FlurryAdNativeAsset coverImageAsset = flurryAdNative.getAsset("secHqImage");
        if (coverImageAsset != null && !TextUtils.isEmpty(coverImageAsset.getValue())) {
            mopubSupportedAd.setMainImageUrl(coverImageAsset.getValue());
        }

        //Icon Image
        FlurryAdNativeAsset iconImageAsset = flurryAdNative.getAsset("secImage");
        if (iconImageAsset != null && !TextUtils.isEmpty(iconImageAsset.getValue())) {
            mopubSupportedAd.setIconImageUrl(iconImageAsset.getValue());
        }

        //Ad Title
        mopubSupportedAd.setTitle(flurryAdNative.getAsset("headline").getValue());

        //Ad Summary
        mopubSupportedAd.setText(flurryAdNative.getAsset("summary").getValue());

        //Sponsored Marker
        FlurryAdNativeAsset asset = flurryAdNative.getAsset("secHqBrandingLogo");
        if(asset != null && !TextUtils.isEmpty(asset.getValue())) {
            mopubSupportedAd.setSponsoredMarkerImageUrl(asset.getValue());
        }

        //Advertiser Name
        mopubSupportedAd.setAdvertiserName(flurryAdNative.getAsset("source").getValue());

        if (mopubSupportedAd.isAppInstallAd()) {
            FlurryAdNativeAsset ratingImgAsset = flurryAdNative.getAsset("secHqRatingImg");
            if (ratingImgAsset != null && !TextUtils.isEmpty(ratingImgAsset.getValue())) {
                mopubSupportedAd.addExtra("flurry_starratingimage", ratingImgAsset.getValue());
            } else {
                ratingImgAsset = flurryAdNative.getAsset("secRatingImg");
                if (ratingImgAsset != null && !TextUtils.isEmpty(ratingImgAsset.getValue())) {
                    mopubSupportedAd.addExtra("flurry_starratingimage", ratingImgAsset.getValue());
                }
            }

            FlurryAdNativeAsset appCategoryAsset = flurryAdNative.getAsset("appCategory");
            if (appCategoryAsset != null) {
                mopubSupportedAd.addExtra("flurry_appcategorytext", appCategoryAsset.getValue());
            }

            FlurryAdNativeAsset appRatingAsset = flurryAdNative.getAsset("appRating");
            if (appRatingAsset != null) {
                mopubSupportedAd.setStarRating(getStarRatingValue(appRatingAsset.getValue()));
            }
        }

        FlurryAdNativeAsset ctaAsset = flurryAdNative.getAsset("callToAction");
        if (ctaAsset != null) {
            mopubSupportedAd.setCallToAction(ctaAsset.getValue());
        }

        if (mopubSupportedAd.getImageUrls().isEmpty()) {
            if(AppUtil.sDebug)
                Log.d(sTAG, "preCacheImages: No images to cache for Flurry Native Ad: " + flurryAdNative.toString());
            mopubSupportedAd.onNativeAdLoaded();
        } else {
            mopubSupportedAd.precacheImages();
        }

    }

    @Nullable
    private static Double getStarRatingValue(@Nullable String appRatingString) {
        Double rating = null;
        if (appRatingString != null) {
            String[] ratingParts = appRatingString.split("/");
            if (ratingParts.length == 2) {
                try {
                    float numer = (float)Integer.valueOf(ratingParts[0]);
                    float denom = (float)Integer.valueOf(ratingParts[1]);
                    rating = (double)(numer / denom) * 5.0D;
                } catch (NumberFormatException var5) {
                    ;
                }
            }
        }

        return rating;
    }

    private boolean validateExtras(Map<String, String> serverExtras) {
        String flurryApiKey = (String)serverExtras.get("apiKey");
        String flurryAdSpace = (String)serverExtras.get("adSpaceName");
        if(AppUtil.sDebug)
            Log.i(sTAG, "ServerInfo fetched from Mopub apiKey : " + flurryApiKey + " and " + "adSpaceName" + " :" + flurryAdSpace);
        return !TextUtils.isEmpty(flurryApiKey) && !TextUtils.isEmpty(flurryAdSpace);
    }

    private void fetchFlurryAd(@NonNull Context context, String flurryAdSpace, @NonNull Map<String, Object> localExtras, @NonNull CustomEventNativeListener customEventNativeListener) {
        FlurryAdNative flurryAdNative = new FlurryAdNative(context, flurryAdSpace);
        if (localExtras.containsKey("enableTestMode") && localExtras.get("enableTestMode") instanceof Boolean) {
            FlurryAdTargeting targeting = new FlurryAdTargeting();
            targeting.setEnableTestAds((Boolean)localExtras.get("enableTestMode"));
        }

        FlurryMediationNativeAd flurryNativeAd;
        flurryNativeAd = new FlurryMediationNativeAd(context, flurryAdNative, customEventNativeListener);
        sFlurryNativeAds.add(flurryAdNative);
        flurryNativeAd.fetchAd();
    }


    abstract static class FlurryBaseAdListener implements FlurryAdNativeListener {
        @NonNull
        private final FlurryMediationNativeAd mBaseNativeAd;

        FlurryBaseAdListener(@NonNull FlurryMediationNativeAd baseNativeAd) {
            this.mBaseNativeAd = baseNativeAd;
        }

        public void onFetched(FlurryAdNative flurryAdNative) {
            if(AppUtil.sDebug)
                Log.d(FlurryCustomEventNative.sTAG, "onFetched: Flurry native ad fetched successfully!");
            FlurryCustomEventNative.mapNativeAd(this.mBaseNativeAd, flurryAdNative);
            FlurryCustomEventNative.sFlurryNativeAds.remove(flurryAdNative);
        }

        public void onShowFullscreen(FlurryAdNative flurryAdNative) {
            if(AppUtil.sDebug)
                Log.d(FlurryCustomEventNative.sTAG, "onShowFullscreen: Flurry native ad in full-screen");
        }

        public void onCloseFullscreen(FlurryAdNative flurryAdNative) {
            if(AppUtil.sDebug)
                Log.d(FlurryCustomEventNative.sTAG, "onCloseFullscreen: Flurry native ad full-screen closed");
        }

        public void onAppExit(FlurryAdNative flurryAdNative) {
            if(AppUtil.sDebug)
                Log.d(FlurryCustomEventNative.sTAG, "onAppExit: Flurry native ad exited app");
        }

        public void onClicked(FlurryAdNative flurryAdNative) {
            if(AppUtil.sDebug)
                Log.d(FlurryCustomEventNative.sTAG, "onClicked: Flurry native ad clicked");
        }

        public void onImpressionLogged(FlurryAdNative flurryAdNative) {
            if(AppUtil.sDebug)
                Log.d(FlurryCustomEventNative.sTAG, "onImpressionLogged: Flurry native ad impression logged");
        }

        public void onExpanded(FlurryAdNative flurryAdNative) {
            if(AppUtil.sDebug)
                Log.d(FlurryCustomEventNative.sTAG, "onExpanded: Flurry native ad expanded");
        }

        public void onCollapsed(FlurryAdNative flurryAdNative) {
            if(AppUtil.sDebug)
                Log.d(FlurryCustomEventNative.sTAG, "onCollapsed: Flurry native ad collapsed");
        }

        public void onError(FlurryAdNative flurryAdNative, FlurryAdErrorType adErrorType, int errorCode) {
            if(AppUtil.sDebug)
                Log.d(FlurryCustomEventNative.sTAG, String.format("onError: Flurry native ad not available. Error type: %s. Error code: %s", adErrorType.toString(), errorCode));
            FlurryCustomEventNative.sFlurryNativeAds.remove(flurryAdNative);
        }
    }

    static class FlurryMediationNativeAd extends BaseNativeAd{
        @NonNull
        private final Context mContext;
        @NonNull
        private final CustomEventNativeListener mCustomEventNativeListener;
        @NonNull
        private final FlurryAdNative mFlurryAdNative;
        private final FlurryAdNativeListener mFlurryNativelistener = new FlurryBaseAdListener(this) {
            public void onClicked(FlurryAdNative flurryAdNative) {
                super.onClicked(flurryAdNative);
                FlurryMediationNativeAd.this.notifyAdClicked();
            }

            public void onImpressionLogged(FlurryAdNative flurryAdNative) {
                super.onImpressionLogged(flurryAdNative);
                FlurryMediationNativeAd.this.notifyAdImpressed();
            }

            public void onError(FlurryAdNative adNative, FlurryAdErrorType adErrorType, int errorCode) {
                super.onError(adNative, adErrorType, errorCode);
                FlurryMediationNativeAd.this.mCustomEventNativeListener.onNativeAdFailed(NativeErrorCode.NETWORK_NO_FILL);
            }
        };
        @Nullable
        private String mTitle;
        @Nullable
        private String mText;

        @Nullable
        private String mAdvertiserName;

        @Nullable
        private String mCallToAction;
        @Nullable
        private String mMainImageUrl;
        @Nullable
        private String mIconImageUrl;

        @Nullable
        private String mSponsoredMarkerImageUrl;

        @Nullable
        private Double mStarRating;
        @NonNull
        private final Map<String, Object> mExtras;

        FlurryMediationNativeAd(@NonNull Context context, @NonNull FlurryAdNative adNative, @NonNull CustomEventNativeListener customEventNativeListener) {
            this.mContext = context;
            this.mFlurryAdNative = adNative;
            this.mCustomEventNativeListener = customEventNativeListener;
            this.mExtras = new HashMap();
        }

        public void prepare(@NonNull View view) {
            this.mFlurryAdNative.setTrackingView(view);
            if(AppUtil.sDebug)
                Log.d(FlurryCustomEventNative.sTAG, "prepare(" + this.mFlurryAdNative.toString() + " " + view.toString() + ")");
        }

        public void clear(@NonNull View view) {
            this.mFlurryAdNative.removeTrackingView();
            if(AppUtil.sDebug)
                Log.d(FlurryCustomEventNative.sTAG, "clear(" + this.mFlurryAdNative.toString() + ")");
        }

        public void destroy() {
            if(AppUtil.sDebug)
                Log.d(FlurryCustomEventNative.sTAG, "destroy(" + this.mFlurryAdNative.toString() + ") started.");
            this.mFlurryAdNative.destroy();
            FlurryAgentWrapper.getInstance().endSession(this.mContext);
        }

        public synchronized void fetchAd() {
            if(AppUtil.sDebug)
                Log.d(FlurryCustomEventNative.sTAG, "Fetching Flurry Native Ad now.");

            this.mFlurryAdNative.setListener(this.mFlurryNativelistener);
            this.mFlurryAdNative.fetchAd();
        }

        public boolean isAppInstallAd() {
            return this.mFlurryAdNative.getAsset("secRatingImg") != null || this.mFlurryAdNative.getAsset("secHqRatingImg") != null || this.mFlurryAdNative.getAsset("appCategory") != null;
        }

        public void precacheImages() {
            NativeImageHelper.preCacheImages(this.mContext, this.getImageUrls(), new ImageListener() {
                public void onImagesCached() {
                    if(AppUtil.sDebug)
                        Log.d(FlurryCustomEventNative.sTAG, "preCacheImages: Ad image cached.");

                    FlurryMediationNativeAd.this.mCustomEventNativeListener.onNativeAdLoaded(FlurryMediationNativeAd.this);
                }

                public void onImagesFailedToCache(NativeErrorCode errorCode) {
                    if(AppUtil.sDebug)
                        Log.d(FlurryCustomEventNative.sTAG, "preCacheImages: Unable to cache Ad image. Error[" + errorCode.toString() + "]");

                    FlurryMediationNativeAd.this.mCustomEventNativeListener.onNativeAdFailed(errorCode);
                }
            });
        }

        @NonNull
        public List<String> getImageUrls() {
            List<String> imageUrls = new ArrayList(2);

            String mainImageUrl = this.getMainImageUrl();
            if (mainImageUrl != null) {
                imageUrls.add(mainImageUrl);

                if(AppUtil.sDebug)
                    Log.d(FlurryCustomEventNative.sTAG, "Flurry Native Ad main image found.");
            }

            String iconUrl = this.getIconImageUrl();
            if (iconUrl != null) {
                imageUrls.add(iconUrl);
                if(AppUtil.sDebug)
                    Log.d(FlurryCustomEventNative.sTAG, "Flurry Native Ad icon image found.");
            }

            String markerUrl = this.getSponsoredMarkerImageUrl();
            if (markerUrl!=null) {
                imageUrls.add(markerUrl);
                if(AppUtil.sDebug)
                    Log.d(FlurryCustomEventNative.sTAG, "Flurry Native Ad Sponsored Marker Image found.");
            }

            return imageUrls;
        }

        @Nullable
        public String getTitle() {
            return this.mTitle;
        }

        @Nullable
        public String getText() {
            return this.mText;
        }

        @Nullable
        public String getCallToAction() {
            return this.mCallToAction;
        }

        @Nullable
        public String getAdvertiserName() {
            return this.mAdvertiserName;
        }

        @Nullable
        public String getMainImageUrl() {
            return this.mMainImageUrl;
        }

        @Nullable
        public String getIconImageUrl() {
            return this.mIconImageUrl;
        }

        @Nullable
        public String getSponsoredMarkerImageUrl() {
            return this.mSponsoredMarkerImageUrl;
        }

        @Nullable
        public Double getStarRating() {
            return this.mStarRating;
        }

        @NonNull
        public Map<String, Object> getExtras() {
            return this.mExtras;
        }

        public void setTitle(@Nullable String title) {
            this.mTitle = title;
        }

        public void setText(@Nullable String text) {
            this.mText = text;
        }

        public void setAdvertiserName(@Nullable String name){
            this.mAdvertiserName = name;
        }

        public void setCallToAction(@Nullable String callToAction) {
            this.mCallToAction = callToAction;
        }

        public void setMainImageUrl(@Nullable String mainImageUrl) {
            this.mMainImageUrl = mainImageUrl;
        }

        public void setIconImageUrl(@Nullable String iconImageUrl) {
            this.mIconImageUrl = iconImageUrl;
        }

        public void setSponsoredMarkerImageUrl(@Nullable String imgUrl) {
            this.mSponsoredMarkerImageUrl = imgUrl;
        }

        public void setStarRating(@Nullable Double starRating) {
            this.mStarRating = starRating;
        }

        public void addExtra(@NonNull String key, @Nullable Object value) {
            this.mExtras.put(key, value);
        }

        public void onNativeAdLoaded() {
            this.mCustomEventNativeListener.onNativeAdLoaded(this);
        }

        boolean isVideoAd() {
            return this.mFlurryAdNative.isVideoAd();
        }

        void loadVideoIntoView(@NonNull ViewGroup videoView) {
            this.mFlurryAdNative.getAsset("videoUrl").loadAssetIntoView(videoView);
        }
    }
}
