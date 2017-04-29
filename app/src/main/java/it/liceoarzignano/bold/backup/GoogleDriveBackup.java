package it.liceoarzignano.bold.backup;

import android.app.Activity;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

import java.lang.ref.WeakReference;

class GoogleDriveBackup implements Backup, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = GoogleDriveBackup.class.getSimpleName();

    private GoogleApiClient mClient;
    private WeakReference<Activity> mReference;

    @Override
    public void init(@NonNull Activity activity) {
        mReference = new WeakReference<>(activity);
        mClient = new GoogleApiClient.Builder(activity)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                    }
                })
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public GoogleApiClient getClient() {
        return mClient;
    }

    @Override
    public void start() {
        if (mClient == null) {
            throw new IllegalStateException("You should call init before start");
        } else {
            mClient.connect();
        }
    }

    @Override
    public void stop() {
        if (mClient == null) {
            throw new IllegalStateException("You should call init before start");
        } else {
            mClient.disconnect();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        if (result.hasResolution() && mReference == null || mReference.get() == null) {
            Log.e(TAG, String.format("Unable to connect!\nError code: %1$s\nError message: %2$s",
                    result.getErrorCode(), result.getErrorMessage()));
            return;
        }
        Activity activity = mReference.get();
        // show the localized error dialog.
        try {
            result.startResolutionForResult(activity, 1);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, e.getMessage());
            GoogleApiAvailability.getInstance()
                    .getErrorDialog(activity, result.getErrorCode(), 0).show();
        }
    }
}
