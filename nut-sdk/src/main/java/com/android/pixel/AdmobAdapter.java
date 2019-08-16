package com.android.pixel;

import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.lang.ref.WeakReference;

public class AdmobAdapter {

    public static void initSdk(Context context, String publisherAppId){
        if(context!=null && !TextUtils.isEmpty(publisherAppId)) {
            if(AppUtil.sDebug) {
                MobileAds.initialize(context, "ca-app-pub-3940256099942544~3347511713");
            } else {
                //MobileAds.initialize(context, publisherAppId);
            }
        }
    }

    public static Object loadAd(Context context, AdType type, AdSize size, String adUnitId, AdLoadListener listener){
        if(context==null || TextUtils.isEmpty(adUnitId))
            return null;

        if(AdType.BANNER.equals(type)){
            if(AppUtil.sDebug){
                adUnitId = "ca-app-pub-3940256099942544/6300978111";
            }
            return loadBanner(context, size, adUnitId, listener);

        } else if(AdType.INTERSTITIAL.equals(type)){
            if(AppUtil.sDebug){
                adUnitId = "ca-app-pub-3940256099942544/1033173712";
            }
            if(DeltaAdSDK.isAdMobInterstitialCacheEnabled()) {
                return AdMobInterstitialCache.getInst().loadAd(adUnitId, listener);
            } else {
                return loadInterstitial(context, adUnitId, listener);
            }
        } else if (AdType.NATIVE.equals(type)) {
            if(AppUtil.sDebug){

            }
        } else if(AdType.REWARDEDVIDEO.equals(type)){
            if(AppUtil.sDebug){
                final String TEST_REWARDED_VIDEO = "ca-app-pub-3940256099942544/5224354917";
                adUnitId = TEST_REWARDED_VIDEO;
            }
            //return loadRewardedVideo(context, adUnitId, listener);
        }

        return null;
    }


    public static boolean isReady(Object ad){
        if(ad instanceof AdView){
            AdView banner = (AdView)ad;
            return true;
        }else if (ad instanceof InterstitialAd) {
            InterstitialAd interstitialAd = (InterstitialAd) ad;
            return interstitialAd.isLoaded();
        }

        return false;
    }


    public static void showAd(Object ad, ViewGroup container, AdSize size, AdDisplayListener listener){
        if(ad instanceof AdView){
            AdView banner = (AdView)ad;
            showBanner(banner, container, listener);
        } else if (ad instanceof InterstitialAd){
            InterstitialAd interstitialAd = (InterstitialAd) ad;
            showInterstitial(interstitialAd, listener);
        }/* else if(ad instanceof RewardedWrapper){
            RewardedWrapper rewardedWrapper = (RewardedWrapper) ad;
            showRewardedVideo(rewardedWrapper, listener);
        }*/

    }


    public static void destroyAd(Object ad){
        if (ad instanceof AdView) {
            ((AdView) ad).destroy();
        } else if (ad instanceof InterstitialAd){
            //admob doesn't provide interface to destroy interstitialAd
        }/* else if (ad instanceof RewardedWrapper) {
            RewardedWrapper rewardedWrapper = (RewardedWrapper) ad;
            rewardedWrapper.adObj.destroy(rewardedWrapper.contextRef.get());
        }*/
    }









    /////////////////////////////////////banner/////////////////////////////////////////////////////
    private static AdView loadBanner(Context context, AdSize size, String adUnitId, final AdLoadListener listener){
        if(context==null)
            return null;
        if(size==null || AdSize.FULLSCREEN.equals(size))
            return null;
        if(TextUtils.isEmpty(adUnitId))
            return null;

        final AdView banner = new AdView(context);
        if(AdSize.SMALL.equals(size)){
            banner.setAdSize(com.google.android.gms.ads.AdSize.BANNER);
        } else {
            banner.setAdSize(com.google.android.gms.ads.AdSize.MEDIUM_RECTANGLE);
        }
        banner.setAdUnitId(adUnitId);

        if(listener!=null){
            banner.setAdListener(new AdListener() {
                final WeakReference<AdLoadListener> listenerRef = new WeakReference<>(listener);
                @Override
                public void onAdFailedToLoad(int i) {
                        AdLoadListener listener = listenerRef.get();
                        if(listener!=null){
                            listener.onAdError(String.valueOf(i));
                        }
                }

                @Override
                public void onAdLoaded() {
                    AdLoadListener listener = listenerRef.get();
                    if(listener!=null){
                        listener.onAdLoaded(AdNetwork.ADMOB, AdType.BANNER, banner);
                    }
                }
            });
        }
        banner.loadAd(new AdRequest.Builder().build());

        return banner;
    }

