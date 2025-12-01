package com.example.newsapplication.utils;

import android.content.Context;

public class PreferenceUtils {
    public static void putString(Context context, String prefsName, String key, String value) {
        context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
               .edit()
               .putString(key, value)
               .apply();
    }

    public static String getString(Context context, String prefsName, String key, String defaultValue) {
        return context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
                      .getString(key, defaultValue);
    }

    public static void remove(Context context, String prefsName, String key) {
        context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
               .edit()
               .remove(key)
               .apply();
    }
}