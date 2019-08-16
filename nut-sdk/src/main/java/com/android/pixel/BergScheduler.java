package com.android.pixel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.format.DateUtils;
import android.util.Log;

import com.EncryptStr;

public class BergScheduler {
    private volatile static BergScheduler sInst=null ;

    public static BergScheduler getInst(){
        if(sInst==null){
           synchronized (BergScheduler.class){
               if(sInst == null){
                   sInst = new BergScheduler(AppUtil.getApp());
               }
           }
        }

        return sInst;
    }



    @EncryptStr(value = "UNLOCK_AD_CLOSE_ACTION")
    public final static String sUnlockAdClose = "kN2/wH6NFWe9Z8p/SCer28rw8WEV2NYXesubzw45ut0" ;

    @EncryptStr(value = "UNLOCK_DELAY_AD_CLOSE_ACTION")
    public final static String sUnlockDelayAdClose = "NFu2MyKqTEFBCpvHe8OIj7NLoYwyqB1yROghmmPpLxs" ;

    @EncryptStr(value = "CLEAN_ALL_TASK_ACTION")
    public final static String sCleanAllTask = "mQcvxsJWomNorSDYm8k+/ilbaXT2dzQ9doIfdlQBjJ4" ;

    static {
        if(AppUtil.sDebug){
            Log.d("copyTheOutputIntoCode", "BergScheduler.sUnlockAdClose:-->"+FunctionUtil.encrypt("UNLOCK_AD_CLOSE_ACTION")+"<--");
            Log.d("verify", sUnlockAdClose+"-->"+FunctionUtil.decrypt(sUnlockAdClose)+"<--");

            Log.d("copyTheOutputIntoCode", "BergScheduler.sUnlockDelayAdClose:-->"+FunctionUtil.encrypt("UNLOCK_DELAY_AD_CLOSE_ACTION")+"<--");
            Log.d("verify", sUnlockDelayAdClose+"-->"+FunctionUtil.decrypt(sUnlockDelayAdClose)+"<--");

            Log.d("copyTheOutputIntoCode", "BergScheduler.sCleanAllTask:-->"+FunctionUtil.encrypt("CLEAN_ALL_TASK_ACTION")+"<--");
            Log.d("verify", sCleanAllTask+"-->"+FunctionUtil.decrypt(sCleanAllTask)+"<--");
        }
    }




    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(AppUtil.sDebug) {
                Log.d("PixelService", "action=" + action);
            }
            //flow: screen_on->3s,pop ->90s, pop->5m, pop ->5m, pop .....
            //the chain would be interrupted once the screen is off
            if(Intent.ACTION_SCREEN_ON.equals(action)){
                cancelAllTask();

                if(BergConfig.getInst().isAdOutAppEnabled()){
                    mHandler.postDelayed(mUnlockBergTsk, 3* DateUtils.SECOND_IN_MILLIS);
                }

            } else if(Intent.ACTION_SCREEN_OFF.equals(action)){
                cancelAllTask();

                mHandler.postDelayed(mSubTsk, 3*DateUtils.SECOND_IN_MILLIS);

            } else if(sUnlockAdClose.equals(action)){
                cancelAllTask();

                long unlockAdDelayT=90;
                mHandler.postDelayed(mUnlockDelayBergTsk, unlockAdDelayT*DateUtils.SECOND_IN_MILLIS);

            } else if(sUnlockDelayAdClose.equals(action)){
                cancelAllTask();

                long adGapT=300;
                mHandler.postDelayed(mUnlockDelayBergTsk, adGapT*DateUtils.SECOND_IN_MILLIS);
            } else if (sCleanAllTask.equals(action)) {
                cancelAllTask();
            }
        }
    };


    private HandlerThread mThread = null;
    private Handler mHandler = null;
    private BergScheduler(Context context){
        mThread = new HandlerThread("sch-thread");
        mThread.start();
        mHandler = new Handler(mThread.getLooper());


        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(sUnlockAdClose);
        filter.addAction(sUnlockDelayAdClose);
        filter.addAction(sCleanAllTask);
        context.getApplicationContext().registerReceiver(mReceiver, filter);
    }



    private Runnable mSubTsk = new Runnable() {
        @Override
        public void run() {
            int isSub = 0 ; //?
            int closeAd = 0; //?
            if(isSub==1 && closeAd==1){//for subscription ?
                //startActivity SubActivity
            }
        }
    };

    private Runnable mUnlockBergTsk = new Runnable() {
        @Override
        public void run() {
            try {
                BergManager.runBerg(AppUtil.getApp(), 1);
            } catch (Throwable e){
                if(AppUtil.sDebug){
                    e.printStackTrace();
                }
            }
        }
    };

    private Runnable mUnlockDelayBergTsk = new Runnable() {
        @Override
        public void run() {
            BergManager.runBerg(AppUtil.getApp(), 2);
        }
    };

    private void cancelAllTask(){
        mHandler.removeCallbacks(mUnlockBergTsk);
        mHandler.removeCallbacks(mUnlockDelayBergTsk);
        mHandler.removeCallbacks(mSubTsk);
    }
}
