package it.liceoarzignano.bold.backup

import android.content.Intent
import android.content.IntentSender
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatButton
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.drive.*
import com.google.android.gms.drive.query.*
import it.liceoarzignano.bold.R
import it.liceoarzignano.bold.events.EventsHandler
import it.liceoarzignano.bold.marks.MarksHandler
import it.liceoarzignano.bold.news.NewsHandler
import it.liceoarzignano.bold.settings.AppPrefs
import it.liceoarzignano.bold.utils.Time
import java.io.FileInputStream
import java.io.IOException
import java.util.*

class BackupActivity : AppCompatActivity() {

    lateinit private var mCoordinatorLayout: CoordinatorLayout
    lateinit private var mBackupButton: AppCompatButton
    lateinit private var mRestoreButton: AppCompatButton

    lateinit private var mPrefs: AppPrefs
    private var mBackup: Backup? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mIntentPicker: IntentSender? = null
    private var mBackupList: MutableList<BackupData> = arrayListOf()
    private var mBackupFolder = ""
    private var mStatus = 0
    private var hasValidFolder = false
    private var mIsEOYSession = false

    public override fun onCreate(savedInstance: Bundle?) {
        super.onCreate(savedInstance)
        setContentView(R.layout.activity_backup)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back)
        toolbar.setNavigationOnClickListener { _ -> finish() }

        mCoordinatorLayout = findViewById(R.id.coordinator_layout)
        mBackupButton = findViewById(R.id.backup_button)
        mRestoreButton = findViewById(R.id.restore_button)

        mPrefs = AppPrefs(baseContext)
        mBackupFolder = mPrefs.get(AppPrefs.KEY_BACKUP_FOLDER)

        hasValidFolder = !mBackupFolder.isEmpty()

        if (hasValidFolder) {
            initBackup()
        }

        setUI()
        mStatus = 4

