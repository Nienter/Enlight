package com.android.pixel;

import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;


import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AdMobInterstitialCache {
    private static volatile AdMobInterstitialCache sInst = null;
    public static AdMobInterstitialCache getInst(){
        if(sInst==null){
            synchronized (AdMobInterstitialCache.class){
                if(sInst==null){
                    sInst = new AdMobInterstitialCache();
                }
            }
        }
        return sInst;
    }

    private Map<String, InterstitialAd> mLoaded = new ConcurrentHashMap<>();
    private Queue<String> mLoading = new ConcurrentLinkedQueue<>();
    private Handler mUIHandler = new Handler(Looper.getMainLooper());
    private AdMobInterstitialCache(){

    }


    public InterstitialAd loadAd(String adUnitId, final AdLoadListener listener){
        InterstitialAd cached = null;
        if(mLoaded.containsKey(adUnitId)) {
            cached = mLoaded.remove(adUnitId);
        }

        while (cached==null || !cached.isLoaded()){
            cached = null;

            if(mLoaded.isEmpty())
                break;

            cached = mLoaded.remove(mLoaded.keySet().iterator().next());
        }


        if(cached != null){
            if(! cached.getAdUnitId().equals(adUnitId)){
                cacheAd(adUnitId);
            }

            if(listener!=null)
                listener.onAdLoaded(AdNetwork.ADMOB, AdType.INTERSTITIAL, cached);

            return cached;
        } else {
            cacheAd(adUnitId);
            return null;
        }
    }

    public void cacheAd(String adUnitId){
        if(mLoading.contains(adUnitId) || mLoaded.containsKey(adUnitId))
            return;

        mLoading.add(adUnitId);


        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                final InterstitialAd ad = new InterstitialAd(AppUtil.getApp());
                ad.setAdUnitId(adUnitId);

                ad.setAdListener(new AdListener() {
                    @Override public void onAdLoaded() {
                        super.onAdLoaded();

                        mLoaded.put(adUnitId, ad);
                        mLoading.remove(adUnitId);
                    }

                    @Override public void onAdFailedToLoad(int i) {
                        super.onAdFailedToLoad(i);
                        mLoading.remove(adUnitId);
                    }
                });

                ad.loadAd(new AdRequest.Builder().build());
            }
        });
    }
}
