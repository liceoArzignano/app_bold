package it.liceoarzignano.bold.backup;

import android.app.Activity;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

import java.lang.ref.WeakReference;

class GoogleDriveBackup implements Backup, GoogleApiClient.OnConnectionFailedListener {
    @Nullable
    private GoogleApiClient mClient;

    @Nullable
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
            Activity mActivity = mReference.get();
            // show the localized error dialog.
            try {
                mResult.startResolutionForResult(mActivity, 1);
            } catch (IntentSender.SendIntentException e) {
                if (android.support.compat.BuildConfig.DEBUG) {
                    Log.e("Backup", e.getMessage());
                }
                GoogleApiAvailability.getInstance()
                        .getErrorDialog(mActivity, mResult.getErrorCode(), 0).show();
            }
        } else {
            Log.e("GoogleDriveAPI", "Unable to connect!");
            Log.e("GoogleDriveAPI", "Error code: " + mResult.getErrorCode());
            Log.e("GoogleDriveAPI", "Error message: " +  mResult.getErrorMessage());
        }
    }
}
