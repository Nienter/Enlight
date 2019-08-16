package com.android.pixel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class AdPlcConfig {
    public static final class Builder{

        private List<AdNetwork> mNetwork = null;
        public Builder network(AdNetwork...networks){
            mNetwork = Arrays.asList(networks);
            return this;
        }

        private List<AdType> mSupportedType=null;
        public Builder types(AdType...types){
            mSupportedType = Arrays.asList(types);
            return this;
        }

        private AdSize mSize=null;
        public Builder size(AdSize size){
            mSize = size;
            return this;
        }

        private List<NetworkConfig> mNetworkConfig =new ArrayList<>();
        public Builder addNetwork(AdNetwork network, AdType type, AdSize size, int priority, String[] ids){
            NetworkConfig config = new NetworkConfig();
            config.network = network;
            config.priority = priority;
            config.type = type;
            config.ids = ids;
            config.size = size;

            mNetworkConfig.add(config);
            return this;
        }

        public AdPlcConfig build(){
            return new AdPlcConfig(this);
        }
    }


    private Builder mData;
    private AdPlcConfig(Builder builder){
        mData = builder;
    }

    public List<AdNetwork> getNetwork(){
        return mData.mNetwork;
    }

    public List<AdType> getSupportType(){
        return mData.mSupportedType;
    }

    public AdSize getSize(){
        return mData.mSize;
    }


    public List<NetworkConfig> getNetworkConfig(){
        return mData.mNetworkConfig;
    }


    static class NetworkConfig{
        AdNetwork network;
        int priority;
        AdType type;
        String[] ids;
        AdSize size;

        String getAdUnitId(){
            if(ids==null || ids.length==0){
                return null;
            }

            int selector = new Random(System.currentTimeMillis()).nextInt(ids.length);
            return ids[selector];
        }
    }
}
