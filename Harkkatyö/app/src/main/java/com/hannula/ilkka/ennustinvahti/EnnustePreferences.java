package com.hannula.ilkka.ennustinvahti;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public final class EnnustePreferences {
    public static long getLastNotificationTimeInMillis(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong("viime_notifikaatio", 0);
    }

    public static long kulunutAikaViimeNotifikaatiosta(Context context) {
        long notifikaationAikaMillisekunteina = getLastNotificationTimeInMillis(context);
        return System.currentTimeMillis() - notifikaationAikaMillisekunteina;
    }

    public static void tallennaNotifikaationAika(Context context, long notifikaationAika) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong("viime_notifikaatio", notifikaationAika);
        editor.apply();
    }
}
