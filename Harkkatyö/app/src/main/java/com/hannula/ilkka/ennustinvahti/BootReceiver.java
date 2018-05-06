package com.hannula.ilkka.ennustinvahti;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        //laitetaan homma käyntiin laitteen herätessä.
        Intent i = new Intent(context, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra("methodName","bootingStart");
        context.startActivity(i);

    }
}