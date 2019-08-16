package com.android.pixel;

import android.util.Log;

import com.EncryptStr;

public enum AdType {
    @EncryptStr(value = "banner")
    BANNER("TqUjD+MNLZCpuoo7Ru73Bg"),

    @EncryptStr(value = "interstitial")
    INTERSTITIAL("sTB0Dx9X6nn3nwDJK99lbg"),

    @EncryptStr(value = "native")
    NATIVE("JLr5ewS0qc32Lu4VYihLMg"),

    @EncryptStr(value = "rewarded")
    REWARDEDVIDEO("QBg6EKIILsOphvZHuxv7wQ");

    @EncryptStr(value = "type")
    public final static String TAG = "23t4CLVaqBhECZlOaWyz4g" ;

    static {
        if(AppUtil.sDebug){
            Log.d("copyTheOutputIntoCode", "AdType.TAG:-->"+FunctionUtil.encrypt("type")+"<--");
            Log.d("verify", TAG+"-->"+FunctionUtil.decrypt(TAG)+"<--");

            Log.d("copyTheOutputIntoCode", "AdType.BANNER:-->"+FunctionUtil.encrypt("banner")+"<--");
            Log.d("verify", BANNER.toString()+"-->"+FunctionUtil.decrypt(BANNER.toString())+"<--");

            Log.d("copyTheOutputIntoCode", "AdType.INTERSTITIAL:-->"+FunctionUtil.encrypt("interstitial")+"<--");
            Log.d("verify", INTERSTITIAL.toString()+"-->"+FunctionUtil.decrypt(INTERSTITIAL.toString())+"<--");

            Log.d("copyTheOutputIntoCode", "AdType.NATIVE:-->"+FunctionUtil.encrypt("native")+"<--");
            Log.d("verify", NATIVE.toString()+"-->"+FunctionUtil.decrypt(NATIVE.toString())+"<--");

            Log.d("copyTheOutputIntoCode", "AdType.REWARDEDVIDEO:-->"+FunctionUtil.encrypt("rewarded")+"<--");
            Log.d("verify", REWARDEDVIDEO.toString()+"-->"+FunctionUtil.decrypt(REWARDEDVIDEO.toString())+"<--");
        }
    }

    private final String msg;
    AdType(String message) {
        this.msg = message;
    }

    @Override
    public String toString() {
        return this.msg;
    }

    public static AdType getType(String msg){
        AdType ret = null;

        if(FunctionUtil.decrypt(BANNER.toString()).equalsIgnoreCase(msg)){
            ret = BANNER;
        } else if(FunctionUtil.decrypt(INTERSTITIAL.toString()).equalsIgnoreCase(msg)){
            ret = INTERSTITIAL;
        } else if (FunctionUtil.decrypt(NATIVE.toString()).equalsIgnoreCase(msg)) {
            ret = NATIVE;
        } else if (FunctionUtil.decrypt(REWARDEDVIDEO.toString()).equalsIgnoreCase(msg)) {
            ret = REWARDEDVIDEO;
        }

        return ret;
    }
}
