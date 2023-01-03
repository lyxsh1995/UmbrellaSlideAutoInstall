package com.example.umbrellaslide.installdemo;

import android.content.Context;
import android.content.Intent;

import com.elclcd.systempal.core.SysManagerImpl;
import com.example.umbrellaslide.installdemo.getui.VeDate;

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
                addLog.addlog("自动升级程序", "接收到重启广播");
                try {
                    MainActivity.RebootSystem(context);
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            case INSTALL:
                addLog.addlog("自动升级程序", "接收到升级广播");
                MainActivity.jdinstall(context);
                break;
        }
    }

}