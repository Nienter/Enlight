package com.android.pixel;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.mopub.common.MoPub;
import com.mopub.common.MoPubReward;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.SdkInitializationListener;
import com.mopub.common.util.Views;
import com.mopub.mobileads.DefaultBannerAdListener;
import com.mopub.mobileads.DefaultInterstitialAdListener;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubRewardedAd;
import com.mopub.mobileads.MoPubRewardedVideoListener;
import com.mopub.mobileads.MoPubRewardedVideoManager;
import com.mopub.mobileads.MoPubRewardedVideos;
import com.mopub.mobileads.MoPubView;
import com.mopub.nativeads.AdapterHelper;
import com.mopub.nativeads.AppLovinAdRenderer;
import com.mopub.nativeads.FlurryNativeAdRenderer;
import com.mopub.nativeads.MediaViewBinder;
import com.mopub.nativeads.MoPubAdRenderer;
import com.mopub.nativeads.MoPubNative;
import com.mopub.nativeads.MoPubStaticNativeAdRenderer;
import com.mopub.nativeads.MoPubVideoNativeAdRenderer;
import com.mopub.nativeads.NativeAd;
import com.mopub.nativeads.NativeErrorCode;
import com.mopub.nativeads.ViewBinder;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MopubAdapter {

    private static volatile boolean sInit = false;
    private static volatile boolean sInitInProgress = false;
    public static void init(Activity activity, String anyAdUnitId){
        if(!sInitInProgress && !sInit) {
            synchronized (MopubAdapter.class) {
                if(!sInitInProgress && !sInit) {
                    if (activity == null || TextUtils.isEmpty(anyAdUnitId))
                        return;

                    sInitInProgress = true;

                    List<String> networksToInit = new ArrayList<String>();
                    networksToInit.add("com.mopub.mobileads.GooglePlayServicesRewardedVideo");
                    networksToInit.add("com.mopub.mobileads.AdColonyRewardedVideo");
                    networksToInit.add("com.mopub.mobileads.ChartboostRewardedVideo");
                    networksToInit.add("com.mopub.mobileads.AppLovinRewardedVideo");

                    SdkConfiguration sdkConfig = new SdkConfiguration.Builder(anyAdUnitId)
                            .withNetworksToInit(networksToInit)
                            .build();
                    MoPub.initializeSdk(activity, sdkConfig, new SdkInitializationListener() {
                        @Override
                        public void onInitializationFinished() {
                            sInitInProgress = false;
                            sInit = MoPub.isSdkInitialized();

                /* MoPub SDK initialized.
                   Check if you should show the consent dialog here, and make your ad requests. */
                        }
                    });
                }
            }
        }
    }


    public static Object loadAd(Context context, AdType type, AdSize size, String adUnitId, AdLoadListener listener) {
        if (context == null || TextUtils.isEmpty(adUnitId))
            return null;

        if(!sInit && (context instanceof Activity)){
            Activity activity = (Activity) context;
            init(activity, adUnitId);
            return null;
        }

        if(AdType.BANNER.equals(type)){
            if(AppUtil.sDebug){
                if(AdSize.SMALL.equals(size)){
                    adUnitId = "b195f8dd8ded45fe847ad89ed1d016da" ;
                } else {
                    adUnitId = "252412d5e9364a05ab77d9396346d73d" ;
                }
            }
            return loadBanner(context, adUnitId, listener);
        } else if(AdType.INTERSTITIAL.equals(type)){
            if(AppUtil.sDebug){
                adUnitId = "24534e1901884e398f1253216226017e";
            }
            if(context instanceof Activity){
                Activity activity = (Activity)context;
                return loadInterstitial(activity, adUnitId, listener);
            }
        } else if (AdType.NATIVE.equals(type)) {
            if(AppUtil.sDebug){
                if(new Random(System.currentTimeMillis()).nextInt(100)>50){
                    adUnitId = "02a2d288d2674ad09f3241d46a44356e";//native video
                } else {
                    adUnitId = "11a17b188668469fb0412708c3d16813";//native static
                }
            }
            return loadNative(context, adUnitId, listener);
        } else if (AdType.REWARDEDVIDEO.equals(type)){
            if(context instanceof Activity) {
                Activity activity = (Activity)context;

                if (AppUtil.sDebug) {
                    final String TEST_REWARDED_video = "920b6145fb1546cf8b5cf2ac34638bb7";
                    final String TEST_REWARDED_playable = "15173ac6d3e54c9389b9a5ddca69b34b";
                    if (new Random(System.currentTimeMillis()).nextInt(100) >= 50) {
                        adUnitId = TEST_REWARDED_playable;
                    } else {
                        adUnitId = TEST_REWARDED_video;
                    }
                }
                return RewardedManager.getInst().load(activity, adUnitId, listener);
            }
        }

        return null;
    }

    public static boolean isReady(Object ad){
        if(ad instanceof MoPubView){
            MoPubView banner = (MoPubView)ad;
            return true;
        } else if(ad instanceof MoPubInterstitial) {
            MoPubInterstitial interstitial = (MoPubInterstitial) ad;
            return interstitial.isReady();
        } else if(ad instanceof NativeWrapper) {
            NativeWrapper nativeWrapper = (NativeWrapper) ad;
            return true;
        } else if(ad instanceof RewardedWrapper) {
            RewardedWrapper rewardedWrapper = (RewardedWrapper) ad;
            return MoPubRewardedVideos.hasRewardedVideo(rewardedWrapper.mAdUnitId);
        }

        return false;
    }

    public static void showAd(Object ad, ViewGroup container, AdSize size, AdDisplayListener listener){
        if(ad instanceof MoPubView){
            MoPubView banner = (MoPubView)ad;
            showBanner(banner, container, listener);
        } else if(ad instanceof MoPubInterstitial){
            MoPubInterstitial interstitial = (MoPubInterstitial) ad;
            showInterstitial(interstitial, listener);
        } else if(ad instanceof NativeWrapper){
            NativeWrapper nativeWrapper = (NativeWrapper)ad;
            showNative(nativeWrapper, container, size, listener);
        } else if(ad instanceof RewardedWrapper){
            RewardedWrapper rewardedWrapper = (RewardedWrapper)ad;
            RewardedManager.getInst().show(rewardedWrapper, listener);
        }
    }

    public static void destroyAd(Object ad){
        if (ad instanceof MoPubView){
            MoPubView banner = (MoPubView)ad;
            banner.destroy();
        } else if(ad instanceof MoPubInterstitial){
            MoPubInterstitial interstitial = (MoPubInterstitial) ad;
            interstitial.destroy();
        } else if(ad instanceof NativeWrapper){
            NativeWrapper nativeWrapper = (NativeWrapper)ad;
            if(nativeWrapper.moPubNative!=null){
                nativeWrapper.moPubNative.destroy();
                nativeWrapper.moPubNative=null;
            }
            if(nativeWrapper.nativeAd!=null){
                nativeWrapper.nativeAd.destroy();
                nativeWrapper.nativeAd=null;
            }
        } else if(ad instanceof RewardedWrapper){
            RewardedWrapper rewardedWrapper = (RewardedWrapper)ad;
            RewardedManager.getInst().destroy(rewardedWrapper);
        }
    }



















////////////////////////////////banner//////////////////////////////////////////////////////////////
    private static MoPubView loadBanner(Context context, String adUnitId, final AdLoadListener listener){
        MoPubView banner = new MoPubView(context);
        banner.setAdUnitId(adUnitId);
        banner.setAutorefreshEnabled(true);

        if(listener!=null){
            banner.setBannerAdListener(new DefaultBannerAdListener() {
                final WeakReference<AdLoadListener> listenerRef = new WeakReference<>(listener);
                @Override
                public void onBannerLoaded(MoPubView banner) {
                    AdLoadListener listener = listenerRef.get();
                    if(listener!=null){
                        listener.onAdLoaded(AdNetwork.MOPUB, AdType.BANNER, banner);
                    }
                }

                @Override
                public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode) {
                    AdLoadListener listener = listenerRef.get();
                    if(listener!=null){
                        listener.onAdError(errorCode.toString());
                    }
                }
            });
        }

        banner.loadAd();

        return banner;
    }

    private static void showBanner(MoPubView banner, ViewGroup container, final AdDisplayListener listener){

        if(listener!=null){
            banner.setBannerAdListener(new DefaultBannerAdListener(){
                final WeakReference<AdDisplayListener> listenerRef = new WeakReference<>(listener);
                @Override
                public void onBannerClicked(MoPubView banner){
                    AdDisplayListener listener = listenerRef.get();
                    if(listener!=null){
                        listener.onAdClicked(AdNetwork.MOPUB, AdType.BANNER, banner.getAdUnitId());
                    }
                }
            });
        }

        Views.removeFromParent(banner);
        container.removeAllViews();
        container.addView(banner);
    }













