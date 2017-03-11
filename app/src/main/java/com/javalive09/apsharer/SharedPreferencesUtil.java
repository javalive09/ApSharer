package com.javalive09.apsharer;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by peter on 2017/3/8.
 */

public class SharedPreferencesUtil {

    public static final String ACTION_AP_KEY = "action_ap_key";

    public static final String ACTION_HAVE_CLIENT = "action_arp_have_client";
    public static final String ACTION_AP_STATUS = "action_ap_status";
    public static final int ACTION_AP_OPENING = 1;
    public static final int ACTION_AP_OPENED = 2;
    public static final int ACTION_AP_CLOSED = 3;

    public static void setApStatus(Context context, int status) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(ACTION_AP_KEY, Context.MODE_PRIVATE);
        sp.edit().putInt(ACTION_AP_STATUS, status).apply();
    }

    public static int getApStatus(Context context) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(ACTION_AP_KEY, Context.MODE_PRIVATE);
        return sp.getInt(ACTION_AP_STATUS, ACTION_AP_CLOSED);
    }

    public static void addListener(Context context, SharedPreferences.OnSharedPreferenceChangeListener listener) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(ACTION_AP_KEY, Context.MODE_PRIVATE);
        sp.registerOnSharedPreferenceChangeListener(listener);
    }

    public static void removeListener(Context context, SharedPreferences.OnSharedPreferenceChangeListener listener) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(ACTION_AP_KEY, Context.MODE_PRIVATE);
        sp.unregisterOnSharedPreferenceChangeListener(listener);
    }

    public static void setHaveClient(Context context, boolean have) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(ACTION_AP_KEY, Context.MODE_PRIVATE);
        sp.edit().putBoolean(ACTION_HAVE_CLIENT, have).apply();
    }

    public static boolean haveClient(Context context) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(ACTION_AP_KEY, Context.MODE_PRIVATE);
        return sp.getBoolean(ACTION_HAVE_CLIENT, false);
    }

}
