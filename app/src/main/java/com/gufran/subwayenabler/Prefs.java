package com.gufran.subwayenabler;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {

    private static final String NAME = "subway_enabler_prefs";

    private static SharedPreferences sp(Context c) {
        return c.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    /** Toggle state check karo. */
    public static boolean isEnabled(Context c, String key) {
        return sp(c).getBoolean(key, false);
    }

    /** Toggle state save karo. */
    public static void setEnabled(Context c, String key, boolean value) {
        sp(c).edit().putBoolean(key, value).apply();
    }

    /** Pehli baar app khula hai ya nahi. */
    public static boolean firstRunDone(Context c) {
        return sp(c).getBoolean("first_run", false);
    }

    /** First run complete mark karo. */
    public static void markFirstRun(Context c) {
        sp(c).edit().putBoolean("first_run", true).apply();
    }

    /** Sab kuch reset kar do (agar zaroorat pade). */
    public static void resetAll(Context c) {
        sp(c).edit().clear().apply();
    }
}
