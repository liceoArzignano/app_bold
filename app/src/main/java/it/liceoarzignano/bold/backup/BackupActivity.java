package it.liceoarzignano.bold.backup;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import it.liceoarzignano.bold.BuildConfig;
import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.Utils;
import it.liceoarzignano.bold.external.expandableheightlistview.ExpandableHeightListView;
import it.liceoarzignano.bold.realm.RealmController;

public class BackupActivity extends AppCompatActivity {

    private Backup backup;
    private GoogleApiClient mGoogleApiClient;
    private IntentSender mIntentPicker;
    private Realm realm;
    private CoordinatorLayout mCoordinatorLayout;
    private ExpandableHeightListView mListView;
    private SharedPreferences prefs;

    private String backupFolder;
    private int status = 0;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        onBackupActivityCreate();
    }

    @Override
    public void onStop() {
        if (backup != null) {
            backup.stop();
        }
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.backup, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_info:
                new MaterialDialog.Builder(this)
                        .title(getString(R.string.backup_explain_title))
                        .content(getString(R.string.backup_explain_message))
                        .neutralText(getString(android.R.string.ok))
                        .show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Setup activity UI
     */
    private void onBackupActivityCreate() {
        setContentView(R.layout.activity_backup);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        prefs = getSharedPreferences("HomePrefs", MODE_PRIVATE);

        backupFolder = prefs.getString("BACKUP_FOLDER", "");

        backup = new GoogleDriveBackup();
        backup.init(this);
        backup.start();
        mGoogleApiClient = backup.getClient();

        realm = RealmController.with(this).getRealm();

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        mListView = (ExpandableHeightListView) findViewById(R.id.backupsListView);

        FloatingActionButton mBackupFab = (FloatingActionButton) findViewById(R.id.fab);
        mBackupFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                status = 1;
                if (!BuildConfig.DEBUG) {
                    openFolderPicker();
                } else {
                    Snackbar.make(mCoordinatorLayout, getString(R.string.backup_error_debug),
                            Snackbar.LENGTH_LONG).show();
                }
                status = 0;
            }
        });

        Utils.animFabIntro(this, mBackupFab, getString(R.string.intro_fab_backup_title),
                getString(R.string.intro_fab_backup), "backupFabIntro");

        if (!backupFolder.equals("")) {
            getBackupsFromDrive(DriveId.decodeFromString(backupFolder).asDriveFolder());
        }
    }

    /**
     * Open GDrive folder picker to select the folder
     * where the backup file will be exported
     */
    private void openFolderPicker() {
        if (backupFolder.equals("")) {
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
                showErrorDialog();
                e.printStackTrace();
            }
        } else {
            uploadToDrive(DriveId.decodeFromString(backupFolder));
        }
    }

    /**
     * Download the backup file from GDrive
     *
     * @param file selected file
     */
    void downloadFromDrive(DriveFile file) {
        file.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null)
                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(@NonNull DriveApi.DriveContentsResult result) {
                        status = 2;
                        if (!result.getStatus().isSuccess()) {
                            showErrorDialog();
                            status = 0;
                            return;
                        }
                        restoreRealmBackup(result);
                    }
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
        DriveContents contents = result.getDriveContents();
        InputStream inputStream = contents.getInputStream();

        try {
            File mFile = new File(realm.getPath());
            OutputStream outputStream = new FileOutputStream(mFile);

            byte[] buffer = new byte[4 * 1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        showSuccessDialog();
        status = 0;

        Intent mActivity = new Intent(getApplicationContext(), BackupActivity.class);
        PendingIntent mPendingIntent =
                PendingIntent.getActivity(getApplicationContext(), 13092, mActivity,
                        PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager)
                getApplicationContext().getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 10, mPendingIntent);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                System.exit(0);
            }
        }, 650);
    }

    /**
     * Upload backup file to GDrive
     *
     * @param mFolderId picked GDrive folder id
     */
    private void uploadToDrive(DriveId mFolderId) {
        if (mFolderId != null) {
            final DriveFolder folder = mFolderId.asDriveFolder();
            Drive.DriveApi.newDriveContents(mGoogleApiClient)
                    .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                        @Override
                        public void onResult(@NonNull DriveApi.DriveContentsResult result) {
                            if (!result.getStatus().isSuccess()) {
                                showErrorDialog();
                                return;
                            }
                            uploadRealmBackup(result, folder);
                        }
                    });
        }
    }

    /**
     * Upload realm database to the folder
     * selected from the user
     *
     * @param result GDrive content result
     * @param folder GDrive destination folder
     */
    private void uploadRealmBackup(final DriveApi.DriveContentsResult result,
                                   final DriveFolder folder) {
        final DriveContents contents = result.getDriveContents();

        new Thread() {
            @Override
            public void run() {
                OutputStream outputStream = contents.getOutputStream();

                FileInputStream inputStream;
                try {
                    inputStream = new FileInputStream(new File(realm.getPath()));
                    byte[] buffer = new byte[1024];
                    int read;

                    while ((read = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, read);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setTitle("Liceo.realm")
                        .setMimeType("text/plain")
                        .build();

                folder.createFile(mGoogleApiClient, changeSet, contents)
                        .setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                            @Override
                            public void onResult(
                                    @NonNull DriveFolder.DriveFileResult driveFileResult) {
                                if (!result.getStatus().isSuccess()) {
                                    showErrorDialog();
                                } else {
                                    showSuccessDialog();
                                }
                            }
                        });
            }
        }.start();
    }

    /**
     * Parse results for GDrive api
     *
     * @param requestCode api request code
     * @param resultCode  api result code
     * @param data        data
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    backup.start();
                } else {
                    showErrorDialog();
                }
                break;
            case 2:
                mIntentPicker = null;
                if (resultCode == RESULT_OK) {
                    DriveId mFolderDriveId = data.getParcelableExtra(
                            OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                    uploadToDrive(mFolderDriveId);
                    prefs.edit().putString("BACKUP_FOLDER",
                            mFolderDriveId.encodeToString()).apply();
                } else {
                    showErrorDialog();
                }
                break;
            case 3:
                if (resultCode == RESULT_OK) {
                    DriveId driveId = data.getParcelableExtra(
                            OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                    downloadFromDrive(driveId.asDriveFile());
                } else {
                    showErrorDialog();
                }
                finish();
                break;
        }
    }

    /**
     * Backup completed successfully, inform the user with a SnackBar
     */
    private void showSuccessDialog() {
        Snackbar.make(mCoordinatorLayout, getString(status == 2 ? R.string.restore_success_message :
                R.string.backup_created_message), Snackbar.LENGTH_LONG).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Refresh UI
                onBackupActivityCreate();
            }
        }, 650);

    }

    /**
     * Backup failed, inform the user with a SnackBar (suggest to retry with action on it)
     */
    private void showErrorDialog() {
        Snackbar.make(mCoordinatorLayout, getString(status == 2 ? R.string.restore_failed_message :
                R.string.backup_failed_message), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.backup_retry), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openFolderPicker();
                    }
                }).show();
    }

    /**
     * Fetch list of backups from Google Drive
     *
     * @param folder backups location
     */
    private void getBackupsFromDrive(DriveFolder folder) {
        final Activity activity = this;
        final List<BackupData> backupList = new ArrayList<>();
        SortOrder order = new SortOrder.Builder()
                .addSortDescending(SortableField.MODIFIED_DATE).build();
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, "Liceo.realm"))
                .addFilter(Filters.eq(SearchableField.TRASHED, false))
                .setSortOrder(order)
                .build();
        folder.queryChildren(mGoogleApiClient, query)
                .setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
                    @Override
                    public void onResult(
                            @NonNull DriveApi.MetadataBufferResult metadataBufferResult) {
                        MetadataBuffer buffer = metadataBufferResult.getMetadataBuffer();
                        int size = buffer.getCount();
                        for (int i = 0; i < size; i++) {
                            BackupData data = new BackupData();
                            Metadata metadata = buffer.get(i);
                            data.setId(metadata.getDriveId());
                            data.setDate(metadata.getModifiedDate());
                            data.setSize(metadata.getFileSize());
                            backupList.add(data);
                            mListView.setAdapter(new BackupListAdapter(activity,
                                    backupList));
                        }
                    }
                });
    }

}
