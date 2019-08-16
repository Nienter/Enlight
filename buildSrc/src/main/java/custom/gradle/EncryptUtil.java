package custom.gradle;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptUtil {
    final private static String ENCRYPTION_KEY = StringEncryptPlugin.sKey;

    //must be 16 bytes
    final private static String ENCRYPTION_IV = StringEncryptPlugin.sIv;

    public static String encrypt(String src){
        try{
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, generateKey(), generateIv());
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
            cipher.init(Cipher.DECRYPT_MODE, generateKey(), generateIv());
            byte[] originalByte = cipher.doFinal(decryptedStr);
            String originalStr = new String(originalByte);
            return originalStr;

        } catch (Throwable e){}

        return "";
    }

    static Key generateKey(){
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] key = md.digest(ENCRYPTION_KEY.getBytes("UTF-8"));
            return new SecretKeySpec(key, "AES");
        } catch (Exception e) {
        }

        return null;
    }

    static AlgorithmParameterSpec generateIv() {
        try {
            return new IvParameterSpec(ENCRYPTION_IV.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
        }
        return null;
    }
}
