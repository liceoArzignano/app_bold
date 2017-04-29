package it.liceoarzignano.bold.backup;

import android.app.Activity;

import com.google.android.gms.common.api.GoogleApiClient;

interface Backup {
    void init(Activity mActivity);
    void start();
    void stop();
    GoogleApiClient getClient();
}
