package com.mh.simplerpc.util;

import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class CheckCode {

    public static String createCode() throws RuntimeException {

        String newUUID = UUID.randomUUID().toString();
        // MD5 to check code
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("NoSuchAlgorithmException to MD5",e);
        }

        messageDigest.update(newUUID.getBytes());
        byte[] encryptAfterData = messageDigest.digest();

        StringBuilder sb = new StringBuilder();
        for (byte item:encryptAfterData) {
            // Integer.toHexString((byteVar & 0x000000FF) | 0xFFFFFF00).substring(6)
            // byteVar & 0x000000FF的作用是，如果byteVar 是负数，则会清除前面24个零，正的byte整型不受影响。(...) | 0xFFFFFF00的作用是，如果byteVar 是正数，则置前24位为一，这样toHexString输出一个小于等于15的byte整型的十六进制时，倒数第二位为零且不会被丢弃，这样可以通过substring方法进行截取最后两位即可
            sb.append(Integer.toHexString((item & 0x00000FF | 0xFFFFFF00)).substring(6));
        }

        return sb.toString();
    }

    public static String signData(String code,String key) {

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(),"HmacSHA256");

            mac.init(secretKeySpec);




        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }


        return null;
    }



}
