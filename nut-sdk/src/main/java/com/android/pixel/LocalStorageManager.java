package com.android.pixel;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.EncryptStr;

public class LocalStorageManager {
    @EncryptStr(value = "app_first_install_time")
    public final static String sAppFirstInstallT= "vKBS4xYpzb/7cut1J5Aa35FNNWuJdAsI2/hOIrJky/w" ;
    static {
        if(AppUtil.sDebug){
            Log.d("copyTheOutputIntoCode", "app_first_install_time:-->"+FunctionUtil.encrypt("app_first_install_time")+"<--");
            Log.d("verify", sAppFirstInstallT+"-->"+FunctionUtil.decrypt(sAppFirstInstallT)+"<--");
        }
    }


    private static LocalStorageManager sInst = null;
    private static void init(Context context){
        if(sInst ==null){
            synchronized (LocalStorageManager.class){
                sInst = new LocalStorageManager(context);
            }
        }

    }

    public static LocalStorageManager getInst(){
        if(sInst==null){
            init(AppUtil.getApp());
        }

        return sInst;
    }

    private SharedPreferences mPref;
    private LocalStorageManager(Context context){
        String prefFile = context.getPackageName()+"_pref";

        mPref = context.getSharedPreferences(prefFile, Context.MODE_PRIVATE);
    }

    public long getLong(String key, long defVal){
        return mPref.getLong(key, defVal);
    }
    public void updateLong(String key, long val){
        mPref.edit().putLong(key, val).apply();
    }

    public String getString(String key, String defVal){
        return mPref.getString(key, defVal);
    }
    public void updateString(String key, String val){
        mPref.edit().putString(key, val).apply();
    }


    public boolean getBoolean(String key, boolean defVal){
        return mPref.getBoolean(key, defVal);
    }
    public void updateBoolean(String key, boolean val){
        mPref.edit().putBoolean(key, val).apply();
    }
}
