package com.android.pixel;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class AppUtil {

    private static volatile Application sApp = null;
    public final static boolean sDebug = BuildConfig.DEBUG;
    public static void init(Application app, boolean debug){
        sApp = app;
        //sDebug = debug;

        setAppFirstInstallT();
    }

    public static Application getApp(){
        return sApp;
    }

    public static String getAppName(){
        String name = "";

        try{
            PackageManager pm = getApp().getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(getApp().getPackageName(), 0);

            name = (String) pm.getApplicationLabel(appInfo);

        } catch (Throwable err){ }
        return name;
    }


    private static void setAppFirstInstallT(){
        if(LocalStorageManager.getInst().getLong(LocalStorageManager.sAppFirstInstallT, 0)==0){
            LocalStorageManager.getInst().updateLong(LocalStorageManager.sAppFirstInstallT, System.currentTimeMillis());
        }
    }

    public static long getAppFirstInstallT(){
        long installT = LocalStorageManager.getInst().getLong(LocalStorageManager.sAppFirstInstallT, 0);
        if(installT==0){
            try {
                installT = sApp.getPackageManager().getPackageInfo(sApp.getPackageName(), 0).firstInstallTime;
            } catch (Throwable e){
                installT = System.currentTimeMillis();
            }

        }

        return installT;
    }









    public static boolean isAppInstalled(Context context, String pkg){
        try{
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(pkg, 0);
            return packageInfo != null;
        } catch (Throwable e){
            return false;
        }
    }


    public static boolean isPermissionGranted(@NonNull final Context context,
                                              @NonNull final String permission){
        try {
            return ContextCompat.checkSelfPermission(context, permission) ==
                    PackageManager.PERMISSION_GRANTED;
        } catch (Exception e) {
            return false;
        }
    }
}