    private static void showBanner(final AdView banner, ViewGroup container, final AdDisplayListener listener){
        if(banner.getParent()!=null){
            return;
        }

        if(listener!=null){
            banner.setAdListener(new AdListener() {
                final WeakReference<AdDisplayListener> listenerRef = new WeakReference<>(listener);
                // com.google.android.gms:play-services-ads:9.2.0
                // lifecycle: onAdLoaded --> click:onAdOpened,onAdLeftApplication
                @Override
                public void onAdLeftApplication() {
                    // com.google.firebase:firebase-ads:15.0.0
                    // after user clicking the ad, onAdOpened() is called first, and then onAdLeftApplication()
                    AdDisplayListener listener = listenerRef.get();
                    if (listener != null) {
                        listener.onAdClicked(AdNetwork.ADMOB, AdType.BANNER, banner.getAdUnitId());
                    }
                }
            });
        }
        container.addView(banner);
    }




    ///////////////////////////////////////////interstitial/////////////////////////////////////////
    private static InterstitialAd loadInterstitial(Context context, String adUnitId, final AdLoadListener listener){
        if(context==null || TextUtils.isEmpty(adUnitId))
            return null;

        final InterstitialAd ad = new InterstitialAd(context);
        ad.setAdUnitId(adUnitId);

        if(listener!=null){
            ad.setAdListener(new AdListener() {
                final WeakReference<AdLoadListener> listenerRef = new WeakReference<>(listener);
                @Override
                public void onAdFailedToLoad(int i) {
                    super.onAdFailedToLoad(i);
                    AdLoadListener listener = listenerRef.get();
                    if(listener!=null){
                        listener.onAdError(String.valueOf(i));
                    }
                }

                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    AdLoadListener listener = listenerRef.get();
                    if(listener!=null){
                        listener.onAdLoaded(AdNetwork.ADMOB, AdType.INTERSTITIAL, ad);
                    }
                }
            });
        }

        ad.loadAd(new AdRequest.Builder().build());
        return ad;
    }


    private static void showInterstitial(final InterstitialAd ad, final AdDisplayListener listener){
        ad.setAdListener(new AdListener() {
            final WeakReference<AdDisplayListener> listenerRef = new WeakReference<>(listener);
            @Override
            public void onAdOpened() {
                super.onAdOpened();
                AdDisplayListener listener = listenerRef.get();
                if(listener!=null){
                    listener.onAdDisplay(AdNetwork.ADMOB, AdType.INTERSTITIAL, ad.getAdUnitId());
                }
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
                AdDisplayListener listener = listenerRef.get();
                if(listener!=null){
                    listener.onAdClicked(AdNetwork.ADMOB, AdType.INTERSTITIAL, ad.getAdUnitId());
                }
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                AdDisplayListener listener = listenerRef.get();
                if(listener!=null){
                    listener.onAdClosed(AdNetwork.ADMOB, AdType.INTERSTITIAL, ad.getAdUnitId());
                }
                if(DeltaAdSDK.isAdMobInterstitialCacheEnabled()) {
                    AdMobInterstitialCache.getInst().cacheAd(ad.getAdUnitId());
                }
            }
        });

        ad.show();
    }








    /////////////////////////////////////////native/////////////////////////////////////////////////










