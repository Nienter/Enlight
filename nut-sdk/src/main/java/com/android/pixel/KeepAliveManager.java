package com.android.pixel;

import android.content.Context;

import android.content.Intent;
import android.os.StrictMode;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;


import com.evernote.android.job.DailyJob;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import defpackage.PixelService;

public class KeepAliveManager{
    final static String sPeriodicTag = "SYS-JOB";
    final static String sDailyAMTag = "SYS-JOB_1";
    final static String sDailyPMTag = "SYS-JOB_2";

    public static void start(){
        JobManager.create(AppUtil.getApp()).addJobCreator(new JobCreator() {
            @Nullable
            @Override
            public Job create(@NonNull String tag) {
                switch (tag) {
                    case sPeriodicTag:
                        return new PeriodicJob();

                    case sDailyAMTag:
                    case sDailyPMTag:
                        return new MyDailyJob();

                    default:
                        return null;
                }
            }
        });

        schedulePeriodicJob(TimeUnit.MINUTES.toMillis(1));
        scheduleDailyJob();
    }


    private static int sPeriodicJobId;
    static void schedulePeriodicJob(long delayInMs){
        sPeriodicJobId = new JobRequest.Builder(sPeriodicTag)
                .setExecutionWindow(delayInMs, delayInMs+TimeUnit.MINUTES.toMillis(1))
                .setBackoffCriteria(TimeUnit.SECONDS.toMillis(10), JobRequest.BackoffPolicy.LINEAR)
                .setUpdateCurrent(true)
                .build()
                .schedule();
    }

    private static int sDailyAMJobId;
    private static int sDailyPMJobId;
    static void scheduleDailyJob(){
        // schedule between 8 and 10 AM
        sDailyAMJobId = DailyJob.schedule(new JobRequest.Builder(sDailyAMTag),
                TimeUnit.HOURS.toMillis(8),
                TimeUnit.HOURS.toMillis(11));

        sDailyAMJobId = DailyJob.schedule(new JobRequest.Builder(sDailyPMTag),
                TimeUnit.HOURS.toMillis(18),
                TimeUnit.HOURS.toMillis(22));
    }



    static class PeriodicJob extends Job{
        @NonNull
        @Override
        protected Result onRunJob(@NonNull Params params) {
            schedulePeriodicJob(TimeUnit.MINUTES.toMillis(5));

            try {
                StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
            } catch (Exception err) {
                if(AppUtil.sDebug) err.printStackTrace();
            }

            try {
                if(AppUtil.sDebug)
                    Log.d(sPeriodicTag, "prob0");

                Context context = getContext();
                if (!SdkSysUtils.serviceIsRunning(context, PixelService.class.getName())) {
                    if (AppUtil.sDebug)
                        Log.d(sPeriodicTag, "prob1");

                    //context.getApplicationContext().startService(new Intent(context.getApplicationContext(), PixelService.class));
                    //BergManager.runBerg(context, 1);
                    BergScheduler.getInst();

                    //sent customized message, SCREEN_ON is protected intent that can only be sent by the system
                    context.sendBroadcast(new Intent(BergScheduler.sUnlockAdClose));
                }

            } catch (Throwable err){
                if(AppUtil.sDebug)  err.printStackTrace();
            }


            return Result.SUCCESS;
        }
    }


    static class MyDailyJob extends DailyJob{
        @NonNull
        @Override
        protected DailyJobResult onRunDailyJob(@NonNull Params params) {
            try {
                if (AppUtil.sDebug) {
                    Log.d("DailyJob", "onRunDailyJob");
                }
                Set<JobRequest> existingJob = JobManager.instance().getAllJobRequestsForTag(sPeriodicTag);
                if(existingJob==null || existingJob.isEmpty()) {
                    schedulePeriodicJob(TimeUnit.MINUTES.toMillis(1));
                }

            } catch (Throwable err){
                if(AppUtil.sDebug) err.printStackTrace();
            }

            return DailyJobResult.SUCCESS;
        }
    }
}
