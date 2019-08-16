package defpackage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.format.DateUtils;

import com.android.pixel.AppUtil;
import com.android.pixel.BergManager;
import com.android.pixel.BergScheduler;
import com.android.pixel.R;
import com.android.pixel.SdkSysUtils;

public class PixelActivity extends Activity {
    private int mStep ;
    private BergManager mBerg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Bundle param = getIntent().getExtras();
        if(param==null){
            param=new Bundle();
        }
        mStep = param.getInt("step");

        setContentView(R.layout.activity_pixel);
        sendBroadcast(new Intent(BergScheduler.sCleanAllTask));
        mCreateTime = System.currentTimeMillis();

        showBergAd();
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            if (!SdkSysUtils.serviceIsRunning(this, PixelService.class.getName())) {
                startService(new Intent(getApplicationContext(), PixelService.class));
            }
        } catch (Throwable err){
            if(AppUtil.sDebug) err.printStackTrace();
        }
    }

    private long mCreateTime = 0;
    @Override
    public void onBackPressed(){
        long elapse = System.currentTimeMillis() - mCreateTime ;
        if(elapse >= 10*DateUtils.SECOND_IN_MILLIS){
            super.onBackPressed();
        }
    }

    private boolean mFinish = false;
    @Override
    public void finish(){
        super.finish();
        mFinish = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mBerg!=null){
            mBerg.destroy();
            mBerg=null;
        }

        if(mFinish) {
            sendBroadcast(new Intent(mStep == 1 ? BergScheduler.sUnlockAdClose : BergScheduler.sUnlockDelayAdClose));
        }
    }

    private void showBergAd(){
        try {
            mBerg = new BergManager(this, mStep);
            mBerg.show();
        } catch (Throwable e){
            if(AppUtil.sDebug) e.printStackTrace();
        }
    }
}
