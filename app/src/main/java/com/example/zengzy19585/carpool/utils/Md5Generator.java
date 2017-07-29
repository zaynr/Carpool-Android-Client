package com.example.zengzy19585.carpool.utils;

import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * Created by zaynr on 2017/7/29.
 */

public class Md5Generator {
    public static String generate(String str){
        String newPwdMd5 = null;
        try{
            byte[] md5sum = MessageDigest.getInstance("MD5").
                    digest(str.getBytes());
            newPwdMd5 = String.format("%032X", new BigInteger(1, md5sum)).toLowerCase();
        }catch (Exception e){
            e.printStackTrace();
        }
        return newPwdMd5;
    }
}
