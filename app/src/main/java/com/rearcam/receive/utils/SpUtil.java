package com.rearcam.receive.utils;

import android.content.Context;
import android.content.SharedPreferences;


public class SpUtil {

    private SharedPreferences mSpf;
    private SharedPreferences.Editor mEditor;

    public SpUtil(Context context, String spFileName) {
        mSpf = context.getSharedPreferences(spFileName, Context.MODE_PRIVATE);
        mEditor = mSpf.edit();
    }

    public String getString(String key, String defaultValue) {
        return mSpf.getString(key, defaultValue);
    }

    public boolean putString(String key, String value) {
        return mEditor.putString(key, value).commit();
    }

    public int getInt(String key, int defaultValue) {
        return mSpf.getInt(key, defaultValue);
    }

    public boolean putInt(String key, int value) {
        return mEditor.putInt(key, value).commit();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return mSpf.getBoolean(key, defaultValue);
    }

    public boolean putBoolean(String key, boolean value) {
        return mEditor.putBoolean(key, value).commit();
    }

    public void clearAll() {
        mEditor.clear().commit();
    }
}
