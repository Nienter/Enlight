package com.android.pixel;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import com.EncryptStr;
import com.applovin.sdk.AppLovinSdk;
import com.flurry.android.FlurryAgent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import defpackage.PixelService;

public class DeltaAdSDK {
    public static class Builder{
        private Application app = null;
        public Builder application(Application app, boolean debug){
            this.app = app;

            AppUtil.init(app, debug);
            return this;
        }

        private String admobAppId = null;
        public Builder admobAppId(String id){
            admobAppId = id;
            return this;
        }

        private String mopubAdUnitId = null;
        public Builder mopubAdUnitId(String id){
            mopubAdUnitId = id;
            return this;
        }

        private String flurryKey=null;
        public Builder flurryApiKey(String key){
            this.flurryKey = key;
            return this;
        }

        public Builder fallbackNetwork(AdNetwork network, AdType type, AdSize size, int priority,  String ...adUnitId){
            addFallbackNetwork(network, type, size, priority, adUnitId);
            return this;
        }

        private void registerActivityCallback(){
            app.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                @Override
                public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                    try {
                        MopubAdapter.init(activity, mopubAdUnitId);

                        BergPopup.hideTitleInRecentApps(activity);
                    } catch (Throwable err){
                        if(AppUtil.sDebug)  err.printStackTrace();
                    }
                }

                @Override
                public void onActivityStarted(Activity activity) {
                    FlurryAgent.onPageView();

                }

                @Override
                public void onActivityResumed(Activity activity) {
                    try {
                        BergPopup.hideTitleInRecentApps(activity);

                        BergPopup.modifyFbBackBtnTime(activity);
                        BergPopup.maskAdmobBackBtn(activity);
                    } catch (Throwable err){
                        if(AppUtil.sDebug)  err.printStackTrace();
                    }
                }

                @Override
                public void onActivityPaused(Activity activity) {
                }

                @Override
                public void onActivityStopped(Activity activity) {

                }

                @Override
                public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                }

