package it.liceoarzignano.bold.events;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import it.liceoarzignano.bold.MainActivity;

public class BootReceiverService extends Service {
    @Override
    public void onCreate() {
        MainActivity.makeEventNotification();
    }

    @Override
    public IBinder onBind(Intent mIntent) {
        return null;
    }
}
