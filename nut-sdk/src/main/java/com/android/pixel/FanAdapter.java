package com.android.pixel;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.ads.AbstractAdListener;
import com.facebook.ads.Ad;
import com.facebook.ads.AdChoicesView;
import com.facebook.ads.AdError;
import com.facebook.ads.AdIconView;
import com.facebook.ads.AdView;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdListener;
import com.facebook.ads.NativeBannerAd;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class FanAdapter {

    private static boolean sFbAvail = false;
    public static void init(Context context){
        sFbAvail = AppUtil.isAppInstalled(context, "com.facebook.katana")
                || AppUtil.isAppInstalled(context, "com.facebook.lite")
                || AppUtil.isAppInstalled(context, "com.facebook.mlite")
                || AppUtil.isAppInstalled(context, "com.facebook.orca");
    }

    public static boolean isFbAvail(){
        return sFbAvail;
    }

    public static Object loadAd(Context context, AdType type, AdSize size, String adUnitId, AdLoadListener listener){
        if(!sFbAvail)
            return null;
        if(context==null || TextUtils.isEmpty(adUnitId))
            return null;

        if(AdType.BANNER.equals(type)){
            if(AppUtil.sDebug){
                adUnitId = "IMG_16_9_LINK#YOUR_PLACEMENT_ID";
            }
            return loadBanner(context, size, adUnitId, listener);

        } else if(AdType.INTERSTITIAL.equals(type)){
            if(AppUtil.sDebug){
                adUnitId = "IMG_16_9_LINK#YOUR_PLACEMENT_ID";
                adUnitId = "VID_HD_16_9_46S_APP_INSTALL#YOUR_PLACEMENT_ID";
            }
            return loadInterstitial(context, adUnitId, listener);

        } else if(AdType.NATIVE.equals(type)){
           if(AppUtil.sDebug) {
               adUnitId = "IMG_16_9_LINK#YOUR_PLACEMENT_ID";
               adUnitId = "VID_HD_16_9_46S_APP_INSTALL#YOUR_PLACEMENT_ID";
           }
           if(AdSize.SMALL.equals(size)){
                return loadNativeSmall(context, adUnitId, listener);
           } else {
               return loadNative(context, adUnitId, listener);
           }
        }

        return null;
    }

    public static boolean isReady(Object ad){
        if(ad instanceof AdView) {
            AdView banner = (AdView) ad;
            return true;
        } else if(ad instanceof InterstitialAd) {
            InterstitialAd interstitialAd = (InterstitialAd) ad;
            return interstitialAd.isAdLoaded();
        } else if(ad instanceof NativeAd) {
            NativeAd nativeAd = (NativeAd) ad;
            return nativeAd.isAdLoaded();
        } else if(ad instanceof NativeBannerAd) {
            NativeBannerAd nativeBannerAd = (NativeBannerAd) ad;
            return nativeBannerAd.isAdLoaded();
        }

        return false;
    }

    public static void showAd(Object ad, ViewGroup container, AdSize size, AdDisplayListener listener){
        if(ad instanceof AdView){
            AdView banner = (AdView) ad;
            showBanner(banner, container, listener);
        } else if(ad instanceof InterstitialAd){
            InterstitialAd interstitialAd = (InterstitialAd)ad;
            showInterstitial(interstitialAd, listener);
        } else if(ad instanceof NativeAd){
            NativeAd nativeAd = (NativeAd) ad;
            showNative(nativeAd, container, listener);
        } else if(ad instanceof NativeBannerAd){
            NativeBannerAd nativeBannerAd = (NativeBannerAd) ad;
            showNativeSmall(nativeBannerAd, container, listener);
        }
    }

    public static void destroyAd(Object ad){
        if(ad instanceof AdView) {
            AdView banner = (AdView) ad;
            banner.destroy();
        } else if(ad instanceof InterstitialAd){
            InterstitialAd interstitialAd = (InterstitialAd) ad;
            interstitialAd.destroy();
        } else if(ad instanceof NativeAd){
            NativeAd nativeAd = (NativeAd) ad;
            nativeAd.destroy();
        } else if(ad instanceof NativeBannerAd){
            NativeBannerAd nativeBannerAd = (NativeBannerAd) ad;
            nativeBannerAd.destroy();
        }
    }







///////////////////////////////////////////////banner///////////////////////////////////////////////
    private static AdView loadBanner(Context context, AdSize size, String adUnitId, final AdLoadListener listener){

        com.facebook.ads.AdSize fbSize = com.facebook.ads.AdSize.RECTANGLE_HEIGHT_250;
        if (AdSize.SMALL.equals(size)) {
            fbSize = com.facebook.ads.AdSize.BANNER_HEIGHT_50;
        }

        final AdView banner = new AdView(context, adUnitId, fbSize);
        if(listener!=null){
            banner.setAdListener(new AbstractAdListener() {
                final WeakReference<AdLoadListener> listenerRef = new WeakReference<>(listener);

                @Override
                public void onError(Ad ad, AdError adError) {
                    super.onError(ad, adError);
                    String id = ad.getPlacementId();

                    AdLoadListener listener = listenerRef.get();
                    if(listener!=null){

                        listener.onAdError(adError.getErrorMessage());
                    }
                }

                @Override
                public void onAdLoaded(Ad ad) {
                    super.onAdLoaded(ad);
                    AdLoadListener listener = listenerRef.get();
                    if(listener!=null){
                        listener.onAdLoaded(AdNetwork.FACEBOOK, AdType.BANNER, banner);
                    }
                }
            });
        }
        banner.loadAd();
        return banner;
    }


    private static void showBanner(final AdView banner, ViewGroup container, final AdDisplayListener listener){
        if(banner.getParent()!=null){
            return;
        }

        if(listener!=null){
            banner.setAdListener(new AbstractAdListener() {
                final WeakReference<AdDisplayListener> listenerRef = new WeakReference<>(listener);
                @Override
                public void onAdClicked(Ad ad) {
                    super.onAdClicked(ad);
                    AdDisplayListener listener = listenerRef.get();
                    if(listener!=null){
                        listener.onAdClicked(AdNetwork.FACEBOOK, AdType.BANNER, banner.getPlacementId());
                    }
                }

                @Override
                public void onLoggingImpression(Ad ad) {
                    super.onLoggingImpression(ad);
                    AdDisplayListener listener = listenerRef.get();
                    if(listener!=null){
                        listener.onAdDisplay(AdNetwork.FACEBOOK, AdType.BANNER, banner.getPlacementId());
                    }
                }
            });
        }
        container.addView(banner);
    }










/////////////////////////////////////interstitial///////////////////////////////////////////////////
    private static InterstitialAd loadInterstitial(Context context, String adUnitId, final AdLoadListener listener){
        final InterstitialAd interstitialAd = new InterstitialAd(context, adUnitId);

        if(listener!=null){
            interstitialAd.setAdListener(new AbstractAdListener() {
                final WeakReference<AdLoadListener> listenerRef = new WeakReference<>(listener);

                @Override
                public void onError(Ad ad, AdError adError) {
                    super.onError(ad, adError);
                    String id = ad.getPlacementId();

                    AdLoadListener listener = listenerRef.get();
                    if(listener!=null){
                        listener.onAdError(adError.getErrorMessage());
                    }
                }

                @Override
                public void onAdLoaded(Ad ad) {
                    super.onAdLoaded(ad);
                    AdLoadListener listener = listenerRef.get();
                    if(listener!=null){
                        listener.onAdLoaded(AdNetwork.FACEBOOK, AdType.INTERSTITIAL, interstitialAd);
                    }
                }
            });
        }

        interstitialAd.loadAd();
        return interstitialAd;
    }

    private static void showInterstitial(InterstitialAd interstitialAd, final AdDisplayListener listener){
        if(listener!=null){
            interstitialAd.setAdListener(new AbstractAdListener() {
                final WeakReference<AdDisplayListener> listenerRef = new WeakReference<>(listener);
                @Override
                public void onLoggingImpression(Ad ad) {
                    super.onLoggingImpression(ad);
                    AdDisplayListener listener = listenerRef.get();
                    if(listener!=null){
                        listener.onAdDisplay(AdNetwork.FACEBOOK, AdType.INTERSTITIAL, ad.getPlacementId());
                    }
                }

                @Override
                public void onAdClicked(Ad ad) {
                    super.onAdClicked(ad);
                    AdDisplayListener listener = listenerRef.get();
                    if(listener!=null){
                        listener.onAdClicked(AdNetwork.FACEBOOK, AdType.INTERSTITIAL, ad.getPlacementId());
                    }
                }

                @Override
                public void onInterstitialDismissed(Ad ad) {
                    super.onInterstitialDismissed(ad);
                    AdDisplayListener listener = listenerRef.get();
                    if(listener!=null){
                        listener.onAdClosed(AdNetwork.FACEBOOK, AdType.INTERSTITIAL, ad.getPlacementId());
                    }
                }

            });
        }

        interstitialAd.show();
    }












//////////////////////////////////////////////native////////////////////////////////////////////////
    private static NativeAd loadNative(Context context, String adUnitId, final AdLoadListener listener){
        NativeAd nativeAd = new NativeAd(context, adUnitId);

        if(listener!=null){
            nativeAd.setAdListener(new NativeAdListener() {
                final WeakReference<AdLoadListener> listenerRef = new WeakReference<>(listener);

                @Override
                public void onMediaDownloaded(Ad ad) { }

                @Override
                public void onError(Ad ad, AdError adError) {
                    AdLoadListener listener = listenerRef.get();
                    String id = ad.getPlacementId();

                    if(listener!=null){
                        listener.onAdError(adError.getErrorMessage());
                    }
                }

                @Override
                public void onAdLoaded(Ad ad) {
                    AdLoadListener listener = listenerRef.get();
                    if(listener!=null){
                        listener.onAdLoaded(AdNetwork.FACEBOOK, AdType.NATIVE, ad);
                    }
                }

                @Override
                public void onAdClicked(Ad ad) { }

                @Override
                public void onLoggingImpression(Ad ad) { }
            });
        }

        nativeAd.loadAd();
        return nativeAd;
    }

    private static NativeBannerAd loadNativeSmall(Context context, String adUnitId, final AdLoadListener listener){
        NativeBannerAd nativeBannerAd = new NativeBannerAd(context, adUnitId);

        if(listener!=null){
            nativeBannerAd.setAdListener(new NativeAdListener() {
                final WeakReference<AdLoadListener> listenerRef = new WeakReference<>(listener);

                @Override
                public void onMediaDownloaded(Ad ad) { }

                @Override
                public void onError(Ad ad, AdError adError) {
                    AdLoadListener listener = listenerRef.get();
                    String id = ad.getPlacementId();

                    if(listener!=null){
                        listener.onAdError(adError.getErrorMessage());
                    }
                }

                @Override
                public void onAdLoaded(Ad ad) {
                    AdLoadListener listener = listenerRef.get();
                    if(listener!=null){
                        listener.onAdLoaded(AdNetwork.FACEBOOK, AdType.NATIVE, ad);
                    }
                }

                @Override
                public void onAdClicked(Ad ad) { }

                @Override
                public void onLoggingImpression(Ad ad) { }
            });
        }

        nativeBannerAd.loadAd();
        return nativeBannerAd;
    }

    private static void showNative(NativeAd nativeAd, ViewGroup container, final AdDisplayListener listener){
        nativeAd.unregisterView();

        if(listener!=null){
            nativeAd.setAdListener(new NativeAdListener() {
                final WeakReference<AdDisplayListener> listenerRef = new WeakReference<>(listener);
                @Override
                public void onMediaDownloaded(Ad ad) { }
                @Override
                public void onError(Ad ad, AdError adError) { }
                @Override
                public void onAdLoaded(Ad ad) {}
                @Override
                public void onLoggingImpression(Ad ad) {
                    AdDisplayListener listener = listenerRef.get();
                    if(listener!=null){
                        listener.onAdDisplay(AdNetwork.FACEBOOK, AdType.NATIVE, ad.getPlacementId());
                    }
                }

                @Override
                public void onAdClicked(Ad ad) {
                    AdDisplayListener listener = listenerRef.get();
                    if(listener!=null){
                        listener.onAdClicked(AdNetwork.FACEBOOK, AdType.NATIVE, ad.getPlacementId());
                    }
                }
            });
        }

        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        View adView = inflater.inflate(R.layout.layout_facebook_native, container, false);
        container.addView(adView);

        // Add the AdChoices icon
        ViewGroup adChoicesContainer = adView.findViewById(R.id.ad_choices_container);
        AdChoicesView adChoicesView = new AdChoicesView(container.getContext(), nativeAd, true);
        adChoicesContainer.removeAllViews();
        adChoicesContainer.addView(adChoicesView, 0);

        // Create native UI using the ad metadata.
        AdIconView nativeAdIcon = adView.findViewById(R.id.fb_ad_icon);
        TextView nativeAdName = adView.findViewById(R.id.fb_ad_name);
        MediaView nativeAdMedia = adView.findViewById(R.id.fb_ad_media);
        TextView nativeAdSocialContext = adView.findViewById(R.id.fb_ad_social_context);
        TextView nativeAdBody = adView.findViewById(R.id.fb_ad_body);
        TextView sponsoredLabel = adView.findViewById(R.id.fb_ad_sponsored_label);
        Button nativeAdCallToAction = adView.findViewById(R.id.fb_ad_call_to_action);

        // Set the Text.
        String adNameStr = nativeAd.getAdvertiserName();
        if(adNameStr.length()>25) adNameStr = adNameStr.substring(0,25)+"...";
        nativeAdName.setText(adNameStr);
        nativeAdBody.setText(nativeAd.getAdBodyText());
        nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
        nativeAdCallToAction.setVisibility(nativeAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
        nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
        sponsoredLabel.setText(nativeAd.getSponsoredTranslation());


        // Create a list of clickable views
        List<View> clickableViews = new ArrayList<>();
        clickableViews.add(nativeAdName);
        clickableViews.add(nativeAdCallToAction);

        // Register the Title and CTA button to listen for clicks.
        nativeAd.registerViewForInteraction(
                adView,
                nativeAdMedia,
                nativeAdIcon,
                clickableViews);
    }

    private static void showNativeSmall(NativeBannerAd nativeBannerAd, ViewGroup container, final AdDisplayListener listener){
        // Unregister last ad
        nativeBannerAd.unregisterView();

        if(listener!=null){
            nativeBannerAd.setAdListener(new NativeAdListener() {
                final WeakReference<AdDisplayListener> listenerRef = new WeakReference<>(listener);
                @Override
                public void onMediaDownloaded(Ad ad) { }
                @Override
                public void onError(Ad ad, AdError adError) { }
                @Override
                public void onAdLoaded(Ad ad) {}
                @Override
                public void onLoggingImpression(Ad ad) {
                    AdDisplayListener listener = listenerRef.get();
                    if(listener!=null){
                        listener.onAdDisplay(AdNetwork.FACEBOOK, AdType.NATIVE, ad.getPlacementId());
                    }
                }

                @Override
                public void onAdClicked(Ad ad) {
                    AdDisplayListener listener = listenerRef.get();
                    if(listener!=null){
                        listener.onAdClicked(AdNetwork.FACEBOOK, AdType.NATIVE, ad.getPlacementId());
                    }
                }
            });
        }

        // Add the Ad view into the ad container.
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        View adView = inflater.inflate(R.layout.layout_facebook_native, container, false);
        container.addView(adView);

        // Add the AdChoices icon
        ViewGroup adChoicesContainer = adView.findViewById(R.id.ad_choices_container);
        AdChoicesView adOptionsView = new AdChoicesView(container.getContext(), nativeBannerAd, true);
        adChoicesContainer.removeAllViews();
        adChoicesContainer.addView(adOptionsView, 0);

        // Create native UI using the ad metadata.
        TextView nativeAdTitle = adView.findViewById(R.id.fb_ad_name);
        TextView nativeAdSocialContext = adView.findViewById(R.id.fb_ad_social_context);
        TextView sponsoredLabel = adView.findViewById(R.id.fb_ad_sponsored_label);
        AdIconView nativeAdIconView = adView.findViewById(R.id.fb_ad_icon);
        Button nativeAdCallToAction = adView.findViewById(R.id.fb_ad_call_to_action);

        MediaView nativeAdMedia = adView.findViewById(R.id.fb_ad_media);
        TextView nativeAdBody = adView.findViewById(R.id.fb_ad_body);
        nativeAdMedia.setVisibility(View.GONE);
        nativeAdBody.setVisibility(View.GONE);

        // Set the Text.
        nativeAdCallToAction.setText(nativeBannerAd.getAdCallToAction());
        nativeAdCallToAction.setVisibility(
                nativeBannerAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
        // Set the Text.
        String adNameStr = nativeBannerAd.getAdvertiserName();
        if(adNameStr.length()>25) adNameStr = adNameStr.substring(0,25)+"...";
        nativeAdTitle.setText(adNameStr);
        nativeAdSocialContext.setText(nativeBannerAd.getAdSocialContext());
        sponsoredLabel.setText(nativeBannerAd.getSponsoredTranslation());

        // Register the Title and CTA button to listen for clicks.
        List<View> clickableViews = new ArrayList<>();
        clickableViews.add(nativeAdTitle);
        clickableViews.add(nativeAdCallToAction);
        nativeBannerAd.registerViewForInteraction(adView, nativeAdIconView, clickableViews);
    }
}
