package it.liceoarzignano.bold.backup;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.drive.query.SortOrder;
import com.google.android.gms.drive.query.SortableField;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import it.liceoarzignano.bold.BoldApp;
import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.firebase.BoldAnalytics;
import it.liceoarzignano.bold.utils.DateUtils;
import it.liceoarzignano.bold.utils.PrefsUtils;

public class BackupActivity extends AppCompatActivity {
    private static final String TAG = BackupActivity.class.getSimpleName();
    private static final String BACKUP_FOLDER = "BACKUP_FOLDER";
    private static final String BACKUP_FILE_NAME = "Liceo.realm";

    private CoordinatorLayout mCoordinatorLayout;
    private TextView mSummary;
    private AppCompatButton mBackupButton;
    private AppCompatButton mRestoreButton;

    private Backup mBackup = null;
    private GoogleApiClient mGoogleApiClient;
    private IntentSender mIntentPicker;
    private Realm mRealm;
    private SharedPreferences mPrefs;
    private List<BackupData> mBackupList;
    private String mBackupFolder;
    private int mStatus = 0;
    private boolean hasValidFolder;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_backup);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(v -> finish());

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        mSummary = (TextView) findViewById(R.id.backup_summary);
        mBackupButton = (AppCompatButton) findViewById(R.id.backup_button);
        mRestoreButton = (AppCompatButton) findViewById(R.id.restore_button);

        mPrefs = getSharedPreferences(PrefsUtils.EXTRA_PREFS, MODE_PRIVATE);
        mBackupFolder = mPrefs.getString(BACKUP_FOLDER, "");

        hasValidFolder = !mBackupFolder.isEmpty();

        if (hasValidFolder) {
            initBackup();
        }

        setUI();
        mStatus = 4;
    }

    @Override
    public void onStop() {
        if (mBackup != null) {
            mBackup.stop();
        }
        super.onStop();
    }

    /**
     * Initialize backup
     */
    private void initBackup() {
        mBackup = new GoogleDriveBackup();
        mBackup.init(this);
        mBackup.start();
        mGoogleApiClient = mBackup.getClient();
        mRealm = Realm.getInstance(((BoldApp) getApplication()).getConfig());
    }

    /**
     * Set up User Interface
     */
    private void setUI() {
        if (hasValidFolder) {
            getBackupsFromDrive(DriveId.decodeFromString(mBackupFolder).asDriveFolder());
        }

        mSummary.setText(getString(R.string.backup_summary));

        mRestoreButton.setOnClickListener(view -> {
            new BoldAnalytics(this).log(FirebaseAnalytics.Event.SELECT_CONTENT, "Restore");
            pickBackup();
            setUI();
        });
        mRestoreButton.setVisibility(hasValidFolder ? View.VISIBLE : View.GONE);

        int backButtonText;
        if (hasValidFolder) {
            backButtonText = R.string.backup_button_backup;
        } else {
            backButtonText = mBackup == null ?
                    R.string.backup_button_login : R.string.backup_button_pick;
        }

        mBackupButton.setText(getString(backButtonText));
        mBackupButton.setOnClickListener(view -> {
            new BoldAnalytics(this).log(FirebaseAnalytics.Event.SELECT_CONTENT, "Backup");
            if (mBackup == null) {
                initBackup();
            }
            openFolderPicker();
            setUI();
        });
    }

    /**
     * Show a dialog with backups list and let user pick one
     */
    private void pickBackup() {
        List<String> backupTitles = new ArrayList<>();
        // Stream fails with proguard optimizations
        //noinspection Convert2streamapi
        for (BackupData data : mBackupList) {
            backupTitles.add(String.format("%1$s (%2$s)", DateUtils.dateToWordsString(this,
                    data.getDate()), backupSize(data.getSize())));
        }
        new MaterialDialog.Builder(this)
                .title(R.string.backup_dialog_list_title)
                .items(backupTitles)
                .itemsCallback((dialog, itemView, position, text) -> {
                    dialog.hide();
                    restoreBackupDialog(mBackupList.get(position).getId(),
                            DateUtils.dateToWordsString(this, mBackupList.get(position).getDate()),
                            backupSize(mBackupList.get(position).getSize()));
                })
                .neutralText(android.R.string.cancel)
                .show();
    }

    /**
     * Ask for confirmation when restoring a dialog
     *
     * @param id      backup id
     * @param date    backup date
     * @param size    backup size
     */
    private void restoreBackupDialog(final DriveId id, String date, String size) {
        new MaterialDialog.Builder(this)
                .title(R.string.restore_dialog_title)
                .content(String.format(getString(R.string.restore_dialog_message), date, size))
                .positiveText(android.R.string.yes)
                .neutralText(R.string.backup_dialog_pick_another)
                .negativeText(android.R.string.no)
                .onPositive((dialog, which) -> {
                    dialog.hide();
                    downloadFromDrive(id != null ? id.asDriveFile() : null);
                })
                .onNeutral((dialog, which) -> pickBackup())
                .show();
    }

    /**
     * Open GDrive folder picker to select the folder
     * where the mBackup file will be exported
     */
    private void openFolderPicker() {
        if (mBackupFolder.isEmpty()) {
            try {
                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    if (mIntentPicker == null) {
                        mIntentPicker = Drive.DriveApi
                                .newOpenFileActivityBuilder()
                                .setMimeType(new String[]{DriveFolder.MIME_TYPE})
                                .build(mGoogleApiClient);
                    }
                    startIntentSenderForResult(mIntentPicker, 2, null, 0, 0, 0);
                }
            } catch (IntentSender.SendIntentException e) {
                Snackbar.make(mCoordinatorLayout, getString(R.string.backup_auth_fail),
                        Snackbar.LENGTH_LONG);
                Log.e(TAG, e.getMessage());
            }
        } else {
            uploadToDrive(DriveId.decodeFromString(mBackupFolder));
        }
    }

    /**
     * Download the backup file from GDrive
     *
     * @param file selected file
     */
    private void downloadFromDrive(DriveFile file) {
        file.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null).setResultCallback(result -> {
            mStatus = 8;
            if (!result.getStatus().isSuccess()) {
                showResult(false);
                mStatus = 0;
                return;
            }
            restoreRealmBackup(result);
        });
    }

    /**
     * Restore realm backup from a
     * file downloaded from GDrive.
     * Once it's done, restart the app
     *
     * @param result GDrive content result
     */
    private void restoreRealmBackup(DriveApi.DriveContentsResult result) {
        DriveContents content = result.getDriveContents();
        InputStream iStream = content.getInputStream();

        try {
            File file = new File(mRealm.getPath());
            OutputStream oStream = new FileOutputStream(file);

            byte[] buffer = new byte[4 * 1024];
            int read;
            while ((read = iStream.read(buffer)) != -1) {
                oStream.write(buffer, 0, read);
            }
            oStream.flush();
            oStream.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        mStatus = 3;
        showResult(true);
        mStatus = 0;

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 13092,
                new Intent(this, BackupActivity.class),
                PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 10, pendingIntent);
        new Handler().postDelayed(() -> System.exit(0), 650);
    }

    /**
     * Upload mBackup file to GDrive
     *
     * @param folderId picked GDrive folder id
     */
    private void uploadToDrive(DriveId folderId) {
        if (folderId == null) {
            return;
        }
        final DriveFolder mFolder = folderId.asDriveFolder();
        Drive.DriveApi.newDriveContents(mGoogleApiClient)
                .setResultCallback(result -> {
                    if (!result.getStatus().isSuccess()) {
                        showResult(false);
                        return;
                    }
                    uploadRealmBackup(result, mFolder);
                });
    }

    /**
     * Upload mRealm database to the folder
     * selected from the user
     *
     * @param result GDrive content result
     * @param folder GDrive destination folder
     */
    private void uploadRealmBackup(final DriveApi.DriveContentsResult result,
                                   final DriveFolder folder) {
        final DriveContents content = result.getDriveContents();

        MaterialDialog progress = new MaterialDialog.Builder(this)
                .title(R.string.backup_progress_title)
                .content(R.string.backup_progress_message)
                .progress(true, 100)
                .progressIndeterminateStyle(false)
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .show();

        new AsyncTask<Void, Boolean, Boolean>() {
            @Override
            public Boolean doInBackground(Void... params) {
                OutputStream oStream = content.getOutputStream();
                FileInputStream iStream;

                try {
                    iStream = new FileInputStream(new File(mRealm.getPath()));
                    byte[] buffer = new byte[1024];
                    int read;

                    while ((read = iStream.read(buffer)) > 0) {
                        oStream.write(buffer, 0, read);
                    }
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }

                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setTitle(BACKUP_FILE_NAME)
                        .setMimeType("text/plain")
                        .build();

                folder.createFile(mGoogleApiClient, changeSet, content)
                        .setResultCallback(driveFileResult ->
                                onBackupCompleted(result.getStatus().isSuccess()));
                mStatus = 2;
                return true;
            }

            @Override
            public void onPostExecute(Boolean values) {
                // Handle exceptions
                if (!values) {
                    progress.dismiss();
                    showResult(false);
                }
            }

            void onBackupCompleted(boolean isSuccess) {
                // Dismiss the dialog (add some delay to make sure the user had time to read it
                new Handler().postDelayed(() -> {
                    progress.dismiss();
                    showResult(isSuccess);
                }, 1000);
            }
        }.execute();
    }

    /**
     * Parse results for GDrive api
     *
     * @param request api request code
     * @param result  api result code
     * @param data    data
     */
    protected void onActivityResult(int request, int result, Intent data) {
        if (result == RESULT_OK) {
            switch (request) {
                case 1:
                    mBackup.start();
                    break;
                case 2:
                    mIntentPicker = null;
                    DriveId backupId = data.getParcelableExtra(
                            OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                    mBackupFolder = backupId.encodeToString();
                    mPrefs.edit().putString(BACKUP_FOLDER, mBackupFolder).apply();
                    hasValidFolder = true;
                    break;
                case 3:
                    DriveId restoreId = data.getParcelableExtra(
                            OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                    downloadFromDrive(restoreId.asDriveFile());
                    break;
                case 4:
                    openFolderPicker();
                    break;
            }
        }

        setUI();
        showResult(result == RESULT_OK);
    }

    /**
     * Fetch list of backups from Google Drive
     *
     * @param folder backups location
     */
    private void getBackupsFromDrive(DriveFolder folder) {
        mBackupList = new ArrayList<>();
        SortOrder order = new SortOrder.Builder()
                .addSortDescending(SortableField.MODIFIED_DATE).build();
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, BACKUP_FILE_NAME))
                .addFilter(Filters.eq(SearchableField.TRASHED, false))
                .setSortOrder(order)
                .build();
        folder.queryChildren(mGoogleApiClient, query)
                .setResultCallback(metadataBufferResult -> {
                    MetadataBuffer buffer = metadataBufferResult.getMetadataBuffer();
                    int size = buffer.getCount();
                    for (int i = 0; i < size; i++) {
                        BackupData data = new BackupData();
                        Metadata metadata = buffer.get(i);
                        data.setId(metadata.getDriveId());
                        data.setDate(metadata.getModifiedDate());
                        data.setSize(metadata.getFileSize());
                        mBackupList.add(data);
                    }
                });
    }

    /**
     * Convert long bytes format to a human-friendly format (eg: 12kb)
     *
     * @param bytes bytes size
     * @return file size: sth{kMGTPE}b
     */
    private static String backupSize(long bytes) {
        int unit = 1000;
        if (bytes < unit) {
            return bytes + "B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        char prefix = "kMGTPE".charAt(exp - 1);
        return String.format(Locale.getDefault(), "%.1f %sB", bytes / Math.pow(unit, exp), prefix);
    }

    /**
     * Inform the user with a SnackBar that the action has successfully been completed
     *
     * @param isSuccess success or failure
     */
    private void showResult(final boolean isSuccess) {
        int message;
        switch (mStatus) {
            case 2:
                message = isSuccess ?
                        R.string.backup_created_message : R.string.backup_failed_message;
                break;
            case 3:
                message = isSuccess ?
                        R.string.restore_success_message : R.string.restore_failed_message;
                break;
            default:
                return;
        }

        Snackbar.make(mCoordinatorLayout, getString(message),
                Snackbar.LENGTH_LONG).show();
    }
}
