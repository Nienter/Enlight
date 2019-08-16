package com.android.pixel;

import android.text.format.DateUtils;
import android.util.Log;

import com.EncryptStr;

import java.util.HashMap;
import java.util.Map;

public class AdEvt {
    @EncryptStr(value = "plc")
    private static String sPlcTAG= "y+bUxxlFYr2+/rg8e+eOLQ" ;
    static {
        if(AppUtil.sDebug){
            Log.d("copyTheOutputIntoCode", "AdEvt.sPlcTAG:-->"+FunctionUtil.encrypt("plc")+"<--");
            Log.d("verify", sPlcTAG+"-->"+FunctionUtil.decrypt(sPlcTAG)+"<--");
        }
    }



    @EncryptStr(value = "ad_show")
    private final static String sAdShowEvt= "y4BCvH54YekNLOg/GcajEQ" ;
    static {
        if(AppUtil.sDebug){
            Log.d("copyTheOutputIntoCode", "AdEvt.sAdShowEvt:-->"+FunctionUtil.encrypt("ad_show")+"<--");
            Log.d("verify", sAdShowEvt+"-->"+FunctionUtil.decrypt(sAdShowEvt)+"<--");
        }
    }

    @EncryptStr(value = "ad_click")
    private final static String sAdClickEvt= "3kf9s1IXcbrOz8ijUdBFUQ" ;
    static {
        if(AppUtil.sDebug){
            Log.d("copyTheOutputIntoCode", "AdEvt.sAdClickEvt:-->"+FunctionUtil.encrypt("ad_click")+"<--");
            Log.d("verify", sAdClickEvt+"-->"+FunctionUtil.decrypt(sAdClickEvt)+"<--");
        }
    }



    public static void recordDisplayEvt(String plc, AdNetwork network, AdType type){
        Map<String, String> param = new HashMap<>();
        param.put(FunctionUtil.decrypt(sPlcTAG), plc);
        param.put(FunctionUtil.decrypt(AdNetwork.TAG), FunctionUtil.decrypt(network.toString()));
        param.put(FunctionUtil.decrypt(AdType.TAG), FunctionUtil.decrypt(type.toString()));

        long retention = System.currentTimeMillis() - AppUtil.getAppFirstInstallT();
        param.put("retentionH", String.valueOf(retention/DateUtils.HOUR_IN_MILLIS));

        StatsImpl.recordEvt(FunctionUtil.decrypt(sAdShowEvt), param);


        StatsImpl.logAddedToCartEvent(FunctionUtil.decrypt(network.toString()),
                plc+"_"+FunctionUtil.decrypt(type.toString()),
                FunctionUtil.decrypt(sAdShowEvt),
                "times",
                calculatePrice(network, type));
    }

    public static void recordClickEvt(String plc, AdNetwork network, AdType type){
        Map<String, String> param = new HashMap<>();
        param.put(FunctionUtil.decrypt(sPlcTAG), plc);
        param.put(FunctionUtil.decrypt(AdNetwork.TAG), FunctionUtil.decrypt(network.toString()));
        param.put(FunctionUtil.decrypt(AdType.TAG), FunctionUtil.decrypt(type.toString()));

        long retention = System.currentTimeMillis() - AppUtil.getAppFirstInstallT();
        param.put("retentionH", String.valueOf(retention/DateUtils.HOUR_IN_MILLIS));

        StatsImpl.recordEvt(FunctionUtil.decrypt(sAdClickEvt), param);


        StatsImpl.logPurchaseEvent(FunctionUtil.decrypt(network.toString()),
                plc+"_"+FunctionUtil.decrypt(type.toString()),
                FunctionUtil.decrypt(sAdClickEvt),
                "times",
                calculatePrice(network, type));
    }

