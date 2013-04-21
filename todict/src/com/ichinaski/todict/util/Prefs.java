package com.ichinaski.todict.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Prefs {
    public static final int DICT_NONE = -1;
    private static final String PREF_DEFAULT_DICT = "pref_dict";
    private static final String PREF_WIDGET_PREFIX = "widget_";
    
    private static SharedPreferences getPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
    
    public static long getDefaultDict(Context context) {
        return getPrefs(context).getLong(PREF_DEFAULT_DICT, DICT_NONE);
    }
    
    public static void setDefaultDict(Context context, long value) {
        getPrefs(context).edit().putLong(PREF_DEFAULT_DICT, value).commit();
    }
    
    public static void setWidgetDict(Context context, int widgetID, long dictID) {
        getPrefs(context).edit().putLong(PREF_WIDGET_PREFIX + widgetID, dictID).commit();
    }
    
    public static long getWidgetDictID(Context context, int widgetID) {
        return getPrefs(context).getLong(PREF_WIDGET_PREFIX + widgetID, DICT_NONE);
    }
    
    public static void deleteWidgetInfo(Context context, int widgetID) {
        getPrefs(context).edit().remove(PREF_WIDGET_PREFIX + widgetID);
    }

}
