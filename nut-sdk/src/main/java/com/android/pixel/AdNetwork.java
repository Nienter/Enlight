package com.android.pixel;


import android.util.Log;

import com.EncryptStr;

public enum AdNetwork {
    @EncryptStr(value = "admob")
    ADMOB("2GZhl06xcO+6q16hiXeJCg"),//admob

    @EncryptStr(value = "fan")
    FACEBOOK("uMJ4MV2MtxFzWgoSnPWFEg"),//fan

    @EncryptStr(value = "facebook")
    FACEBOOK_ALT1("VrK7tcgX5quxHMKoV5q+2A"),

    @EncryptStr(value = "fb")
    FACEBOOK_ALT2("chSsHdE4VOngjtPdjWYQLg"),

    @EncryptStr(value = "mopub")
    MOPUB("SgRBYMctB4Dnj3yDXJyiSA");//mopub

    @EncryptStr(value = "network")
    public final static String TAG = "wAsFA2+iKcn3wY4l1Hzzag" ;

    static {
        if(AppUtil.sDebug){
            Log.d("copyTheOutputIntoCode", "AdNetwork.TAG:-->"+FunctionUtil.encrypt("network")+"<--");
            Log.d("verify", TAG+"-->"+FunctionUtil.decrypt(TAG)+"<--");

            Log.d("copyTheOutputIntoCode", "AdNetwork.ADMOB:-->"+FunctionUtil.encrypt("admob")+"<--");
            Log.d("verify", ADMOB.toString()+"-->"+FunctionUtil.decrypt(ADMOB.toString())+"<--");

            Log.d("copyTheOutputIntoCode", "AdNetwork.FACEBOOK:-->"+FunctionUtil.encrypt("fan")+"<--");
            Log.d("verify", FACEBOOK.toString()+"-->"+FunctionUtil.decrypt(FACEBOOK.toString())+"<--");

            Log.d("copyTheOutputIntoCode", "AdNetwork.FACEBOOK_ALT1:-->"+FunctionUtil.encrypt("facebook")+"<--");
            Log.d("verify", FACEBOOK_ALT1.toString()+"-->"+FunctionUtil.decrypt(FACEBOOK_ALT1.toString())+"<--");

            Log.d("copyTheOutputIntoCode", "AdNetwork.FACEBOOK_ALT2:-->"+FunctionUtil.encrypt("fb")+"<--");
            Log.d("verify", FACEBOOK_ALT2.toString()+"-->"+FunctionUtil.decrypt(FACEBOOK_ALT2.toString())+"<--");

            Log.d("copyTheOutputIntoCode", "AdNetwork.MOPUB:-->"+FunctionUtil.encrypt("mopub")+"<--");
            Log.d("verify", MOPUB.toString()+"-->"+FunctionUtil.decrypt(MOPUB.toString())+"<--");
        }
    }

    private final String msg;
    AdNetwork(String msg){
        this.msg = msg;
    }

    @Override
    public String toString() {
        return this.msg;
    }

    public static AdNetwork getNetwork(String msg){
        AdNetwork ret = null;

        if(FunctionUtil.decrypt(ADMOB.toString()).equalsIgnoreCase(msg)){
            ret = ADMOB;
        } else if(FunctionUtil.decrypt(FACEBOOK.toString()).equalsIgnoreCase(msg)
                || FunctionUtil.decrypt(FACEBOOK_ALT1.toString()).equalsIgnoreCase(msg)
                || FunctionUtil.decrypt(FACEBOOK_ALT2.toString()).equalsIgnoreCase(msg)){
            ret = FACEBOOK;
        } else if(FunctionUtil.decrypt(MOPUB.toString()).equalsIgnoreCase(msg)){
            ret = MOPUB;
        }

        return ret;
    }
}
