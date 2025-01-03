package com.airliftcompany.alp3.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.util.HashSet;
import java.util.Set;

/* loaded from: classes.dex */
public class ALP3Preferences {
    private static final String ALP3_PREFERENCES_AUTHORIZED_KEY = "authorized";
    private static final String ALP3_PREFERENCES_BLE_FILTERING_KEY = "ble_filtering";
    private static final String ALP3_PREFERENCES_DEVICE_ADDRESS_KEY = "device_address";
    private static final String ALP3_PREFERENCES_PREVENT_SLEEP_KEY = "prevent_sleep";
    private static final String ALP3_REG_DEVICES_KEY = "reg_devices";

    public static void setDeviceAddress(String str, Context context) {
        Log.i("Set Address", str);
        SharedPreferences.Editor edit = context.getSharedPreferences(context.getPackageName(), 0).edit();
        edit.putString(ALP3_PREFERENCES_DEVICE_ADDRESS_KEY, str);
        edit.apply();
    }

    public static String deviceAddress(Context context) {
        return context.getSharedPreferences(context.getPackageName(), 0).getString(ALP3_PREFERENCES_DEVICE_ADDRESS_KEY, "");
    }

    public static void setPreventSleep(Boolean bool, Context context) {
        SharedPreferences.Editor edit = context.getSharedPreferences(context.getPackageName(), 0).edit();
        edit.putBoolean(ALP3_PREFERENCES_PREVENT_SLEEP_KEY, bool.booleanValue());
        edit.apply();
    }

    public static Boolean preventSleep(Context context) {
        return Boolean.valueOf(context.getSharedPreferences(context.getPackageName(), 0).getBoolean(ALP3_PREFERENCES_PREVENT_SLEEP_KEY, false));
    }

    public static void setBleFiltering(Boolean bool, Context context) {
        SharedPreferences.Editor edit = context.getSharedPreferences(context.getPackageName(), 0).edit();
        edit.putBoolean(ALP3_PREFERENCES_BLE_FILTERING_KEY, bool.booleanValue());
        edit.apply();
    }

    public static Boolean bleFiltering(Context context) {
        return Boolean.valueOf(context.getSharedPreferences(context.getPackageName(), 0).getBoolean(ALP3_PREFERENCES_BLE_FILTERING_KEY, true));
    }

    public static void setAuthorized(Boolean bool, Context context) {
        Log.i("Set Authorized", bool.toString());
        SharedPreferences.Editor edit = context.getSharedPreferences(context.getPackageName(), 0).edit();
        edit.putBoolean(ALP3_PREFERENCES_AUTHORIZED_KEY, bool.booleanValue());
        edit.apply();
    }

    public static Boolean authorized(Context context) {
        return Boolean.valueOf(context.getSharedPreferences(context.getPackageName(), 0).getBoolean(ALP3_PREFERENCES_AUTHORIZED_KEY, false));
    }

    public static void setRegisteredDevices(Set<String> set, Context context) {
        SharedPreferences.Editor edit = context.getSharedPreferences(context.getPackageName(), 0).edit();
        edit.remove(ALP3_REG_DEVICES_KEY);
        edit.putStringSet(ALP3_REG_DEVICES_KEY, set).apply();
    }

    public static Set<String> registeredDevices(Context context) {
        return context.getSharedPreferences(context.getPackageName(), 0).getStringSet(ALP3_REG_DEVICES_KEY, new HashSet());
    }
}
