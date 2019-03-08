package com.advantech.edgexdeployclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Objects;


public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), "android.intent.action.BOOT_COMPLETED"))
        {
            Intent mIntent = new Intent(context, DeployClient.class);
            mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            context.startService(mIntent);
        }
    }
}
