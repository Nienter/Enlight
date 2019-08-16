package com.en.enlight.ads;

import android.content.Context;
import android.view.ViewGroup;

import com.android.pixel.AdDisplayListener;
import com.android.pixel.AdEvt;
import com.android.pixel.AdLoadListener;
import com.android.pixel.AdNetwork;
import com.android.pixel.AdPlcConfig;
import com.android.pixel.AdSize;
import com.android.pixel.AdType;
import com.android.pixel.DeltaAdSDK;
import com.android.pixel.GenericAd;

import java.lang.ref.WeakReference;
import java.util.Random;

public class InnerAds {
    private WeakReference<GenericAd> mAdRef = null;
    private WeakReference<Context> mContextRef=null;
    private String mPlc="";

    public InnerAds(final ViewGroup adContainer, String plc, AdSize size){
        this(adContainer, plc,
                //for inline ad in app, FB(8) VS Admob(2)
                new Random(System.currentTimeMillis()).nextInt(100)<82
                        ? (AdSize.SMALL.equals(size) ? DefaultPlcConfig.sBannerNativeSmall : DefaultPlcConfig.sBannerNativeMedium)
                        : (AdSize.SMALL.equals(size) ? DefaultPlcConfig.sBannerNativeSmallWithoutFan : DefaultPlcConfig.sBannerNativeMediumWithoutFan));
    }

    /**
     * @deprecated Use {@link #InnerAds(ViewGroup, String, AdSize)} instead.
     */
    @Deprecated
    private InnerAds(final ViewGroup adContainer, String plc, AdPlcConfig config){
        Context context = adContainer.getContext();
        mContextRef = new WeakReference<>(context);

        mPlc = plc;

        if(! DeltaAdSDK.isInappEnabled()){
            return;
        }

        mAdRef = new WeakReference<>(new GenericAd.Builder()
                        .placementId(plc)
                        .defaultPlcConfig(config)
                        .build());


        if(mAdRef.get()!=null){
            mAdRef.get().load(context, new AdLoadListener() {
                @Override
                public void onAdLoaded(AdNetwork network, AdType type, Object ad, Object... params) {
                    super.onAdLoaded(network, type, ad, params);
                    if(mAdRef!=null && mAdRef.get()!=null
                            && mContextRef!=null && mContextRef.get()!=null){
                        mAdRef.get().show(adContainer, new AdDisplayListener() {
                            @Override
                            public void onAdDisplay(AdNetwork network, AdType type, String adId, Object... params) {
                                AdEvt.recordDisplayEvt(mPlc, network, type);
                            }

                            @Override
                            public void onAdClicked(AdNetwork network, AdType type, String adId, Object... params) {
                                AdEvt.recordClickEvt(mPlc, network, type);
                            }
                        });

                        if(AdNetwork.ADMOB.equals(network) && AdType.BANNER.equals(type)){
                            AdEvt.recordDisplayEvt(mPlc, network, type);
                        }
                    }
                }
            });
        }
    }

    public void destroy(){
        if(mAdRef!=null && mAdRef.get()!=null){
            mAdRef.get().destroy();
            mAdRef=null;
        }
    }
}
