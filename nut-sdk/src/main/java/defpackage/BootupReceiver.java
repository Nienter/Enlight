package defpackage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.android.pixel.AppUtil;
import com.android.pixel.BergScheduler;

public class BootupReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                BergScheduler.getInst();
            } else {
                context.startService(new Intent(context, PixelService.class));
            }
        } catch (Throwable e){
            if(AppUtil.sDebug) e.printStackTrace();
        }
    }
}
