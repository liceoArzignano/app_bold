package it.liceoarzignano.bold.events;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context mContext, Intent mIntent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(mIntent.getAction())) {
            Intent mService = new Intent(mContext, BootReceiverService.class);
            mContext.startService(mService);
        }
    }

}