///////////////////////////////////////////////rewarded video///////////////////////////////////////
//    private static RewardedWrapper loadRewardedVideo(Context context, String adUnitId, final AdLoadListener listener){
//        final RewardedVideoAd rewardedVideoAd = MobileAds.getRewardedVideoAdInstance(context);
//
//        final RewardedWrapper rewardedWrapper = new RewardedWrapper(adUnitId, rewardedVideoAd, context);
//        if(listener!=null){
//            rewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
//                WeakReference<RewardedWrapper> rewardedWrapperRef = new WeakReference<>(rewardedWrapper);
//                WeakReference<AdLoadListener> listenerRef = new WeakReference<>(listener);
//                @Override
//                public void onRewardedVideoAdFailedToLoad(int i) {
//                    rewardedVideoAd.setRewardedVideoAdListener(null);
//
//                    RewardedWrapper rewardedWrapper = rewardedWrapperRef.get();
//                    AdLoadListener listener = listenerRef.get();
//                    if(listener!=null && rewardedWrapper!=null){
//                        listener.onAdError(String.valueOf(i));
//                    }
//                }
//                @Override
//                public void onRewardedVideoAdLoaded() {
//                    rewardedVideoAd.setRewardedVideoAdListener(null);
//
//                    RewardedWrapper rewardedWrapper = rewardedWrapperRef.get();
//                    AdLoadListener listener = listenerRef.get();
//                    if(listener!=null && rewardedWrapper!=null){
//                        listener.onAdLoaded(AdNetwork.ADMOB, AdType.REWARDEDVIDEO, rewardedWrapper.adUnitId);
//                    }
//                }
//
//                @Override
//                public void onRewardedVideoAdOpened() { }
//
//                @Override
//                public void onRewardedVideoStarted() { }
//
//                @Override
//                public void onRewardedVideoAdClosed() { }
//
//                @Override
//                public void onRewarded(RewardItem rewardItem) { }
//
//                @Override
//                public void onRewardedVideoAdLeftApplication() { }
//
//            });
//        }
//
//        rewardedVideoAd.loadAd(adUnitId, new AdRequest.Builder().build());
//        return rewardedWrapper;
//    }
//
//    private static void showRewardedVideo(final RewardedWrapper rewardedWrapper, final AdDisplayListener listener){
//        final RewardedVideoAd rewardedVideoAd = rewardedWrapper.adObj;
//
//        if(!rewardedVideoAd.isLoaded()){
//            return;
//        }
//
//        if(listener!=null){
//            rewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
//                WeakReference<AdDisplayListener> listenerRef = new WeakReference<>(listener);
//
//                @Override
//                public void onRewardedVideoAdFailedToLoad(int i) { }
//                @Override
//                public void onRewardedVideoAdLoaded() { }
//
//                @Override
//                public void onRewardedVideoAdOpened() {
//
//                }
//
//                @Override
//                public void onRewardedVideoStarted() {
//                    AdDisplayListener listener = listenerRef.get();
//                    if(listener!=null){
//                        listener.onAdDisplay(AdNetwork.ADMOB, AdType.REWARDEDVIDEO, rewardedWrapper.adUnitId);
//                    }
//                }
//
//                @Override
//                public void onRewardedVideoAdClosed() {
//                    rewardedVideoAd.setRewardedVideoAdListener(null);
//
//                    AdDisplayListener listener = listenerRef.get();
//                    if(listener!=null){
//                        listener.onAdClosed(AdNetwork.ADMOB, AdType.REWARDEDVIDEO, rewardedWrapper.adUnitId);
//                    }
//                }
//
//                @Override
//                public void onRewarded(RewardItem rewardItem) {
//
//                }
//
//                @Override
//                public void onRewardedVideoAdLeftApplication() {
//                    AdDisplayListener listener = listenerRef.get();
//                    if(listener!=null){
//                        listener.onAdClicked(AdNetwork.ADMOB, AdType.REWARDEDVIDEO, rewardedWrapper.adUnitId);
//                    }
//                }
//
//
//            });
//        }
//
//        rewardedVideoAd.show();
//    }
//
//    private final static class RewardedWrapper{
//        String adUnitId;
//        RewardedVideoAd adObj;
//        WeakReference<Context> contextRef;
//
//        RewardedWrapper(String adUnitId, RewardedVideoAd ad, Context context){
//            this.adUnitId = adUnitId;
//            this.adObj = ad;
//            contextRef = new WeakReference<>(context);
//        }
//    }
}
