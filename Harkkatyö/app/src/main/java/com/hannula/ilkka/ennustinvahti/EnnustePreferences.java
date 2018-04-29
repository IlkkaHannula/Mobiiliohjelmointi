package com.hannula.ilkka.ennustinvahti;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public final class EnnustePreferences {

    public static long kulunutAikaViimeNotifikaatiosta(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return System.currentTimeMillis() - sp.getLong("viime_notifikaatio", 0);
    }

    public static void tallennaNotifikaationAika(Context context, long notifikaationAika) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong("viime_notifikaatio", notifikaationAika);
        editor.apply();
    }

    public static float haeEnnusteRaja(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getFloat("ennuste_raja", Float.parseFloat("1.6"));
    }

    public static void tallennaEnnusteRaja(Context context, float raja) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putFloat("ennuste_raja", raja);
        editor.apply();
    }
}