/////////////////////////////////interstitial///////////////////////////////////////////////////////
    private static MoPubInterstitial loadInterstitial(Activity activity, String adUnitId, final AdLoadListener listener) {
        MoPubInterstitial interstitial = new MoPubInterstitial(activity, adUnitId);

        if(listener!=null){
            interstitial.setInterstitialAdListener(new DefaultInterstitialAdListener() {
                final WeakReference<AdLoadListener> listenerRef = new WeakReference<>(listener);
                @Override
                public void onInterstitialLoaded(MoPubInterstitial interstitial) {
                    AdLoadListener listener = listenerRef.get();
                    if(listener!=null){
                        listener.onAdLoaded(AdNetwork.MOPUB, AdType.INTERSTITIAL, interstitial);
                    }
                }

                @Override
                public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
                    AdLoadListener listener = listenerRef.get();
                    if(listener!=null){
                        listener.onAdError(errorCode.toString());
                    }
                }
            });
        }

        interstitial.load();
        return interstitial;
    }

    private static void showInterstitial(final MoPubInterstitial interstitial, final AdDisplayListener listener) {
        if(listener!=null){
            interstitial.setInterstitialAdListener(new DefaultInterstitialAdListener(){
                final WeakReference<AdDisplayListener> listenerRef = new WeakReference<>(listener);
                @Override
                public void onInterstitialShown(MoPubInterstitial interstitial) {
                    AdDisplayListener listener = listenerRef.get();
                    if(listener!=null){
                        listener.onAdDisplay(AdNetwork.MOPUB, AdType.INTERSTITIAL, "unknown_id");
                    }
                }

                @Override
                public void onInterstitialClicked(MoPubInterstitial interstitial) {
                    AdDisplayListener listener = listenerRef.get();
                    if(listener!=null){
                        listener.onAdClicked(AdNetwork.MOPUB, AdType.INTERSTITIAL, "unknown_id");
                    }
                }

                @Override
                public void onInterstitialDismissed(MoPubInterstitial interstitial) {
                    AdDisplayListener listener = listenerRef.get();
                    if(listener!=null){
                        listener.onAdClosed(AdNetwork.MOPUB, AdType.INTERSTITIAL, "unknown_id");
                    }
                }
            });
        }

        interstitial.show();
    }




