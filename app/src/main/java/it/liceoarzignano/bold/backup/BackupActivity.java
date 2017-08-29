package it.liceoarzignano.bold.backup;

import android.content.Context;
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.events.EventsHandler;
import it.liceoarzignano.bold.firebase.BoldAnalytics;
import it.liceoarzignano.bold.marks.MarksHandler;
import it.liceoarzignano.bold.news.NewsHandler;
import it.liceoarzignano.bold.utils.DateUtils;
import it.liceoarzignano.bold.utils.PrefsUtils;

public class BackupActivity extends AppCompatActivity {
    private static final String TAG = BackupActivity.class.getSimpleName();
    public static final String EXTRA_EOY_BACKUP = "extraEndOfYearBackup";
    private static final String BACKUP_FOLDER = "BACKUP_FOLDER";

    private CoordinatorLayout mCoordinatorLayout;
    private AppCompatButton mBackupButton;
    private AppCompatButton mRestoreButton;

    private Backup mBackup = null;
    private GoogleApiClient mGoogleApiClient;
    private IntentSender mIntentPicker;
    private SharedPreferences mPrefs;
    private List<BackupData> mBackupList;
    private String mBackupFolder;
    private int mStatus = 0;
    private boolean hasValidFolder;
    private boolean mIsEOYSession;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_backup);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(v -> finish());

        mCoordinatorLayout = findViewById(R.id.coordinator_layout);
        mBackupButton = findViewById(R.id.backup_button);
        mRestoreButton = findViewById(R.id.restore_button);

        mPrefs = getSharedPreferences(PrefsUtils.EXTRA_PREFS, MODE_PRIVATE);
        mBackupFolder = mPrefs.getString(BACKUP_FOLDER, "");

        hasValidFolder = !mBackupFolder.isEmpty();

        if (hasValidFolder) {
            initBackup();
        }

        setUI();
        mStatus = 4;

        mIsEOYSession = "OK".equals(getIntent().getStringExtra(EXTRA_EOY_BACKUP));

        if (mIsEOYSession) {
            if (mBackup == null) {
                initBackup();
            }
            openFolderPicker();
            setUI();
        }
    }

    @Override
    public void onStop() {
        if (mBackup != null) {
            mBackup.stop();
        }
        super.onStop();
    }

    private void initBackup() {
        mBackup = new GoogleDriveBackup();
        mBackup.init(this);
        mBackup.start();
        mGoogleApiClient = mBackup.getClient();
    }

    private void setUI() {
        mRestoreButton.setOnClickListener(view -> {
            new BoldAnalytics(this).log(FirebaseAnalytics.Event.SELECT_CONTENT, "Restore");
            pickBackup();
            setUI();
        });
        mRestoreButton.setVisibility(hasValidFolder ? View.VISIBLE : View.GONE);

        int backButtonText;
        if (hasValidFolder) {
            getBackupsFromDrive(DriveId.decodeFromString(mBackupFolder).asDriveFolder());
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

    private void openFolderPicker() {
        if (!mBackupFolder.isEmpty()) {
            uploadToDrive(DriveId.decodeFromString(mBackupFolder));
            return;
        }

        try {
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                if (mIntentPicker == null) {
                    mIntentPicker = Drive.DriveApi
                            .newOpenFileActivityBuilder()
                            .setMimeType(new String[]{ DriveFolder.MIME_TYPE })
                            .build(mGoogleApiClient);
                }
                startIntentSenderForResult(mIntentPicker, 2, null, 0, 0, 0);
            }
        } catch (IntentSender.SendIntentException e) {
            Snackbar.make(mCoordinatorLayout, getString(R.string.backup_auth_fail),
                    Snackbar.LENGTH_LONG);
            Log.e(TAG, e.getMessage());
        }
    }

    private void downloadFromDrive(DriveFile file) {
        file.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null)
                .setResultCallback(result -> {
                    mStatus = 8;
                    if (!result.getStatus().isSuccess()) {
                        showResult(false);
                        mStatus = 0;
                        return;
                    }
                    restoreBackup(result);
                });
    }

    private void restoreBackup(DriveApi.DriveContentsResult result) {
        DriveContents content = result.getDriveContents();
        BackupFile backupFile = new BackupFile(this);
        backupFile.fetch(content.getInputStream());

        MaterialDialog progress = new MaterialDialog.Builder(this)
                .content(R.string.restore_progress_message)
                .progress(true, 10)
                .progressIndeterminateStyle(true)
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .show();

        Context context = this;

        new AsyncTask<Void, Void, Boolean>() {
            @Override
            public Boolean doInBackground(Void... params) {
                MarksHandler.getInstance(context).refillTable(backupFile.getMarks());
                EventsHandler.getInstance(context).refillTable(backupFile.getEvents());
                NewsHandler.getInstance(context).refillTable(backupFile.getNews());
                return true;
            }

            @Override
            public void onPostExecute(Boolean result) {
                new Handler().postDelayed(() -> {
                    progress.dismiss();
                    mStatus = 3;
                    showResult(true);
                    mStatus = 0;
                }, 1000);
            }
        }.execute();
    }

    private void uploadToDrive(DriveId folderId) {
        if (folderId == null) {
            return;
        }

        DriveFolder mFolder = folderId.asDriveFolder();
        Drive.DriveApi.newDriveContents(mGoogleApiClient)
                .setResultCallback(result -> {
                    if (!result.getStatus().isSuccess()) {
                        showResult(false);
                        return;
                    }
                    uploadBackup(result, mFolder);
                });
    }

    private void uploadBackup(DriveApi.DriveContentsResult result,
                              DriveFolder folder) {
        DriveContents content = result.getDriveContents();
        BackupFile backupFile = new BackupFile(this);
        backupFile.createBackup(this);

        MaterialDialog progress = new MaterialDialog.Builder(this)
                .content(R.string.backup_progress_message)
                .progress(true, 100)
                .progressIndeterminateStyle(true)
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .show();

        new AsyncTask<Void, Boolean, Boolean>() {
            @Override
            public Boolean doInBackground(Void... params) {
                OutputStream oStream = content.getOutputStream();
                FileInputStream iStream;

                try {
                    iStream = new FileInputStream(backupFile.getOutput());
                    byte[] buffer = new byte[1024];
                    int read;

                    while ((read = iStream.read(buffer)) > 0) {
                        oStream.write(buffer, 0, read);
                    }
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }

                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setTitle(BackupFile.FILE_NAME)
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

                    if (mIsEOYSession) {
                        endOfYearCleanup();
                    }
                }, 1000);
            }
        }.execute();
    }

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

    private void getBackupsFromDrive(DriveFolder folder) {
        mBackupList = new ArrayList<>();
        SortOrder order = new SortOrder.Builder()
                .addSortDescending(SortableField.MODIFIED_DATE).build();
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, BackupFile.FILE_NAME))
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

    private static String backupSize(long bytes) {
        int unit = 1000;
        if (bytes < unit) {
            return bytes + "B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        char prefix = "kMGTPE".charAt(exp - 1);
        return String.format(Locale.getDefault(), "%.1f %sB", bytes / Math.pow(unit, exp), prefix);
    }

    private void showResult(boolean isSuccess) {
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

        Snackbar.make(mCoordinatorLayout, getString(message), Snackbar.LENGTH_LONG).show();
    }

    private void endOfYearCleanup() {
        new MaterialDialog.Builder(this)
                .title(R.string.backup_end_of_year_delete_title)
                .content(R.string.backup_end_of_year_delete_message)
                .positiveText(android.R.string.yes)
                .negativeText(android.R.string.no)
                .onPositive(((dialog, which) -> removeAllData()))
                .show();
    }

    private void removeAllData() {
        MaterialDialog progress = new MaterialDialog.Builder(this)
                .content(R.string.backup_end_of_year_deleting_message)
                .progress(true, 10)
                .progressIndeterminateStyle(true)
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .show();

        Context context = this;

        new AsyncTask<Void, Void, Boolean>() {
            @Override
            public Boolean doInBackground(Void... params) {
                MarksHandler.getInstance(context).clearTable();
                EventsHandler.getInstance(context).clearTable();
                NewsHandler.getInstance(context).clearTable();
                return true;
            }

            @Override
            public void onPostExecute(Boolean result) {
                new Handler().postDelayed(() -> {
                    progress.dismiss();
                    new MaterialDialog.Builder(context)
                            .title(R.string.backup_end_of_year_done_title)
                            .content(R.string.backup_end_of_year_done_message)
                            .neutralText(android.R.string.ok)
                            .show();
                }, 1000);
            }
        }.execute();
    }
}
