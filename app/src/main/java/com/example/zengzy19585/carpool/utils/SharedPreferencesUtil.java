package com.example.zengzy19585.carpool.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by zengzy19585 on 2017/7/12.
 */

public class SharedPreferencesUtil {
    private Context context;
    private SharedPreferences sharedPreferences;
    private String prefName;

    public SharedPreferencesUtil(Context context, String prefName){
        this.context = context;
        this.prefName = prefName;
        sharedPreferences = context.getSharedPreferences(prefName, context.MODE_PRIVATE);
    }

    public void setStringValue(String key, String value){
        sharedPreferences.edit().putString(key, value).apply();
    }

    public String getStringValue(String key){
        return sharedPreferences.getString(key, prefName);
    }

}