///////////////////////////////////////////////native///////////////////////////////////////////////
    private static NativeWrapper loadNative(Context context, String adUnitId, final AdLoadListener listener){
        final NativeWrapper nativeWrapper = new NativeWrapper();

        MoPubNative.MoPubNativeNetworkListener networkListener = new MoPubNative.MoPubNativeNetworkListener() {
                final WeakReference<AdLoadListener> listenerRef = new WeakReference<>(listener);
                @Override
                public void onNativeLoad(NativeAd nativeAd) {
                    nativeWrapper.nativeAd = nativeAd;
                    AdLoadListener listener = listenerRef.get();
                    if(listener!=null){
                        listener.onAdLoaded(AdNetwork.MOPUB, AdType.NATIVE, nativeWrapper);
                    }
                }

                @Override
                public void onNativeFail(NativeErrorCode errorCode) {
                    AdLoadListener listener = listenerRef.get();
                    if(listener!=null){
                        listener.onAdError(errorCode.toString());
                    }
                }
            };


        final MoPubNative moPubNative = new MoPubNative(context, adUnitId, networkListener);
        nativeWrapper.moPubNative = moPubNative;

        //Per Mopub documentation, The video renderer must be declared before the MoPubStaticNativeAdRenderer
        //However, in current version v5.4.1,  VideoNativeAd doesn't inherit from StaticNativeAd.
        //check MoPubVideoNativeAdRenderer#supports & MoPubStaticNativeAdRenderer#supports for details
        moPubNative.registerAdRenderer(new MoPubVideoNativeAdRenderer(
                new MediaViewBinder.Builder(R.layout.layout_mopub_native)
                        .iconImageId(R.id.mopub_ad_icon)
                        .titleId(R.id.mopub_ad_title)
                        .textId(R.id.mopub_ad_text)
                        .mediaLayoutId(R.id.mopub_ad_main_media)
                        .callToActionId(R.id.mopub_ad_cta)
                        .privacyInformationIconImageId(R.id.mopub_privacy_info_icon_img)
                        .build()));

        moPubNative.registerAdRenderer(new MoPubStaticNativeAdRenderer(
                new ViewBinder.Builder(R.layout.layout_mopub_native)
                .iconImageId(R.id.mopub_ad_icon)
                .titleId(R.id.mopub_ad_title)
                .textId(R.id.mopub_ad_text)
                .mainImageId(R.id.mopub_ad_main_img)
                .callToActionId(R.id.mopub_ad_cta)
                .privacyInformationIconImageId(R.id.mopub_privacy_info_icon_img)
                .build()));



        moPubNative.registerAdRenderer(new AppLovinAdRenderer(
                new AppLovinAdRenderer.AppLovinViewBinder.Builder(R.layout.layout_applovin_native)
                        .iconImageId(R.id.applovin_native_appIcon)
                        .ratingImageId(R.id.applovin_native_appRating)
                        .titleId(R.id.applovin_native_appTitle)
                        .textId(R.id.applovin_native_appDescription)
                        .callToActionId(R.id.applovin_native_CTABtn)
                        .mediaPlaceHolderId(R.id.applovin_native_media_Placeholder)
                        .build()));

        moPubNative.registerAdRenderer(new FlurryNativeAdRenderer(
                new FlurryNativeAdRenderer.FlurryViewBinder.Builder(new ViewBinder.Builder(R.layout.layout_flurry_native)
                        .iconImageId(R.id.flurry_ad_icon)
                        .titleId(R.id.flurry_ad_title)
                        .callToActionId(R.id.flurry_ad_cta)
                        .mainImageId(R.id.flurry_ad_media_img)
                        .privacyInformationIconImageId(R.id.flurry_ad_sponsored_marker)
                        .build())
                        .advertiserNameViewId(R.id.flurry_ad_source)
                        .videoViewId(R.id.flurry_ad_media_video)
                        .build()));

        moPubNative.makeRequest();

        return nativeWrapper;
    }

    private static void showNative(NativeWrapper nativeWrapper, ViewGroup container, AdSize size, final AdDisplayListener listener){
        final NativeAd nativeAd = nativeWrapper.nativeAd;
        if(nativeAd==null || nativeAd.isDestroyed()){
            return;
        }

        if(listener!=null){
            nativeAd.setMoPubNativeEventListener(new NativeAd.MoPubNativeEventListener() {
                final WeakReference<AdDisplayListener> listenerRef = new WeakReference<>(listener);
                @Override
                public void onImpression(View view) {
                    AdDisplayListener listener = listenerRef.get();
                    if (listener!=null){
                        listener.onAdDisplay(AdNetwork.MOPUB, AdType.NATIVE, nativeAd.getAdUnitId());
                    }
                }

                @Override
                public void onClick(View view) {
                    AdDisplayListener listener = listenerRef.get();
                    if (listener!=null){
                        listener.onAdClicked(AdNetwork.MOPUB, AdType.NATIVE, nativeAd.getAdUnitId());
                    }
                }
            });
        }

        AdapterHelper adapterHelper = new AdapterHelper(container.getContext(), 0, 3);
        View adView = adapterHelper.getAdView(null, container, nativeAd, new ViewBinder.Builder(0).build());

        MoPubAdRenderer renderer = nativeAd.getMoPubAdRenderer();
        if(renderer instanceof MoPubStaticNativeAdRenderer){
            adView.findViewById(R.id.mopub_ad_main_img).setVisibility(View.VISIBLE);
        } else if(renderer instanceof MoPubVideoNativeAdRenderer){
            adView.findViewById(R.id.mopub_ad_main_media).setVisibility(View.VISIBLE);
        } else if(renderer instanceof AppLovinAdRenderer){
        } else if(renderer instanceof FlurryNativeAdRenderer){
        }

        if(AdSize.SMALL.equals(size)){
            if ((renderer instanceof MoPubStaticNativeAdRenderer)
                    || (renderer instanceof MoPubVideoNativeAdRenderer)) {
                adView.findViewById(R.id.mopub_ad_main_container).setVisibility(View.GONE);
            } else if (renderer instanceof AppLovinAdRenderer){
                adView.findViewById(R.id.applovin_native_media_Placeholder).setVisibility(View.GONE);
            } else if (renderer instanceof FlurryNativeAdRenderer){
                adView.findViewById(R.id.flurry_ad_media_container).setVisibility(View.GONE);
            }
        }

        container.removeAllViews();
        container.addView(adView);
    }


    private static final class NativeWrapper{
        NativeAd nativeAd=null;
        MoPubNative moPubNative = null;
    }



