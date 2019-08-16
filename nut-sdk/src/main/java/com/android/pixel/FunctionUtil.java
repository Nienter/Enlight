package com.android.pixel;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.spec.AlgorithmParameterSpec;
import android.util.Base64;

public class FunctionUtil {
    final private static String ENCRYPTION_KEY = "Android Activity";

    //must be 16 bytes
    final private static String ENCRYPTION_IV = "FacebookAudience";

    public static String encrypt(String src){
        try{
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, makeKey(), makeIv());
            return Base64.encodeToString(cipher.doFinal(src.getBytes()),
                    Base64.NO_WRAP|Base64.NO_PADDING);

        } catch (Throwable e) {
        }

        return "";
    }

    public static String decrypt(String src){
        try{
            byte[] decryptedStr = Base64.decode(src, Base64.DEFAULT);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, makeKey(), makeIv());
            byte[] originalByte = cipher.doFinal(decryptedStr);
            String originalStr = new String(originalByte);
            return originalStr;

        } catch (Throwable e){}

        return "";
    }

    static Key makeKey(){
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] key = md.digest(ENCRYPTION_KEY.getBytes("UTF-8"));
            return new SecretKeySpec(key, "AES");
        } catch (Exception e) {
        }

        return null;
    }

    static AlgorithmParameterSpec makeIv() {
        try {
            return new IvParameterSpec(ENCRYPTION_IV.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
        }
        return null;
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////
    public static int dp2px(float dp){
        return (int)(dp*AppUtil.getApp().getResources().getDisplayMetrics().density + 0.5f);
    }


}
