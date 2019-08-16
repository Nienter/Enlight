package com.android.pixel;

import android.util.Log;

import com.EncryptStr;

public enum AdSize {
    @EncryptStr(value = "small")
    SMALL("cPLvb6nCZq9J85J1zECYOQ"),           //AdMob:BANNER,  FAN:BANNER_50, Mopub:banner_320x50

    @EncryptStr(value = "medium")
    MEDIUM("/gICNoCTTqWW6q/lZRLqGA"),           //AdMob:MEDIUM_RECTANGLE, FAN:RECTANGLE_HEIGHT_250, Mopub:Medium_300*250

    @EncryptStr(value = "fullscreen")
    FULLSCREEN("RTEIspjeTBa+KV1GQ7ifFQ");

    @EncryptStr(value = "size")
    public final static String TAG = "r/QN/9AIFRHnrbu/JhTGSA" ;
    static {
        if(AppUtil.sDebug){
            Log.d("copyTheOutputIntoCode", "AdSize.TAG:-->"+FunctionUtil.encrypt("size")+"<--");
            Log.d("verify", TAG+"-->"+FunctionUtil.decrypt(TAG)+"<--");

            Log.d("copyTheOutputIntoCode", "AdSize.SMALL:-->"+FunctionUtil.encrypt("small")+"<--");
            Log.d("verify", SMALL.toString()+"-->"+FunctionUtil.decrypt(SMALL.toString())+"<--");

            Log.d("copyTheOutputIntoCode", "AdSize.MEDIUM:-->"+FunctionUtil.encrypt("medium")+"<--");
            Log.d("verify", MEDIUM.toString()+"-->"+FunctionUtil.decrypt(MEDIUM.toString())+"<--");

            Log.d("copyTheOutputIntoCode", "AdSize.FULLSCREEN:-->"+FunctionUtil.encrypt("fullscreen")+"<--");
            Log.d("verify", FULLSCREEN.toString()+"-->"+FunctionUtil.decrypt(FULLSCREEN.toString())+"<--");
        }
    }

    private final String msg;
    AdSize(String message) {
        this.msg = message;
    }

    @Override
    public String toString() {
        return this.msg;
    }

    public static AdSize getSize(String msg){
        AdSize ret = null;

        if(FunctionUtil.decrypt(SMALL.toString()).equalsIgnoreCase(msg)){
            ret = SMALL;
        } else if(FunctionUtil.decrypt(MEDIUM.toString()).equalsIgnoreCase(msg)){
            ret = MEDIUM;
        } else if (FunctionUtil.decrypt(FULLSCREEN.toString()).equalsIgnoreCase(msg)) {
            ret = FULLSCREEN;
        }

        return ret ;
    }
}