///////////////////////////////////////////////rewarded//////////////////////////////////////////////
private static final class RewardedManager{
    private static volatile RewardedManager sInst = null;
    public static RewardedManager getInst(){
        if(sInst == null){
            synchronized (RewardedManager.class){
                if(sInst == null){
                    sInst = new RewardedManager();
                }
            }
        }

        return sInst;
    }

    MoPubRewardedVideoListener mGlobalListener = null;
    RewardedManager(){
        mGlobalListener = new MoPubRewardedVideoListener() {
            @Override
            public void onRewardedVideoLoadSuccess(@NonNull String adUnitId) {
                WeakReference<RewardedWrapper> rewardedWrapperRef = mListenerMap.remove(adUnitId);

                if(rewardedWrapperRef!=null && rewardedWrapperRef.get()!=null
                        && (rewardedWrapperRef.get().mLoadListener!=null)){
                    AdLoadListener listener = rewardedWrapperRef.get().mLoadListener;
                    listener.onAdLoaded(AdNetwork.MOPUB, AdType.REWARDEDVIDEO, rewardedWrapperRef.get());
                }
            }

            @Override
            public void onRewardedVideoLoadFailure(@NonNull String adUnitId, @NonNull MoPubErrorCode errorCode) {
                WeakReference<RewardedWrapper> rewardedWrapperRef = mListenerMap.remove(adUnitId);

                if(rewardedWrapperRef!=null && rewardedWrapperRef.get()!=null
                        && (rewardedWrapperRef.get().mLoadListener!=null)){
                    AdLoadListener listener = rewardedWrapperRef.get().mLoadListener;
                    listener.onAdError(errorCode.toString());
                }
            }

            @Override
            public void onRewardedVideoStarted(@NonNull String adUnitId) {
                WeakReference<RewardedWrapper> rewardedWrapperRef = mListenerMap.get(adUnitId);

                if(rewardedWrapperRef!=null && rewardedWrapperRef.get()!=null
                        && (rewardedWrapperRef.get().mDisplayListener !=null)){
                    AdDisplayListener listener = rewardedWrapperRef.get().mDisplayListener;
                    listener.onAdDisplay(AdNetwork.MOPUB, AdType.REWARDEDVIDEO, adUnitId);
                }
            }

            @Override
            public void onRewardedVideoPlaybackError(@NonNull String adUnitId, @NonNull MoPubErrorCode errorCode) {
                if(AppUtil.sDebug){
                    boolean probe=false;
                }
            }

            @Override
            public void onRewardedVideoClicked(@NonNull String adUnitId) {
                WeakReference<RewardedWrapper> rewardedWrapperRef = mListenerMap.get(adUnitId);

                if(rewardedWrapperRef!=null && rewardedWrapperRef.get()!=null
                        && (rewardedWrapperRef.get().mDisplayListener !=null)){
                    AdDisplayListener listener = rewardedWrapperRef.get().mDisplayListener;
                    listener.onAdClicked(AdNetwork.MOPUB, AdType.REWARDEDVIDEO, adUnitId);
                }
            }

            @Override
            public void onRewardedVideoClosed(@NonNull String adUnitId) {
                WeakReference<RewardedWrapper> rewardedWrapperRef = mListenerMap.remove(adUnitId);

                if(rewardedWrapperRef!=null && rewardedWrapperRef.get()!=null
                        && (rewardedWrapperRef.get().mDisplayListener !=null)){
                    AdDisplayListener listener = rewardedWrapperRef.get().mDisplayListener;
                    listener.onAdClosed(AdNetwork.MOPUB, AdType.REWARDEDVIDEO, adUnitId, rewardedWrapperRef.get().mRewardAmount);
                }
            }

            @Override
            public void onRewardedVideoCompleted(@NonNull Set<String> adUnitIds, @NonNull MoPubReward reward) {
                for (String adUnitId:adUnitIds) {
                    if(reward.isSuccessful()){
                        WeakReference<RewardedWrapper> rewardedWrapperRef = mListenerMap.get(adUnitId);
                        if(rewardedWrapperRef!=null && rewardedWrapperRef.get()!=null){
                            RewardedWrapper rewardedWrapper= rewardedWrapperRef.get();
                            rewardedWrapper.mRewardSuccess = reward.isSuccessful();
                            rewardedWrapper.mRewardLabel = reward.getLabel();
                            rewardedWrapper.mRewardAmount = reward.getAmount();
                        }
                    }
                }
                if(AppUtil.sDebug){
                    int amount = reward.getAmount();
                    String label = reward.getLabel();
                    boolean isSuccessful = reward.isSuccessful();
                    boolean probe=false;
                }
            }
        };

        MoPubRewardedVideos.setRewardedVideoListener(mGlobalListener);
    }