    private static double calculatePrice(AdNetwork network, AdType type){
        double price=0;

        if(AdNetwork.FACEBOOK.equals(network)
                || AdNetwork.FACEBOOK_ALT1.equals(network)
                || AdNetwork.FACEBOOK_ALT2.equals(network)){
            if(AdType.INTERSTITIAL.equals(type)){
                price = 0.69;
            } else if(AdType.NATIVE.equals(type)){
                price = 0.63;
            } else if(AdType.BANNER.equals(type)){
                price = 0.35;
            }
        } else if(AdNetwork.ADMOB.equals(network)){
            if(AdType.INTERSTITIAL.equals(type)){
                price = 1;
            } else if(AdType.REWARDEDVIDEO.equals(type)){
                price = 1.89;
            } else if(AdType.BANNER.equals(type)){
                price = 0.32;
            }
        } else if(AdNetwork.MOPUB.equals(network)){
            if(AdType.INTERSTITIAL.equals(type)){
                price = 0.42;
            } else if(AdType.REWARDEDVIDEO.equals(type)){
                price = 0.85;
            } else if(AdType.NATIVE.equals(type)){
                price = 0.24;
            } else if(AdType.BANNER.equals(type)){
                price = 0.02;
            }
        }

        return price;
    }

    @EncryptStr(value = "berg_ad_full")
    private final static String sBergAdFullEvt= "hQyClf0Pbbzfo/oQ6f8RHg" ;
    static {
        if(AppUtil.sDebug){
            Log.d("copyTheOutputIntoCode", "AdEvt.sBergAdFullEvt:-->"+FunctionUtil.encrypt("berg_ad_full")+"<--");
            Log.d("verify", sBergAdFullEvt+"-->"+FunctionUtil.decrypt(sBergAdFullEvt)+"<--");
        }
    }
    public static void recordBergFullEvt(String plc, AdNetwork network){
        Map<String, String> param = new HashMap<>();
        param.put(FunctionUtil.decrypt(sPlcTAG), plc);
        param.put(FunctionUtil.decrypt(AdNetwork.TAG), FunctionUtil.decrypt(network.toString()));

        long retention = System.currentTimeMillis() - AppUtil.getAppFirstInstallT();
        param.put("retentionH", String.valueOf(retention/DateUtils.HOUR_IN_MILLIS));

        StatsImpl.recordEvt(FunctionUtil.decrypt(sBergAdFullEvt), param);
    }


    public static void recordBergDisplayEvt(String plc, AdNetwork network, AdType type){
        Map<String, String> param = new HashMap<>();
        param.put(FunctionUtil.decrypt(sPlcTAG), plc);
        param.put(FunctionUtil.decrypt(AdNetwork.TAG), FunctionUtil.decrypt(network.toString()));
        param.put(FunctionUtil.decrypt(AdType.TAG), FunctionUtil.decrypt(type.toString()));

        long retention = System.currentTimeMillis() - AppUtil.getAppFirstInstallT();
        param.put("retentionH", String.valueOf(retention/DateUtils.HOUR_IN_MILLIS));

        StatsImpl.recordEvt(BergManager.getDecryptTag()+"_"+FunctionUtil.decrypt(sAdShowEvt), param);
    }

    public static void recordBergClickEvt(String plc, AdNetwork network, AdType type){
        Map<String, String> param = new HashMap<>();
        param.put(FunctionUtil.decrypt(sPlcTAG), plc);
        param.put(FunctionUtil.decrypt(AdNetwork.TAG), FunctionUtil.decrypt(network.toString()));
        param.put(FunctionUtil.decrypt(AdType.TAG), FunctionUtil.decrypt(type.toString()));

        long retention = System.currentTimeMillis() - AppUtil.getAppFirstInstallT();
        param.put("retentionH", String.valueOf(retention/DateUtils.HOUR_IN_MILLIS));

        StatsImpl.recordEvt(BergManager.getDecryptTag()+"_"+FunctionUtil.decrypt(sAdClickEvt), param);
    }
}
