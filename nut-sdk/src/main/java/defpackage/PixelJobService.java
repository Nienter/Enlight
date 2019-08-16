package defpackage;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.RequiresApi;

import com.android.pixel.AppUtil;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class PixelJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        try {
            startService(new Intent(getApplicationContext(), PixelService.class));
            jobFinished(params, false);
        } catch (Throwable e){
            if(AppUtil.sDebug) e.printStackTrace();
        }

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }
}
