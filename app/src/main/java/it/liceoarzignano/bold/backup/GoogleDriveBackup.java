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
    private GoogleApiClient mClient;
    private WeakReference<Activity> mReference;

    @Override
    public void init(@NonNull Activity mActivity) {
        mReference = new WeakReference<>(mActivity);

        mClient = new GoogleApiClient.Builder(mActivity)
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
        if (mClient != null) {
            mClient.connect();
        } else {
            throw new IllegalStateException("You should call init before start");
        }
    }

    @Override
    public void stop() {
        if (mClient != null) {
            mClient.disconnect();
        } else {
            throw new IllegalStateException("You should call init before start");
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult mResult) {
        if (mResult.hasResolution() && mReference != null && mReference.get() != null) {
            Activity activity = mReference.get();
            // show the localized error dialog.
            try {
                mResult.startResolutionForResult(activity, 1);
            } catch (IntentSender.SendIntentException e) {
                if (android.support.compat.BuildConfig.DEBUG) {
                    Log.e("Backup", e.getMessage());
                }
                GoogleApiAvailability.getInstance()
                        .getErrorDialog(activity, mResult.getErrorCode(), 0).show();
            }
        } else {
            Log.e("GoogleDriveAPI", "Unable to connect!\nError code: " +
                    mResult.getErrorCode() + "\nError message: " + mResult.getErrorMessage());
        }
    }
}
