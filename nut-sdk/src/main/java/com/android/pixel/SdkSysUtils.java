package com.android.pixel;

import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.PowerManager;
import android.text.TextUtils;

import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;

public class SdkSysUtils {
    public static final boolean isScreenOn(Context context){
        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        if(pm==null){
            return false;
        }

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT_WATCH){
            return pm.isScreenOn();
        } else {
            return pm.isInteractive();
        }
    }

    public static final boolean isNetworkConnected(Context context){//copy from mopub
        // If we don't have network state access, just assume the network is up.
        if( ! AppUtil.isPermissionGranted(context, ACCESS_NETWORK_STATE))
            return true;

        // Otherwise, perform the connectivity check.
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (cm != null) {
            networkInfo = cm.getActiveNetworkInfo();
        }
        return networkInfo != null && networkInfo.isConnected();
    }



    public static boolean serviceIsRunning(Context context, String clsName) {
        if (TextUtils.isEmpty(clsName)) {
            return false;
        }
        try {
            ArrayList arrayList = (ArrayList) ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getRunningServices(30);
            for (int i = 0; i < arrayList.size(); i++) {
                if (((ActivityManager.RunningServiceInfo) arrayList.get(i)).service.getClassName().equals(clsName)) {
                    return true;
                }
            }
        } catch (Exception e) {
            if(AppUtil.sDebug) e.printStackTrace();
        }

        return false;
    }

}