    private Map<String, WeakReference<RewardedWrapper>> mListenerMap = new ConcurrentHashMap<>();
    RewardedWrapper load(Activity activity, String adUnitId, AdLoadListener listener){
        RewardedWrapper rewardedWrapper = new RewardedWrapper();
        rewardedWrapper.mAdUnitId = adUnitId;
        rewardedWrapper.mLoadListener = listener;

        synchronized (RewardedManager.class) {
            if (mListenerMap.containsKey(adUnitId)) {
                WeakReference<RewardedWrapper> rewardedWrapperRef = mListenerMap.get(adUnitId);
                if (rewardedWrapperRef.get() != null) {// the previous ad does not exit

                    if(MoPubRewardedVideos.hasRewardedVideo(adUnitId)){
                        mListenerMap.remove(adUnitId).get();
                    }else {
                        mListenerMap.put(adUnitId, new WeakReference<>(rewardedWrapper));

                        MoPubRewardedVideoManager.updateActivity(activity);
                        return rewardedWrapper;
                    }
                }
            }
            mListenerMap.put(adUnitId, new WeakReference<>(rewardedWrapper));
        }

        MoPubRewardedVideoManager.updateActivity(activity);
        MoPubRewardedVideos.loadRewardedVideo(adUnitId);

        return rewardedWrapper;
    }

