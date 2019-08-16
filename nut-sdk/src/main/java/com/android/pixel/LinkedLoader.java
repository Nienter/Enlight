package com.android.pixel;

import android.content.Context;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

class LinkedLoader extends AdLoadListener implements GenericAd.LoaderDelegateI {
    private WeakReference<Context> mContextRef;
    private Iterator<AdPlcConfig.NetworkConfig> mNetwork;
    private AdSize mSize;

    private AdLoadListener mNextListener=null;

    private AdNetwork mLoadingNetwork=null;
    private AdType mLoadingType=null;
    private Object mLoadingAd = null;

    @Override
    public void load(Context context, List<AdPlcConfig.NetworkConfig> networkList, AdSize size, AdLoadListener listener){
        mContextRef = new WeakReference<>(context);

        Collections.sort(networkList, new Comparator<AdPlcConfig.NetworkConfig>() {
            @Override
            public int compare(AdPlcConfig.NetworkConfig o1, AdPlcConfig.NetworkConfig o2) {
                return o2.priority - o1.priority;
            }
        });
        mNetwork = networkList.iterator();

        mSize = size;

        mNextListener = listener;

        cancelLoading();

        loadNextNetwork();
    }

    @Override
    public void cancelLoading(){
        if(mLoadingAd != null){
            if (AdNetwork.ADMOB.equals(mLoadingNetwork)) {
                AdmobAdapter.destroyAd(mLoadingAd);
            } else if (AdNetwork.FACEBOOK.equals(mLoadingNetwork)) {
                FanAdapter.destroyAd(mLoadingAd);
            } else if (AdNetwork.MOPUB.equals(mLoadingNetwork)) {
                MopubAdapter.destroyAd(mLoadingAd);
            }
        }

        mLoadingNetwork=null;
        mLoadingType=null;
        mLoadingAd = null;
    }


    private void loadNextNetwork(){
        boolean continueLoad = true;
        String errMsg="";

        if(mContextRef.get()==null){
            continueLoad = false;
            errMsg = "context is empty";

        } else if(!mNetwork.hasNext()){
            continueLoad = false;
            errMsg = "all networks failed";
        }

        if(!continueLoad){
            if(mNextListener!=null){
                mNextListener.onAdError(errMsg);
            }
            return;
        }

        AdPlcConfig.NetworkConfig config = mNetwork.next();
        AdNetwork network = config.network;
        AdType type = config.type;
        String adUnitId = config.getAdUnitId();

        Object ad=null;
        if (AdNetwork.ADMOB.equals(network)) {
            ad = AdmobAdapter.loadAd(mContextRef.get(), type, mSize, adUnitId, this);
        } else if(AdNetwork.FACEBOOK.equals(network)) {
            ad = FanAdapter.loadAd(mContextRef.get(), type, mSize, adUnitId, this);
        } else if(AdNetwork.MOPUB.equals(network)) {
            ad = MopubAdapter.loadAd(mContextRef.get(), type, mSize, adUnitId, this);
        }

        if(ad!=null){
            mLoadingNetwork = network;
            mLoadingType    = type;
            mLoadingAd      = ad;
        } else {
            //if the network return immediately, the network may be not ready,
            // for example: the initialization of mopub sdk is not finished.
            loadNextNetwork();
        }
    }




    @Override
    public void onAdLoaded(AdNetwork network, AdType type, Object ad, Object...params){
        mLoadingNetwork= null;
        mLoadingType   = null;
        mLoadingAd     = null;

        if(mNextListener!=null){
            mNextListener.onAdLoaded(network, type, ad, params);
        }
    }

    public void onAdError(String err){
        cancelLoading();

        loadNextNetwork();
    }
}