                @Override
                public void onActivityDestroyed(Activity activity) {
                }
            });
        }

        private static boolean sInitNotif = false;
        private void startKeepAlive(Context context){
            try {//On Android 8.0, the app is running background after reboot, and can't launch service
                app.startService(new Intent(app, PixelService.class));
            } catch (Throwable err){
                if(AppUtil.sDebug) err.printStackTrace();
            }


            if(sInitNotif)
                return;

            try {
                KeepAliveManager.start();

                //only start notification on 8.0 and later
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
                    //usage? unknown, copied from lanzong
                    (context.getSystemService(NotificationManager.class)).createNotificationChannel(
                            new NotificationChannel("notify-chan", "notify-chan", NotificationManager.IMPORTANCE_DEFAULT));
                }

                sInitNotif = true;
            } catch (Throwable err){
                if(AppUtil.sDebug) err.printStackTrace();
            }
        }

        public void init() throws Exception{
            if(app==null){
                if(AppUtil.sDebug) {
                    throw new Exception("must set app instance");
                }
            }


            AdmobAdapter.initSdk(app, admobAppId);

            FanAdapter.init(app);

            AppLovinSdk.initializeSdk(app);
            AppLovinSdk.getInstance( app ).getSettings().setTestAdsEnabled(AppUtil.sDebug);

            registerActivityCallback();

            StatsImpl.init(app, flurryKey);

            ServerConfigManager.getInst();

            startKeepAlive(app);
        }
    }


    @EncryptStr(value = "inapp_ads_enable")
    private final static String sInappAdsEnable_key = "ynDA79ZIkykHf5FMtwvnMB9cnUFdsMMMyTl4IGu7m34" ;
    static {
        if(AppUtil.sDebug){
            Log.d("copyTheOutputIntoCode", "DeltaAdSDK.sInappAdsEnable_key:-->"+FunctionUtil.encrypt("inapp_ads_enable")+"<--");
            Log.d("verify", sInappAdsEnable_key+"-->"+FunctionUtil.decrypt(sInappAdsEnable_key)+"<--");
        }
    }
    public static final boolean isInappEnabled(){
        return AppUtil.sDebug
                ? AppUtil.sDebug
                : ServerConfigManager.getInst().getConfigBoolean(FunctionUtil.decrypt(sInappAdsEnable_key), AppUtil.sDebug);
    }


    private static List<AdPlcConfig.NetworkConfig> sFallbackNetwork = new ArrayList<>();
    private static void addFallbackNetwork(AdNetwork network, AdType type, AdSize size, int priority, String ...adUnitId){
        if(adUnitId==null || adUnitId.length==0)
            return;

        AdPlcConfig.NetworkConfig config = new AdPlcConfig.NetworkConfig();
        config.network = network;
        config.type = type;
        config.size = size;
        config.ids = adUnitId;
        config.priority = priority;
        sFallbackNetwork.add(config);
    }



    public static List<AdPlcConfig.NetworkConfig> getFallbackNetworkConfig(){
        List<AdPlcConfig.NetworkConfig> remoteConfig = getFallbackNetworkConfigRemoteConfig();

        if(remoteConfig!=null && !remoteConfig.isEmpty()){
            return remoteConfig;
        }
        return sFallbackNetwork;
    }


    @EncryptStr(value = "ads_fallback")
    private final static String sAdsFallback_key = "ud9qxMguhqYwcWCMbSWhaw" ;
    static {
        if(AppUtil.sDebug){
            Log.d("copyTheOutputIntoCode", "DeltaAdSDK.sAdsFallback_key:-->"+FunctionUtil.encrypt("ads_fallback")+"<--");
            Log.d("verify", sAdsFallback_key+"-->"+FunctionUtil.decrypt(sAdsFallback_key)+"<--");
        }
    }
    private static List<AdPlcConfig.NetworkConfig> sCachedFallback = null;
    private static long sLastCacheT=0;
    private static List<AdPlcConfig.NetworkConfig> getFallbackNetworkConfigRemoteConfig(){
        if(System.currentTimeMillis() - sLastCacheT < 1* DateUtils.HOUR_IN_MILLIS){
            return sCachedFallback;
        }
        sLastCacheT = System.currentTimeMillis();


        List<AdPlcConfig.NetworkConfig> config = new ArrayList<>();

        AdNetwork [] networks = {AdNetwork.ADMOB, AdNetwork.FACEBOOK, AdNetwork.MOPUB};
        for (AdNetwork network:networks) {
            try {
                //ads_fallback_fan
                String key = FunctionUtil.decrypt(sAdsFallback_key) + "_" + FunctionUtil.decrypt(network.toString());
                String configRaw = ServerConfigManager.getInst().getConfigString(key);
                JSONArray configJson = new JSONArray(configRaw);
                for (int i = 0; i < configJson.length(); i++) {
                    JSONObject typeConfig = configJson.getJSONObject(i);

                    AdType adType = AdType.getType(typeConfig.optString(FunctionUtil.decrypt(AdType.TAG), null));


                    AdSize adSize = AdSize.getSize(typeConfig.optString(FunctionUtil.decrypt(AdSize.TAG), null));

                    int priority = typeConfig.optInt("priority", 0);

                    List<String> idsList = new ArrayList<>();
                    JSONArray idsJson = typeConfig.optJSONArray("ids");
                    if (idsJson != null && idsJson.length() > 0) {
                        for (int j = 0; j < idsJson.length(); j++) {
                            String id = idsJson.optString(j, null);
                            if (!TextUtils.isEmpty(id)) {
                                idsList.add(id);
                            }
                        }
                    }
                    if (idsList.isEmpty()) {
                        continue;
                    }


                    if (adSize == null) {
                        if (AdNetwork.ADMOB.equals(network)) {
                            if (AdType.BANNER.equals(adType) || AdType.NATIVE.equals(adType)) {
                                AdPlcConfig.NetworkConfig networkConfig = new AdPlcConfig.NetworkConfig();
                                networkConfig.network = network;
                                networkConfig.type = adType;
                                networkConfig.size = AdSize.SMALL;
                                networkConfig.priority = priority;
                                networkConfig.ids = idsList.toArray(new String[idsList.size()]);
                                config.add(networkConfig);

                                networkConfig = new AdPlcConfig.NetworkConfig();
                                networkConfig.network = network;
                                networkConfig.type = adType;
                                networkConfig.size = AdSize.MEDIUM;
                                networkConfig.priority = priority;
                                networkConfig.ids = idsList.toArray(new String[idsList.size()]);
                                config.add(networkConfig);
                            }
                        } else if (AdNetwork.MOPUB.equals(network)) {
                            if (AdType.NATIVE.equals(adType)) {
                                AdPlcConfig.NetworkConfig networkConfig = new AdPlcConfig.NetworkConfig();
                                networkConfig.network = network;
                                networkConfig.type = adType;
                                networkConfig.size = AdSize.SMALL;
                                networkConfig.priority = priority;
                                networkConfig.ids = idsList.toArray(new String[idsList.size()]);
                                config.add(networkConfig);

                                networkConfig = new AdPlcConfig.NetworkConfig();
                                networkConfig.network = network;
                                networkConfig.type = adType;
                                networkConfig.size = AdSize.MEDIUM;
                                networkConfig.priority = priority;
                                networkConfig.ids = idsList.toArray(new String[idsList.size()]);
                                config.add(networkConfig);
                            }
                        }

                        if (AdType.INTERSTITIAL.equals(adType) || AdType.REWARDEDVIDEO.equals(adType)) {
                            AdPlcConfig.NetworkConfig networkConfig = new AdPlcConfig.NetworkConfig();
                            networkConfig.network = network;
                            networkConfig.type = adType;
                            networkConfig.size = AdSize.FULLSCREEN;
                            networkConfig.priority = priority;
                            networkConfig.ids = idsList.toArray(new String[idsList.size()]);
                            config.add(networkConfig);
                        }
                    } else {
                        AdPlcConfig.NetworkConfig networkConfig = new AdPlcConfig.NetworkConfig();
                        networkConfig.network = network;
                        networkConfig.type = adType;
                        networkConfig.size = adSize;
                        networkConfig.priority = priority;
                        networkConfig.ids = idsList.toArray(new String[idsList.size()]);
                        config.add(networkConfig);
                    }
                }
            } catch(Throwable e){
                    //if (AppUtil.sDebug) e.printStackTrace();
            }
        }

        sCachedFallback = config;
        return sCachedFallback;
    }


    @EncryptStr(value = "admobInt_cache")
    private final static String sAdMobIntCache_key = "JGHiEL8wLrMNWTE5/zYrmA" ;
    static {
        if(AppUtil.sDebug){
            Log.d("copyTheOutputIntoCode", "DeltaAdSDK.sAdMobIntCache_key:-->"+FunctionUtil.encrypt("admobInt_cache")+"<--");
            Log.d("verify", sAdMobIntCache_key+"-->"+FunctionUtil.decrypt(sAdMobIntCache_key)+"<--");
        }
    }
    public static boolean isAdMobInterstitialCacheEnabled(){
        return AppUtil.sDebug
                || ServerConfigManager.getInst()
                    .getConfigBoolean(FunctionUtil.decrypt(sAdMobIntCache_key), true);
    }
}
