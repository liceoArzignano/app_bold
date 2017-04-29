package it.liceoarzignano.bold.events;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import it.liceoarzignano.bold.utils.ContentUtils;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            ContentUtils.makeEventNotification(context);
        }
    }
}
