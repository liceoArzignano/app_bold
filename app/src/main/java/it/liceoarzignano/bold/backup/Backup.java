package it.liceoarzignano.bold.backup;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.google.android.gms.common.api.GoogleApiClient;

interface Backup {
    void init(@NonNull Activity activity);

    void start();

    void stop();

    GoogleApiClient getClient();
}
