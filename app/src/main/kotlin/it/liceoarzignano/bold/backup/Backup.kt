package it.liceoarzignano.bold.backup

import android.app.Activity

import com.google.android.gms.common.api.GoogleApiClient

internal interface Backup {
    fun init(activity: Activity)
    fun start()
    fun stop()
    var mClient: GoogleApiClient?
}
