package it.liceoarzignano.bold.backup


import com.google.android.gms.drive.DriveId
import it.liceoarzignano.bold.utils.Time

internal class BackupData {
    var id: DriveId? = null
    var time: Time? = null
    var size: Long = 0
}
