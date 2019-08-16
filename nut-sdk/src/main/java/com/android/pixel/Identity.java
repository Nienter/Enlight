package com.android.pixel;

import android.text.TextUtils;
import android.util.Log;

import com.EncryptStr;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;

import java.util.UUID;

public class Identity {
    private static volatile Identity sInst = null;
    public static Identity getInst(){
        if(sInst==null){
            synchronized (Identity.class){
                if(sInst==null){
                    sInst = new Identity();
                }
            }
        }

        return sInst;
    }

    private volatile String mId="";
    private Identity(){
        String idInPersist = reloadId();
        if(!TextUtils.isEmpty(idInPersist)){
            mId = idInPersist;
        } else {
            generateId();
        }
    }

    public String getId(){
        return mId;
    }


    @EncryptStr(value = "identity")
    private static String sID_Key = "h1HdLJv/G21ZzD6JkbCz7Q" ;
    static {
        if(AppUtil.sDebug){
            Log.d("copyTheOutputIntoCode", "Identity.sID_Key:-->"+FunctionUtil.encrypt("identity")+"<--");
            Log.d("verify", sID_Key+"-->"+FunctionUtil.decrypt(sID_Key)+"<--");
        }
    }
    private String reloadId(){
        return LocalStorageManager.getInst().getString(sID_Key, "");
    }

    private void saveId(String id){
        LocalStorageManager.getInst().updateString(sID_Key, id);
    }

    private void generateId(){
        new Thread(new Runnable() {
            @Override
            public void run() {

                try{
                    String googleAdId = null;
                    AdvertisingIdClient.Info info = AdvertisingIdClient.getAdvertisingIdInfo(AppUtil.getApp());
                    googleAdId = info.getId();
                    mId = UUID.nameUUIDFromBytes(googleAdId.getBytes("UTF-8")).toString();
                } catch ( Throwable e){
                    if(AppUtil.sDebug) e.printStackTrace();
                }

                if(TextUtils.isEmpty(mId)){
                    mId = UUID.randomUUID().toString();
                }

                saveId(mId);
            }
        }).start();

    }
}
