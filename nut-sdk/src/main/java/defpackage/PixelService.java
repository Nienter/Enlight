package defpackage;

import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.text.format.DateUtils;

import com.android.pixel.BergScheduler;


public class PixelService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            JobInfo job = new JobInfo.Builder(1, new ComponentName(getPackageName(), PixelJobService.class.getName()))
                    .setPeriodic(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                            ? DateUtils.SECOND_IN_MILLIS * 901
                            : DateUtils.SECOND_IN_MILLIS * 299)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .build();

            ((JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE))
                    .schedule(job);
        }

        BergScheduler.getInst();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }
}