    void show(RewardedWrapper ad, AdDisplayListener listener){
        ad.mDisplayListener = listener;
        String adUnitId = ad.mAdUnitId;

        synchronized (RewardedManager.class) {
            if (mListenerMap.containsKey(adUnitId)) {
                WeakReference<RewardedWrapper> rewardedWrapperRef = mListenerMap.get(adUnitId);
                if (rewardedWrapperRef.get() != null) {// the previous ad does not exit
                    return ;
                }
            }
            mListenerMap.put(adUnitId, new WeakReference<>(ad));
        }

        if(!MoPubRewardedVideos.hasRewardedVideo(adUnitId)){
            mListenerMap.remove(adUnitId);
            return;
        }

        MoPubRewardedVideos.showRewardedVideo(adUnitId);
    }

    void destroy(RewardedWrapper ad){
        String adUnitId =ad.mAdUnitId;
        if(TextUtils.isEmpty(adUnitId))
            return;

        if(mListenerMap.containsKey(adUnitId)) {
            WeakReference<RewardedWrapper> rewardedWrapperRef = mListenerMap.get(adUnitId);

            boolean remove = false;
            if(rewardedWrapperRef==null || rewardedWrapperRef.get()==null){
                remove = true;
            } else if(rewardedWrapperRef.get().equals(ad)){
                remove = true;
            }
            if(remove) {
                mListenerMap.remove(adUnitId);
            }
        }
    }
}


    private static final class RewardedWrapper{
        String mAdUnitId=null;
        MoPubRewardedAd mAd=null;

        AdLoadListener mLoadListener=null;
        AdDisplayListener mDisplayListener=null;

        boolean mRewardSuccess=false;
        String  mRewardLabel=null;
        int     mRewardAmount=0;
    }
}
