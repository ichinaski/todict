package com.ichinaski.todict.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Prefs {
    public static final int DICT_NONE = -1;
    private static final String PREF_DEFAULT_DICT = "pref_dict";
    
    private static SharedPreferences getPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
    
    public static long getDefaultDict(Context context) {
        return getPrefs(context).getLong(PREF_DEFAULT_DICT, DICT_NONE);
    }
    
    public static void setDefaultDict(Context context, long value) {
        getPrefs(context).edit().putLong(PREF_DEFAULT_DICT, value).commit();
    }

}