        mIsEOYSession = "OK" == intent.getStringExtra(EXTRA_EOY_BACKUP)
        if (mIsEOYSession) {
            if (mBackup == null) {
                initBackup()
            }
            openFolderPicker()
            setUI()
        }
    }

    public override fun onStop() {
        if (mBackup != null) {
            mBackup!!.stop()
        }
        super.onStop()
    }

    private fun initBackup() {
        mBackup = GoogleDriveBackup()
        mBackup!!.init(this)
        mBackup!!.start()
        mGoogleApiClient = mBackup!!.mClient
    }

    private fun setUI() {
        mRestoreButton.setOnClickListener { _ ->
            pickBackup()
            setUI()
        }
        mRestoreButton.visibility = if (hasValidFolder) View.VISIBLE else View.GONE

        val backButtonText = if (hasValidFolder) {
            getBackupsFromDrive(DriveId.decodeFromString(mBackupFolder).asDriveFolder())
            R.string.backup_button_backup
        } else {
            if (mBackup == null)
                R.string.backup_button_login
            else
                R.string.backup_button_pick
        }

        mBackupButton.text = getString(backButtonText)
        mBackupButton.setOnClickListener { _ ->
            if (mBackup == null) {
                initBackup()
            }
            openFolderPicker()
            setUI()
        }
    }

    private fun pickBackup() {
        val backupTitles = mBackupList.map {
            String.format("%1\$s (%2\$s)", it.time?: "", backupSize(it.size))
        }

        MaterialDialog.Builder(this)
                .title(R.string.backup_dialog_list_title)
                .items(backupTitles)
                .itemsCallback { dialog, _, position, _ ->
                    dialog.hide()
                    restoreBackupDialog(mBackupList[position].id,
                            mBackupList[position].time!!.asString(baseContext),
                            backupSize(mBackupList[position].size))
                }
                .neutralText(android.R.string.cancel)
                .show()
    }

    private fun restoreBackupDialog(id: DriveId?, date: String, size: String) {
        MaterialDialog.Builder(this)
                .title(R.string.restore_dialog_title)
                .content(String.format(getString(R.string.restore_dialog_message), date, size))
                .positiveText(android.R.string.yes)
                .neutralText(R.string.backup_dialog_pick_another)
                .negativeText(android.R.string.no)
                .onPositive { dialog, _ ->
                    dialog.hide()
                    downloadFromDrive(id!!.asDriveFile())
                }
                .onNeutral { _, _ -> pickBackup() }
                .show()
    }

    private fun openFolderPicker() {
        if (!mBackupFolder.isEmpty()) {
            uploadToDrive(DriveId.decodeFromString(mBackupFolder))
            return
        }

        if (mGoogleApiClient == null) {
            return
        }

        try {
            if (!mGoogleApiClient!!.isConnected) {
                return
            }
            if (mIntentPicker == null) {
                mIntentPicker = Drive.DriveApi
                        .newOpenFileActivityBuilder()
                        .setMimeType(arrayOf(DriveFolder.MIME_TYPE))
                        .build(mGoogleApiClient)
            }
            startIntentSenderForResult(mIntentPicker, 2, null, 0, 0, 0)
        } catch (e: IntentSender.SendIntentException) {
            Snackbar.make(mCoordinatorLayout, getString(R.string.backup_auth_fail),
                    Snackbar.LENGTH_LONG)
        }
    }

    private fun downloadFromDrive(file: DriveFile) =
            file.open(mGoogleApiClient!!, DriveFile.MODE_READ_ONLY, null)
                    .setResultCallback { result ->
                        mStatus = 8
                        if (!result.status.isSuccess) {
                            showResult(false)
                            mStatus = 0
                            file.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null)
                        }
                        restoreBackup(result)
                    }

    private fun restoreBackup(result: DriveApi.DriveContentsResult) {
        val content = result.driveContents
        val backupFile = BackupFile(this)
        backupFile.fetch(content.inputStream)

        val progress = MaterialDialog.Builder(this)
                .content(R.string.restore_progress_message)
                .progress(true, 10)
                .progressIndeterminateStyle(true)
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .show()

        val context = applicationContext

        object : AsyncTask<Void, Void, Boolean>() {
            public override fun doInBackground(vararg params: Void): Boolean? {
                MarksHandler.getInstance(context).refillTable(backupFile.marks)
                EventsHandler.getInstance(context).refillTable(backupFile.events)
                NewsHandler.getInstance(context).refillTable(backupFile.news)
                return true
            }

            public override fun onPostExecute(result: Boolean?) {
                Handler().postDelayed({
                    progress.dismiss()
                    mStatus = 3
                    showResult(true)
                    mStatus = 0
                }, 1000)
            }
        }.execute()
    }

    private fun uploadToDrive(folderId: DriveId?) {
        if (folderId == null) {
            return
        }

        val mFolder = folderId.asDriveFolder()

        mGoogleApiClient.let {
            Drive.DriveApi.newDriveContents(mGoogleApiClient).setResultCallback { result ->
                if (!result.status.isSuccess) {
                    showResult(false)
                    Drive.DriveApi.newDriveContents(mGoogleApiClient)
                }
                uploadBackup(result, mFolder)
            }
        }
    }

    private fun uploadBackup(result: DriveApi.DriveContentsResult,
                             folder: DriveFolder) {
        val content = result.driveContents
        val backupFile = BackupFile(this)
        backupFile.createBackup(this)

        val progress = MaterialDialog.Builder(this)
                .content(R.string.backup_progress_message)
                .progress(true, 100)
                .progressIndeterminateStyle(true)
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .show()

        object : AsyncTask<Void, Boolean, Boolean>() {
            public override fun doInBackground(vararg params: Void): Boolean? {
                val oStream = content.outputStream
                val iStream: FileInputStream

                try {
                    iStream = FileInputStream(backupFile.output)
                    val buffer = ByteArray(1024)
                    var read = iStream.read(buffer)

                    while (read > 0) {
                        oStream.write(buffer, 0, read)
                        read = iStream.read(buffer)
                    }
                } catch (e: IOException) {
                    Log.e(TAG, e.message)
                }

                val changeSet = MetadataChangeSet.Builder()
                        .setTitle(BackupFile.FILE_NAME)
                        .setMimeType("text/plain")
                        .build()

                folder.createFile(mGoogleApiClient!!, changeSet, content)
                        .setResultCallback { _ -> onBackupCompleted(result.status.isSuccess) }
                mStatus = 2
                return true
            }

            public override fun onPostExecute(values: Boolean) {
                // Handle exceptions
                if (!values) {
                    progress.dismiss()
                    showResult(false)
                }
            }

            internal fun onBackupCompleted(isSuccess: Boolean) {
                // Dismiss the dialog (add some delay to make sure the user had time to read it
                Handler().postDelayed({
                    progress.dismiss()
                    showResult(isSuccess)

                    if (mIsEOYSession) {
                        endOfYearCleanup()
                    }
                }, 1000)
            }
        }.execute()
    }

    override fun onActivityResult(request: Int, result: Int, data: Intent?) {
        if (result == RESULT_OK) {
            when (request) {
                1 -> mBackup!!.start()
                2 -> {
                    mIntentPicker = null
                    val backupId = data?.getParcelableExtra<DriveId>(
                            OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID)
                    mBackupFolder = backupId?.encodeToString()!!
                    mPrefs.set(AppPrefs.KEY_BACKUP_FOLDER, mBackupFolder)
                    hasValidFolder = true
                }
                3 -> {
                    val restoreId = data?.getParcelableExtra<DriveId>(
                            OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID)
                    downloadFromDrive(restoreId?.asDriveFile()!!)
                }
                4 -> openFolderPicker()
            }
        }

        setUI()
        showResult(result == RESULT_OK)
    }

    private fun getBackupsFromDrive(folder: DriveFolder) {
        mBackupList = ArrayList()
        val order = SortOrder.Builder()
                .addSortDescending(SortableField.MODIFIED_DATE).build()
        val query = Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, BackupFile.FILE_NAME))
                .addFilter(Filters.eq(SearchableField.TRASHED, false))
                .setSortOrder(order)
                .build()
        folder.queryChildren(mGoogleApiClient!!, query)
                .setResultCallback { metadataBufferResult ->
                    val buffer = metadataBufferResult.metadataBuffer
                    val size = buffer.count
                    for (i in 0 until size) {
                        val data = BackupData()
                        val metadata = buffer.get(i)
                        data.id = metadata.driveId
                        data.time = Time(metadata.modifiedDate.time)
                        data.size = metadata.fileSize
                        mBackupList.add(data)
                    }
                }
    }

    private fun showResult(isSuccess: Boolean) {
        val message: Int = when (mStatus) {
            2 -> if (isSuccess)
                R.string.backup_created_message
            else
                R.string.backup_failed_message
            3 -> if (isSuccess)
                R.string.restore_success_message
            else
                R.string.restore_failed_message
            else -> return
        }

        Snackbar.make(mCoordinatorLayout, getString(message), Snackbar.LENGTH_LONG).show()
    }

    private fun endOfYearCleanup() {
        MaterialDialog.Builder(this)
                .title(R.string.backup_end_of_year_delete_title)
                .content(R.string.backup_end_of_year_delete_message)
                .positiveText(android.R.string.yes)
                .negativeText(android.R.string.no)
                .onPositive { _, _ -> removeAllData() }
                .show()
    }

    private fun removeAllData() {
        val progress = MaterialDialog.Builder(this)
                .content(R.string.backup_end_of_year_deleting_message)
                .progress(true, 10)
                .progressIndeterminateStyle(true)
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .show()

        val context = this

        object : AsyncTask<Void, Void, Boolean>() {
            public override fun doInBackground(vararg params: Void): Boolean? {
                MarksHandler.getInstance(context).clearTable()
                EventsHandler.getInstance(context).clearTable()
                NewsHandler.getInstance(context).clearTable()
                return true
            }

            public override fun onPostExecute(result: Boolean?) {
                Handler().postDelayed({
                    progress.dismiss()
                    MaterialDialog.Builder(context)
                            .title(R.string.backup_end_of_year_done_title)
                            .content(R.string.backup_end_of_year_done_message)
                            .neutralText(android.R.string.ok)
                            .show()
                }, 1000)
            }
        }.execute()
    }

    companion object {
        private val TAG = "BackupActivity"
        val EXTRA_EOY_BACKUP = "extraEndOfYearBackup"

        private fun backupSize(bytes: Long): String {
            val unit = 1000
            if (bytes < unit) {
                return bytes.toString() + "B"
            }
            val exp = (Math.log(bytes.toDouble()) / Math.log(unit.toDouble())).toInt()
            val prefix = "kMGTPE"[exp - 1]
            return String.format(Locale.getDefault(), "%.1f %sB",
                    bytes / Math.pow(unit.toDouble(), exp.toDouble()), prefix)
        }
    }
}
