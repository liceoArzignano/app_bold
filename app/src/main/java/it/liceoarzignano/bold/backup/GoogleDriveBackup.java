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
    private GoogleApiClient googleApiClient;

    @Nullable
    private WeakReference<Activity> activityRef;

    @Override
    public void init(@NonNull Activity activity) {
        activityRef = new WeakReference<>(activity);

        googleApiClient = new GoogleApiClient.Builder(activity)
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
        return googleApiClient;
    }

    @Override
    public void start() {
        if (googleApiClient != null) {
            googleApiClient.connect();
        } else {
            throw new IllegalStateException("You should call init before start");
        }
    }

    @Override
    public void stop() {
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        } else {
            throw new IllegalStateException("You should call init before start");
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        if (result.hasResolution() && activityRef != null && activityRef.get() != null) {
            Activity a = activityRef.get();
            // show the localized error dialog.
            try {
                result.startResolutionForResult(a, 1);
            } catch (IntentSender.SendIntentException e) {
                if (android.support.compat.BuildConfig.DEBUG) {
                    Log.e("Backup", e.getMessage());
                }
                GoogleApiAvailability.getInstance()
                        .getErrorDialog(a, result.getErrorCode(), 0).show();
            }
        } else {
            Log.e("GoogleDriveAPI", "Unable to connect!");
            Log.e("GoogleDriveAPI", "Error code: " + result.getErrorCode());
            Log.e("GoogleDriveAPI", "Error message: " +  result.getErrorMessage());
        }
    }
}
