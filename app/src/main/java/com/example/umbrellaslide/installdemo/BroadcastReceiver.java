package com.example.umbrellaslide.installdemo;

import android.content.Context;
import android.content.Intent;

import com.elclcd.systempal.core.SysManagerImpl;

public class BroadcastReceiver extends android.content.BroadcastReceiver {
    static final String HEATBEAT = "umbrella.HEATBEAT";
    static final String REBOOT = "umbrella.REBOOT";
    static final String INSTALL = "umbrella.INSTALL";

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()){
            case HEATBEAT:
                MainActivity.um_heatbeat_time = System.currentTimeMillis();
                break;
            case REBOOT:
                if (SysManagerImpl.isInitialized()) {
                    SysManagerImpl.getInstance().reboot();
                }
                Intent mintent = new Intent("com.android.action.reboot");
                context.sendBroadcast(mintent);
                break;
            case INSTALL:
                MainActivity.jdinstall(context);
                break;
        }
    }

}