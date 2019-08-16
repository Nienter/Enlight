package com.android.pixel;

import android.util.Log;

import com.EncryptStr;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class BergStats {
    private static BergStats sInst = null;
    public static BergStats getInst(){
        if(sInst==null){
            synchronized (BergStats.class){
                if(sInst==null){
                    sInst = new BergStats();
                }
            }
        }

        return sInst;
    }

    @EncryptStr(value = "imp_track_last_day")
    private final static String IMP_TRACK_LAST_DAY = "fnaCIVLM+S/zU04L8maVu9eaVfMympsXvC4xYOPGxOM" ;

    @EncryptStr(value = "imp_today_times")
    private final static String IMP_TODAY_TIMES    = "FsDtYOxpP2qOznfMiJSexA" ;
    static {
        if(AppUtil.sDebug){
            Log.d("copyTheOutputIntoCode", "BergStats.IMP_TRACK_LAST_DAY:-->"+FunctionUtil.encrypt("imp_track_last_day")+"<--");
            Log.d("verify", IMP_TRACK_LAST_DAY+"-->"+FunctionUtil.decrypt(IMP_TRACK_LAST_DAY)+"<--");

            Log.d("copyTheOutputIntoCode", "BergStats.IMP_TODAY_TIMES:-->"+FunctionUtil.encrypt("imp_today_times")+"<--");
            Log.d("verify", IMP_TODAY_TIMES+"-->"+FunctionUtil.decrypt(IMP_TODAY_TIMES)+"<--");


        }
    }

    public long getTodayImpressionTimes(){
        //combine to "berg_imp_track_last_day"
        String lastDayKey = BergManager.getTag()//+BergManager.getIntPlc()
                +"_"+IMP_TRACK_LAST_DAY;

        //combine to "berg_imp_today_times"
        String impTodayTimesKey = BergManager.getTag()//+BergManager.getIntPlc()
                +"_"+IMP_TODAY_TIMES;

        String lastDay = LocalStorageManager.getInst().getString(lastDayKey, "");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT-8:00"));
        if(dateFormat.format(new Date(System.currentTimeMillis())).equalsIgnoreCase(lastDay)){
            return LocalStorageManager.getInst().getLong(impTodayTimesKey, 0);
        } else {
            updateTodayImpressionTimes(0);

            for (AdNetwork network:BergManager.sDefaultNetwork) {
                updateTodayImpressionTimesNetwork(network, 0);
                updateTotalInnerClickTimesNetwork(network, 0);
            }
            return 0;
        }
    }
    public void updateTodayImpressionTimes(long times){
        //combine to "berg_imp_track_last_day"
        String lastDayKey = BergManager.getTag()//+BergManager.getIntPlc()
                +"_"+IMP_TRACK_LAST_DAY;

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT-8:00"));
        String today = dateFormat.format(new Date(System.currentTimeMillis()));
        LocalStorageManager.getInst().updateString(lastDayKey, today);

        //combine to "berg_imp_today_times"
        String impTodayTimesKey = BergManager.getTag()//+BergManager.getIntPlc()
                +"_"+IMP_TODAY_TIMES;
        LocalStorageManager.getInst().updateLong(impTodayTimesKey, times);
    }


    public long getTodayImpressionTimesNetwork(AdNetwork network){
        //combine to "berg_imp_track_last_day"
        String lastDayKey = BergManager.getTag()//+BergManager.getIntPlc()
                +"_"+IMP_TRACK_LAST_DAY;

        String lastDay = LocalStorageManager.getInst().getString(lastDayKey, "");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT-8:00"));
        if(dateFormat.format(new Date(System.currentTimeMillis())).equalsIgnoreCase(lastDay)){
            //combine to "berg_imp_today_times_fan"
            String key = BergManager.getTag()//+BergManager.getIntPlc()
                    +"_"+IMP_TODAY_TIMES
                    +"_"+network.toString();
            return LocalStorageManager.getInst().getLong(key, 0);
        } else {
            return 0;
        }
    }

    public void updateTodayImpressionTimesNetwork(AdNetwork network, long times){
        //combine to "berg_imp_today_times_fan"
        String key = BergManager.getTag()//+BergManager.getIntPlc()
                +"_"+IMP_TODAY_TIMES
                +"_"+network.toString();
        LocalStorageManager.getInst().updateLong(key, times);
    }



    @EncryptStr(value = "inner_click_times")
    private final static String INNER_CLICK_TIMES = "Agd+EbUmEZjuYZxXgOqesypn11n2QhxPpMjI6gVWn/I" ;
    static {
        if(AppUtil.sDebug){
            Log.d("copyTheOutputIntoCode", "BergStats.INNER_CLICK_TIMES:-->"+FunctionUtil.encrypt("inner_click_times")+"<--");
            Log.d("verify", INNER_CLICK_TIMES+"-->"+FunctionUtil.decrypt(INNER_CLICK_TIMES)+"<--");
        }
    }
    /**
     * return the clicked times for native ad out of app for specified network
     * @param network
     * @return
     */
    public long getTotalInnerClickTimesNetwork(AdNetwork network){
        //combine to "bergInner_inner_click_times_admob"
        String key = BergManager.getTag()+BergInner.getInnerPlc()
                +"_"+INNER_CLICK_TIMES
                +"_"+network.toString();
        return LocalStorageManager.getInst().getLong(key, 0);
    }

    public void updateTotalInnerClickTimesNetwork(AdNetwork network, long times){
        //combine to "bergInner_native_click_times_admob"
        String key = BergManager.getTag()+BergInner.getInnerPlc()
                +"_"+INNER_CLICK_TIMES
                +"_"+network.toString();
        LocalStorageManager.getInst().updateLong(key, times);
    }
}
