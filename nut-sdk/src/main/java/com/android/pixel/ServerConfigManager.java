package com.android.pixel;

import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import com.flurry.android.FlurryConfig;
import com.flurry.android.FlurryConfigListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ServerConfigManager {
    private volatile static ServerConfigManager sInst=null;

    public static ServerConfigManager getInst(){
        if(sInst==null){
            synchronized (ServerConfigManager.class){
                if(sInst==null){
                    sInst = new ServerConfigManager();
                }
            }
        }

        return sInst;
    }

    private FlurryConfig mFlurryConfig = null;
    private Map<String, Object> mCachedConfigs = new HashMap<>();
    private ServerConfigManager(){
        if(AppUtil.sDebug){
            Log.d("ServerConfigManager", "ServerConfigManager:init");
        }

        mFlurryConfig = FlurryConfig.getInstance();
        mFlurryConfig.registerListener(new FlurryConfigListener() {
            @Override
            public void onFetchSuccess() {
                if(AppUtil.sDebug){
                    Log.d("ServerConfigManager","onFetchSuccess");
                }
                mFlurryConfig.activateConfig();
            }

            @Override
            public void onFetchNoChange() {
                if(AppUtil.sDebug){
                    Log.d("ServerConfigManager","onFetchNoChange");
                }
            }

            @Override
            public void onFetchError(boolean isRetrying) {
                if(AppUtil.sDebug){
                    Log.d("ServerConfigManager","onFetchError");
                }
            }

            @Override
            public void onActivateComplete(boolean b) {
                if(AppUtil.sDebug){
                    Log.d("ServerConfigManager","onActivateComplete");
                }
                mCachedConfigs.clear();
                mCachedConfigs.put(String.valueOf(new Random(System.currentTimeMillis()).nextInt(1000)),
                        new Random(System.currentTimeMillis()).nextInt(1000));
            }
        });

        pullServerConfig();
    }

    private final long mCacheExp = 3*DateUtils.HOUR_IN_MILLIS;
    private long mLastPollT=0;
    private void pullServerConfig(){
        long now   = System.currentTimeMillis();
        long elapse= now -mLastPollT;
        if(elapse<0) elapse=0;

        boolean pullData = false;
        if(mCachedConfigs.isEmpty()){
            if(elapse>=40*DateUtils.SECOND_IN_MILLIS){
                pullData = true;
            }

        } else if(elapse>mCacheExp){
            pullData = true;
        }

        if(pullData) {
            if(AppUtil.sDebug){
                Log.d("ServerConfigManager", "ServerConfigManager:pullServerConfig");
            }

            mLastPollT = now;
            mFlurryConfig.fetchConfig();

        } else {
            if(AppUtil.sDebug){
                Log.d("ServerConfigManager","the interval is too short");
            }
        }
    }

    public String getConfigString(String key){
        pullServerConfig();

        if(TextUtils.isEmpty(key)){
            return null;
        }

        String config=null;
        if(mCachedConfigs.containsKey(key)){
            Object val = mCachedConfigs.get(key);
            if(val instanceof String){
                config = (String) val;
            }
        }else {
            String val =mFlurryConfig.getString(key, null);
            if(val!=null){
                mCachedConfigs.put(key, val);
                config=val;
            }
        }

        return config;
    }

    public Boolean getConfigBoolean(String key, boolean defVal){
        if(TextUtils.isEmpty(key)){
            return null;
        }

        Boolean config=null;
        if(mCachedConfigs.containsKey(key)){
            Object val = mCachedConfigs.get(key);
            if(val instanceof Boolean){
                config = (Boolean) val;
            } else {
                config = defVal;
            }
        }else {
            boolean val =mFlurryConfig.getBoolean(key, defVal);
            if(val != defVal) {
                mCachedConfigs.put(key, val);
            }
            config=val;
        }

        return config;
    }

    public Long getConfigLong(String key, Long defVal){
        if(TextUtils.isEmpty(key)){
            return null;
        }

        Long config = null;
        if (mCachedConfigs.containsKey(key)) {
            Object val = mCachedConfigs.get(key);
            if(val instanceof Long){
                config = (Long) val;
            }
        } else {
            Long val = mFlurryConfig.getLong(key, defVal);
            if(val!=defVal) {
                mCachedConfigs.put(key, val);
            }
            config=val;
        }

        return config;
    }
}
