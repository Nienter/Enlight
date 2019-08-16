package com.mopub.nativeads;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.android.pixel.AppUtil;
import com.applovin.nativeAds.AppLovinNativeAd;
import com.applovin.nativeAds.AppLovinNativeAdLoadListener;
import com.applovin.nativeAds.AppLovinNativeAdPrecacheListener;
import com.applovin.nativeAds.AppLovinNativeAdService;
import com.applovin.sdk.AppLovinErrorCodes;
import com.applovin.sdk.AppLovinPostbackListener;
import com.applovin.sdk.AppLovinPrivacySettings;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkSettings;
import com.mopub.common.MoPub;
import com.mopub.common.Preconditions;
import com.mopub.common.logging.MoPubLog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppLovinMediationNative extends CustomEventNative {
    private static final Handler UI_HANDLER = new Handler(Looper.getMainLooper());

    @Override
    protected void loadNativeAd(@NonNull Context context,
                                @NonNull CustomEventNativeListener customEventNativeListener,
                                @NonNull Map<String, Object> localExtras,
                                @NonNull Map<String, String> serverExtras) {
        boolean canCollectPersonalInfo = MoPub.canCollectPersonalInformation();
        AppLovinPrivacySettings.setHasUserConsent(canCollectPersonalInfo, context);

        AppLovinSdk sdk = retrieveSdk(serverExtras, context);
        sdk.setPluginVersion("MoPub-3.1.0");
        sdk.setMediationProvider("mopub");

        final AppLovinMediationNativeAd applovinNativeAd = new AppLovinMediationNativeAd(context, sdk, customEventNativeListener);
        applovinNativeAd.loadAd();
    }



    static class AppLovinMediationNativeAd extends BaseNativeAd{
        private AppLovinSdk mSdk;
        /**
         * A custom event native listener used to forward Google Mobile Ads SDK events to MoPub.
         */
        private CustomEventNativeListener mCustomEventNativeListener;

        private final Map<String, Object> mExtras;

        private AppLovinNativeAd mNativeAd = null;

        private Context mAppContext = null;

        public AppLovinMediationNativeAd(Context context, AppLovinSdk sdk, CustomEventNativeListener customEventNativeListener){
            mAppContext = context.getApplicationContext();
            mSdk = sdk;
            mCustomEventNativeListener = customEventNativeListener;
            mExtras = new HashMap<String, Object>();
        }

        /**
         * Given a particular String key, return the associated Object value from the ad's extras map.
         * See {@link StaticNativeAd#getExtras()} for more information.
         */
        final public Object getExtra(final String key) {
            if (!Preconditions.NoThrow.checkNotNull(key, "getExtra key is not allowed to be null")) {
                return null;
            }
            return mExtras.get(key);
        }

        /**
         * Returns a copy of the extras map, reflecting additional ad content not reflected in any
         * of the above hardcoded setters. This is particularly useful for passing down custom fields
         * with MoPub's direct-sold native ads or from mediated networks that pass back additional
         * fields.
         */
        final public Map<String, Object> getExtras() {
            return new HashMap<String, Object>(mExtras);
        }

        final public void addExtra(final String key, final Object value) {
            if (!Preconditions.NoThrow.checkNotNull(key, "addExtra key is not allowed to be null")) {
                return;
            }
            mExtras.put(key, value);
        }

        public void loadAd(){

            final AppLovinNativeAdService nativeAdService = mSdk.getNativeAdService();
            nativeAdService.loadNextAd(new AppLovinNativeAdLoadListener() {
                @Override
                public void onNativeAdsLoaded(List<AppLovinNativeAd> nativeAds) {
                    mSdk.getNativeAdService().precacheResources(nativeAds.get(0), new AppLovinNativeAdPrecacheListener() {
                        @Override
                        public void onNativeAdImagesPrecached(AppLovinNativeAd ad) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String videoUrl = ad.getVideoUrl();
                                    if(TextUtils.isEmpty(videoUrl)){
                                        extractAdAsset(ad);
                                        if(mCustomEventNativeListener!=null){
                                            mCustomEventNativeListener.onNativeAdLoaded(AppLovinMediationNativeAd.this);
                                        }
                                    }
                                }
                            });

                        }

                        @Override
                        public void onNativeAdVideoPreceached(AppLovinNativeAd ad) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    extractAdAsset(ad);
                                    if(mCustomEventNativeListener!=null){
                                        mCustomEventNativeListener.onNativeAdLoaded(AppLovinMediationNativeAd.this);
                                    }
                                }
                            });

                        }

                        @Override
                        public void onNativeAdImagePrecachingFailed(AppLovinNativeAd ad, int errorCode) {
                            MoPubLog.d("AppLovin native mediation, Failed to precache native ad image with code: " + errorCode);

                            if(mCustomEventNativeListener!=null){
                                mCustomEventNativeListener.onNativeAdFailed(toMoPubNativeErrorCode(errorCode));
                            }
                        }

                        @Override
                        public void onNativeAdVideoPrecachingFailed(AppLovinNativeAd ad, int errorCode) {
                            MoPubLog.d("AppLovin native mediation, Failed to precache native ad video with code: " + errorCode);

                            if(mCustomEventNativeListener!=null){
                                mCustomEventNativeListener.onNativeAdFailed(toMoPubNativeErrorCode(errorCode));
                            }
                        }
                    });
                }

                @Override
                public void onNativeAdsFailedToLoad(int errorCode) {
                    MoPubLog.d("AppLovin native mediation, Failed to load native ad with code: " + errorCode);

                    if(mCustomEventNativeListener!=null){
                        mCustomEventNativeListener.onNativeAdFailed(toMoPubNativeErrorCode(errorCode));
                    }
                }
            });
        }

        private void extractAdAsset(AppLovinNativeAd nativeAd){
            mNativeAd = nativeAd;
        }

        public String getIconImageUrl(){
            String url = null;
            if(mNativeAd!=null)
                url = mNativeAd.getIconUrl();
            return url;
        }

        public Drawable getRatingImage(){
            final float starRating = mNativeAd==null ? 0: mNativeAd.getStarRating();

            final String sanitizedRating = Float.toString( starRating ).replace( ".", "_" );
            final String resourceName = "applovin_star_sprite_" + sanitizedRating;

            final int drawableId = mAppContext.getResources().getIdentifier( resourceName, "drawable", mAppContext.getPackageName() );

            return mAppContext.getResources().getDrawable( drawableId);
        }

        public String getTitle(){
            String str = "";
            if(mNativeAd!=null)
                str = mNativeAd.getTitle();
            return str;
        }

        public String getText(){
            String str = "";
            if(mNativeAd!=null)
                str = mNativeAd.getDescriptionText();
            return str;
        }

        public String getCta(){
            String str = "";
            if(mNativeAd!=null)
                str = mNativeAd.getCtaText();
            return str;
        }

        public void adIsClicked(View view){
            if(mNativeAd!=null) {
                notifyAdClicked();
                mNativeAd.launchClickTarget(view.getContext());
            }
        }

        public AppLovinNativeMediaView getMediaView(){
            AppLovinNativeMediaView mediaView = null;
            if(mNativeAd!=null
                    && (mNativeAd.isImagePrecached() || mNativeAd.isVideoPrecached())){
                mediaView = new AppLovinNativeMediaView( mAppContext );

                mediaView.setAd( mNativeAd );
                mediaView.setVideoState( new AppLovinNativeMediaView.AppLovinNativeVideoState() );
                mediaView.setSdk( mSdk );
                mediaView.setUiHandler( new Handler( Looper.getMainLooper() ) );
                mediaView.setUpView();
                mediaView.autoplayVideo();
            }

            return mediaView;
        }


        @Override
        public void prepare(@NonNull View view) {
            if(mNativeAd!=null){
                mNativeAd.trackImpression(new AppLovinPostbackListener() {
                    @Override
                    public void onPostbackSuccess(String url) {
                        if(mCustomEventNativeListener!=null){
                            notifyAdImpressed();
                        }
                    }

                    @Override
                    public void onPostbackFailure(String url, int errorCode) {
                        if(AppUtil.sDebug){
                            Log.d("Applovin native", "impression failure, url="+url+", errCode="+errorCode);
                        }
                    }
                });
            }
        }

        @Override
        public void clear(@NonNull View view) {
            // Called when an ad is no longer displayed to a user.
            //mCustomEventNativeListener=null;
        }

        @Override
        public void destroy() {
            mNativeAd=null;
        }
    }


    private static NativeErrorCode toMoPubNativeErrorCode(int applovinErrorCode) {
        if (applovinErrorCode == AppLovinErrorCodes.NO_FILL) {
            return NativeErrorCode.NETWORK_NO_FILL;
        } else if (applovinErrorCode == AppLovinErrorCodes.UNSPECIFIED_ERROR) {
            return NativeErrorCode.UNSPECIFIED;
        } else if (applovinErrorCode == AppLovinErrorCodes.NO_NETWORK) {
            return NativeErrorCode.CONNECTION_ERROR;
        } else if (applovinErrorCode == AppLovinErrorCodes.UNABLE_TO_PRECACHE_RESOURCES
                || applovinErrorCode == AppLovinErrorCodes.UNABLE_TO_PRECACHE_IMAGE_RESOURCES
                || applovinErrorCode == AppLovinErrorCodes.UNABLE_TO_PRECACHE_VIDEO_RESOURCES) {
            return NativeErrorCode.IMAGE_DOWNLOAD_FAILURE;
        } else if (applovinErrorCode == AppLovinErrorCodes.FETCH_AD_TIMEOUT) {
            return NativeErrorCode.NETWORK_TIMEOUT;
        } else {
            return NativeErrorCode.UNSPECIFIED;
        }
    }


    /**
     * Retrieves the appropriate instance of AppLovin's SDK from the SDK key given in the server parameters, or Android Manifest.
     */
    private static AppLovinSdk retrieveSdk(final Map<String, String> serverExtras, final Context context) {
        final String sdkKey = serverExtras != null ? serverExtras.get("sdk_key") : null;
        final AppLovinSdk sdk;

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
