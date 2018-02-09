package it.liceoarzignano.bold.backup

import android.app.Activity
import android.content.IntentSender
import android.os.Bundle
import android.util.Log

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.drive.Drive

import java.lang.ref.WeakReference

internal class GoogleDriveBackup : Backup, GoogleApiClient.OnConnectionFailedListener {

    override var mClient: GoogleApiClient? = null
    private var mReference: WeakReference<Activity>? = null

    override fun init(activity: Activity) {
        mReference = WeakReference(activity)
        mClient = GoogleApiClient.Builder(activity)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                    override fun onConnected(bundle: Bundle?) = Unit

                    override fun onConnectionSuspended(i: Int) = Unit
                })
                .addOnConnectionFailedListener(this)
                .build()
    }

    override fun start() {
        mClient?.connect()
    }

    override fun stop() {
        mClient?.disconnect()
    }

    override fun onConnectionFailed(result: ConnectionResult) {
        if (result.hasResolution() && mReference == null) {
            Log.e(TAG, "Unable to connect! Error code ${result.errorCode}." +
                    "Error message: ${result.errorMessage}")
            return
        }
        val activity = mReference?.get()
        // show the localized error dialog.
        try {
            result.startResolutionForResult(activity, 1)
        } catch (e: IntentSender.SendIntentException) {
            Log.e(TAG, e.message)
            GoogleApiAvailability.getInstance().getErrorDialog(activity, result.errorCode, 0).show()
        }

    }

    companion object {
        private const val TAG = "GoogleDriveBackup"
    }
}
