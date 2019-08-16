package com.android.pixel;

import android.util.Log;

import com.EncryptStr;

import java.util.ArrayList;
import java.util.List;

public class BergInner {
    @EncryptStr(value = "Inner")
    private static final String sInnerPlc= "6Q+3prt/9XvKJ1QiWXUgHA" ;
    static {
        if(AppUtil.sDebug){
            Log.d("copyTheOutputIntoCode", "BergInner.sInnerPlc:-->"+FunctionUtil.encrypt("Inner")+"<--");
            Log.d("verify", sInnerPlc+"-->"+FunctionUtil.decrypt(sInnerPlc)+"<--");
        }
    }
    public static String getDecryptInnerPlc(){
        return FunctionUtil.decrypt(sInnerPlc);
    }
    public static String getInnerPlc(){
        return sInnerPlc;
    }


    private static volatile BergInner sInst = null;
    public static BergInner getInst(){
        if(sInst==null){
            synchronized (BergInner.class){
                if(sInst==null){
                    sInst = new BergInner();
                }
            }
        }

        return sInst;
    }

    private boolean mIsCenter = false;
    private BergInnerView mView = null;

    private String mPlc;
    private GenericAd     mAd=null;

    public boolean isShown(){
        return mView!=null && !mView.isDestroyed() && mAd!=null;
    }

    public void  loadAndShow(final int unlockStep){
        if(mAd!=null){
            mAd.destroy();
            mAd=null;
        }
        if(mView!=null){
            mView.destroy();
            mView = null;
        }


        List<AdNetwork> networkList = getAvailableNetwork();
        if(networkList==null || networkList.isEmpty()){
            return;
        }

        mPlc = BergManager.getDecryptTag()+getDecryptInnerPlc();
        mAd = new GenericAd.Builder()
                .placementId(mPlc)
                .defaultPlcConfig(new AdPlcConfig.Builder()
                        .types(/*AdType.BANNER, */AdType.NATIVE)
                        .size(AdSize.MEDIUM)
                        .network(networkList.toArray(new AdNetwork[0]))
                        .build())
                .build();
        if(mAd==null)
            return;



        mAd.load(AppUtil.getApp(), new AdLoadListener() {
            @Override
            public void onAdLoaded(AdNetwork network, AdType type, Object ad, Object... params) {
                super.onAdLoaded(network, type, ad, params);
                if(isShown())
                    return;

                mView = new BergInnerView(AppUtil.getApp(), unlockStep, mIsCenter);
                mIsCenter = !mIsCenter;

                mAd.show(mView.getAdContainer(network), new AdDisplayListener() {
                    @Override
                    public void onAdDisplay(AdNetwork network, AdType type, String adId, Object... params) {
                        super.onAdDisplay(network, type, adId, params);
                        BergStats.getInst().updateTodayImpressionTimes(
                                BergStats.getInst().getTodayImpressionTimes()+1);

                        BergStats.getInst().updateTodayImpressionTimesNetwork(network,
                                BergStats.getInst().getTodayImpressionTimesNetwork(network)+1);

                        AdEvt.recordDisplayEvt(mPlc, network, type);
                        AdEvt.recordBergDisplayEvt(mPlc, network, type);

                        long imp = BergStats.getInst().getTodayImpressionTimesNetwork(network);
                        long limit = BergConfig.getInst().getImpDailyLimitNetwork(network);
                        if(imp >= limit){
                            AdEvt.recordBergFullEvt(mPlc, network);
                        }
                    }

                    @Override
                    public void onAdClicked(AdNetwork network, AdType type, String adId, Object... params) {
                        super.onAdClicked(network, type, adId, params);

                        if(mView!=null){//if the user clicked ad, show the closeable btn immediately
                            mView.showCloseBtn();
                        }

                        BergStats.getInst().updateTotalInnerClickTimesNetwork(network,
                                BergStats.getInst().getTotalInnerClickTimesNetwork(network)+1);

                        AdEvt.recordClickEvt(mPlc, network, type);
                        AdEvt.recordBergClickEvt(mPlc, network, type);
                    }

                    @Override
                    public void onAdClosed(AdNetwork network, AdType type, String adId, Object... params) {
                        super.onAdClosed(network, type, adId, params);
                        if(mView!=null){
                            mView.destroy();
                            mView=null;
                        }
                        if(mAd!=null){
                            mAd.destroy();
                            mAd=null;
                        }
                    }
                });

            }

            public void onAdError(String err){
                if(mView!=null){
                    mView.destroy();
                    mView=null;
                }
                if(mAd!=null){
                    mAd.destroy();
                    mAd=null;
                }
            }
        });

    }

    private List<AdNetwork> getAvailableNetwork(){
        List<AdNetwork> networkList = new ArrayList<>();

        for (AdNetwork network:BergManager.sDefaultNetwork ) {
            long imp = BergStats.getInst().getTodayImpressionTimesNetwork(network);
            long limit = BergConfig.getInst().getImpDailyLimitNetwork(network);
            if(AppUtil.sDebug){
                Log.d("Berg", FunctionUtil.decrypt(network.toString())+", imp/limit:"+imp+"/"+limit);
            }
            if(imp < limit){
                networkList.add(network);
            }
        }

        return networkList;
    }
}
