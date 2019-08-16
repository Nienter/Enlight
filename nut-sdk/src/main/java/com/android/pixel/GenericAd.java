package com.android.pixel;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class GenericAd {
    public static class Builder{
        public Builder(){
        }

        private AdPlcConfig mRemoteConfig=null;

        private AdPlcConfig mDefaultConfig=null;
        public Builder defaultPlcConfig(AdPlcConfig config){
            mDefaultConfig = config;
            return this;
        }

        private String mPlcId=null;
        public Builder placementId(@NonNull String plc){
            mPlcId = plc;
            return this;
        }

//        private List<AdNetwork> mNetwork = null;
//        public Builder network(AdNetwork...networks){
//            mNetwork = Arrays.asList(networks);
//            return this;
//        }
//
//        private List<AdType> mSupportedType=null;
//        public Builder supportedType(AdType ... types){
//           mSupportedType = Arrays.asList(types);
//           return this;
//        }
//
//        private AdSize mSize=null;
//        public Builder adSize(AdSize size){
//            mSize = size;
//            return this;
//        }

        public GenericAd build(){
            GenericAd ad = null;
            do{
                if(TextUtils.isEmpty(mPlcId))
                    break;


                String key = mPlcId+"_enable";
                boolean isOpen = ServerConfigManager.getInst().getConfigBoolean(key, true);
                if(!isOpen){
                    break;
                }

                mRemoteConfig = getRemoteConfig();

                AdPlcConfig plcConfig = combineAllConfig();
                if(plcConfig==null)
                    break;

                ad = new GenericAd(plcConfig);
            } while (false);

            return ad;
        }

        /**
         * combine all config in following order: remote config, user customized in code, default  config
         * @return
         */
        private AdPlcConfig combineAllConfig(){
            List<AdType> supportedType = combineType();
            if(supportedType==null || supportedType.isEmpty()){
                return null;
            }

            AdSize size = combineSize();
            if (supportedType.contains(AdType.BANNER) || supportedType.contains(AdType.NATIVE)) {
                if (supportedType.contains(AdType.INTERSTITIAL)){
                    return null;
                } else if(size==null || size==AdSize.FULLSCREEN)
                    return null;
            } else if(supportedType.contains(AdType.INTERSTITIAL)){
                size = AdSize.FULLSCREEN;
            }

            AdPlcConfig.Builder builder = new AdPlcConfig.Builder();
            builder.types(supportedType.toArray(new AdType[0]));
            builder.size(size);


            List<AdNetwork> network = combineNetwork();

            List<AdPlcConfig.NetworkConfig> networkConfig = combineNetworkConfig();
            if(networkConfig==null){
                return null;
            }
            boolean isEmptyConfig = true;
            for (AdPlcConfig.NetworkConfig config: networkConfig) {
                boolean skip = false;
                if( ! supportedType.contains(config.type)){
                    skip = true;
                }
                if (network!=null && !network.isEmpty()){
                    if ( ! network.contains(config.network)){
                        skip = true;
                    }
                }
                if (!size.equals(config.size)){
                    if (AdType.BANNER.equals(config.type) || AdType.NATIVE.equals(config.type)){
                        skip = true;
                    }
                }
                if(!skip){
                    builder.addNetwork(config.network, config.type, config.size, config.priority, config.ids);
                    isEmptyConfig = false;
                }
            }
            if(isEmptyConfig)
                return null;

            return builder.build();
        }

        private List<AdType> combineType(){
            List<AdType> ret = null;

            if(mRemoteConfig!=null){
                ret = mRemoteConfig.getSupportType();
            }
            if(ret!=null && !ret.isEmpty()){
                return ret;
            }

            if(mDefaultConfig != null){
                ret = mDefaultConfig.getSupportType();
            }
            if(ret!=null && !ret.isEmpty()){
                return ret;
            }

            return ret;
        }

        private AdSize combineSize(){
            AdSize size = null;

            if(mRemoteConfig!=null){
                size = mRemoteConfig.getSize();
            }
            if(size!=null){
                return size;
            }

            if(mDefaultConfig!=null){
                size = mDefaultConfig.getSize();
            }
            if(size!=null){
                return size;
            }

            return size;
        }

        private List<AdNetwork> combineNetwork(){
            List<AdNetwork> ret = null;

            if(mRemoteConfig!=null){
                ret = mRemoteConfig.getNetwork();
            }
            if(ret!=null && !ret.isEmpty()){
                return ret;
            }

            if(mDefaultConfig!=null){
                ret = mDefaultConfig.getNetwork();
            }
            if(ret!=null && !ret.isEmpty()){
                return ret;
            }

            return ret;
        }

        private List<AdPlcConfig.NetworkConfig> combineNetworkConfig(){
            List<AdPlcConfig.NetworkConfig> ret = null;

            if(mRemoteConfig!=null){
                ret = mRemoteConfig.getNetworkConfig();
            }
            if(ret!=null && !ret.isEmpty()){
                return ret;
            }

            if(mDefaultConfig!=null){
                ret = mDefaultConfig.getNetworkConfig();
            }
            if(ret!=null && !ret.isEmpty()){
                return ret;
            }

            ret = DeltaAdSDK.getFallbackNetworkConfig();
            return ret;
        }


        private AdPlcConfig getRemoteConfig(){
            if(TextUtils.isEmpty(mPlcId)){
                return null;
            }

            AdPlcConfig.Builder builder = new AdPlcConfig.Builder();

            List<AdNetwork> networkList = new ArrayList<>();
            try {//network
                String key = mPlcId+"_"+FunctionUtil.decrypt(AdNetwork.TAG);
                String configRaw = ServerConfigManager.getInst().getConfigString(key);
                JSONArray networkJson = new JSONArray(configRaw);
                for (int i = 0; i < networkJson.length(); i++) {
                    AdNetwork network = AdNetwork.getNetwork(networkJson.optString(i,null));
                    if(network!=null){
                        networkList.add(network);
                    }
                }
                if(!networkList.isEmpty()){
                    builder.network(networkList.toArray(new AdNetwork[0]));
                }
            } catch (Throwable err){}


            List<AdType> typeList = new ArrayList<>();
            try {//type
                String key = mPlcId+"_"+FunctionUtil.decrypt(AdType.TAG);
                String configRaw = ServerConfigManager.getInst().getConfigString(key);
                JSONArray typeJson = new JSONArray(configRaw);
                for (int i = 0; i < typeJson.length(); i++) {
                    AdType type = AdType.getType(typeJson.optString(i, null));
                    if(type!=null){
                        typeList.add(type);
                    }
                }
                if(!typeList.isEmpty()){
                    builder.types(typeList.toArray(new AdType[0]));
                }

            }catch (Throwable err){}

            AdSize size = null;
            try {//size
                String key = mPlcId+"_"+FunctionUtil.decrypt(AdSize.TAG);
                String configRaw = ServerConfigManager.getInst().getConfigString(key);
                size = AdSize.getSize(configRaw);
                if(size!=null){
                    builder.size(size);
                }
            } catch (Throwable err){}



            try{
                String key = mPlcId+"_"+"config";
                String configRaw = ServerConfigManager.getInst().getConfigString(key);
                JSONArray plcConfigJson = new JSONArray(configRaw);
                for (int i = 0; i < plcConfigJson.length(); i++) {
                    JSONObject configJson = plcConfigJson.getJSONObject(i);

                    AdNetwork network = AdNetwork.getNetwork(configJson.optString(FunctionUtil.decrypt(AdNetwork.TAG), null));
                    if(network==null)
                        continue;

                    AdType adType = AdType.getType(configJson.optString(FunctionUtil.decrypt(AdType.TAG), null));
                    if(!typeList.isEmpty() && !typeList.contains(adType)){
                        continue;
                    }

                    AdSize adSize = AdSize.getSize(configJson.optString(FunctionUtil.decrypt(AdSize.TAG), null));
                    if(size!=null && adSize!=null && size!=adSize){
                        continue;
                    }

                    int priority = configJson.optInt("priority", 0);

                    List<String> idsList = new ArrayList<>();
                    JSONArray idsJson = configJson.optJSONArray("ids");
                    if(idsJson!=null && idsJson.length()>0){
                        for (int j = 0; j < idsJson.length(); j++) {
                            String id = idsJson.optString(j, null);
                            if(!TextUtils.isEmpty(id)){
                                idsList.add(id);
                            }
                        }
                    }
                    if(idsList.isEmpty()){
                        continue;
                    }


                    if(adSize==null){
                        if(AdNetwork.ADMOB.equals(network)){
                            if(AdType.BANNER.equals(adType) || AdType.NATIVE.equals(adType)){
                                builder.addNetwork(network, adType, AdSize.SMALL, priority, idsList.toArray(new String[idsList.size()]));
                                builder.addNetwork(network, adType, AdSize.MEDIUM, priority, idsList.toArray(new String[idsList.size()]));
                            }
                        } else if(AdNetwork.MOPUB.equals(network)){
                            if(AdType.NATIVE.equals(adType)){
                                builder.addNetwork(network, adType, AdSize.SMALL, priority, idsList.toArray(new String[idsList.size()]));
                                builder.addNetwork(network, adType, AdSize.MEDIUM, priority, idsList.toArray(new String[idsList.size()]));
                            }
                        }

                        if(AdType.INTERSTITIAL.equals(adType) || AdType.REWARDEDVIDEO.equals(adType)){
                            builder.addNetwork(network, adType, AdSize.FULLSCREEN, priority, idsList.toArray(new String[idsList.size()]));
                        }
                    } else {
                        builder.addNetwork(network, adType, adSize, priority, idsList.toArray(new String[0]));
                    }
                }

            } catch (Throwable e){
                //if(AppUtil.sDebug) e.printStackTrace();
            }

            return builder.build();
        }
    }


















    private GenericAd(){}
    private AdPlcConfig mPlcConfig=null;
    private AdNetwork mNetwork;
    private AdType mType;
    private AdSize mSize;
    private Object mAd =null;
    private LoaderDelegateI mLoader = null;
    private GenericAd(AdPlcConfig plcConfig){
        mPlcConfig = plcConfig;
        mSize = mPlcConfig.getSize();
    }

    /**
     * This function can be called multiple times:
     *      Create GenericAd obj --> load --> show(optional) --> destroy(optional) -->load -->....-->load...-->destroy
     *
     * @param page for interstitial, it is a Activity instance
     * @param listener
     */
    public void load(@NonNull Context page, @Nullable final AdLoadListener listener){
        destroy();

        List<AdPlcConfig.NetworkConfig> networkList = mPlcConfig.getNetworkConfig();
        if(networkList==null || networkList.isEmpty())
            return;

        if(mLoader==null) {
            mLoader = new LinkedLoader();
        }
        mLoader.load(page, networkList, mSize, new AdLoadListener() {
            WeakReference<AdLoadListener> listenerRef = new WeakReference<>(listener);
            @Override
            public void onAdLoaded(AdNetwork network, AdType type, Object ad, Object...params) {
                mNetwork = network;
                mType = type;
                mAd = ad;

                AdLoadListener listener = listenerRef.get();
                if(listener!=null){
                    listener.onAdLoaded(network, type, ad, params);
                }
            }

            @Override
            public void onAdError(String err) {
                AdLoadListener listener = listenerRef.get();
                if(listener!=null){
                    listener.onAdError(err);
                }
            }
        });
    }

    public boolean isReady(){
        if(mAd==null){
            return false;
        }


        if (AdNetwork.ADMOB.equals(mNetwork)) {
            return AdmobAdapter.isReady(mAd);
        } else if (AdNetwork.FACEBOOK.equals(mNetwork)) {
            return FanAdapter.isReady(mAd);
        } else if (AdNetwork.MOPUB.equals(mNetwork)) {
            return MopubAdapter.isReady(mAd);
        }

        return false;
    }

    /**
     *
     * @param container for interstitial, the container is ignored.
     * @param listener
     */
    public void show(ViewGroup container, @Nullable AdDisplayListener listener){
        if(mAd==null){
            return;
        }

        if (AdNetwork.ADMOB.equals(mNetwork)) {
            AdmobAdapter.showAd(mAd, container, mSize, listener);
        } else if (AdNetwork.FACEBOOK.equals(mNetwork)) {
            FanAdapter.showAd(mAd, container, mSize, listener);
        } else if (AdNetwork.MOPUB.equals(mNetwork)) {
            MopubAdapter.showAd(mAd, container, mSize, listener);
        }

    }

    public void destroy(){
        if(mLoader!=null){
            mLoader.cancelLoading();
        }

        if(mAd==null){
            return;
        }

        if (AdNetwork.ADMOB.equals(mNetwork)) {
            AdmobAdapter.destroyAd(mAd);
        } else if (AdNetwork.FACEBOOK.equals(mNetwork)) {
            FanAdapter.destroyAd(mAd);
        } else if(AdNetwork.MOPUB.equals(mNetwork)) {
            MopubAdapter.destroyAd(mAd);
        }


        mAd = null;
    }



    interface LoaderDelegateI{
        void load(Context context, List<AdPlcConfig.NetworkConfig> networkList, AdSize size, AdLoadListener listener);
        void cancelLoading();
    }
}
