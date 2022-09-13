package com.example.umbrellaslide.installdemo;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BroadcastReceiver extends android.content.BroadcastReceiver {
    static final String ACTION = "umbrella.HEATBEAT";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION)) {
            MainActivity.um_heatbeat_time = System.currentTimeMillis();
        }
    }

}