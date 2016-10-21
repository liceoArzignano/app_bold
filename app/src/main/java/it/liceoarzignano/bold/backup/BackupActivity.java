package it.liceoarzignano.bold.backup;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.realm.RealmController;

public class BackupActivity extends AppCompatActivity {
    private static final String PREFERENCES = "HomePrefs";
    private static final String BACKUP_FOLDER = "BACKUP_FOLDER";

    private Backup mBackup = null;
    private GoogleApiClient mGoogleApiClient;
    private IntentSender mIntentPicker;
    private Realm mRealm;
    private SharedPreferences mPrefs;
    private List<BackupData> mBackupList;

    private CoordinatorLayout mCoordinatorLayout;
    private TextView mSummary;
    private AppCompatButton mBackupButton;
    private AppCompatButton mRestoreButton;

    private String mBackupFolder;
    private int mStatus = 0;
    private boolean hasValidFolder;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_backup);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        mSummary = (TextView) findViewById(R.id.backup_summary);
        mBackupButton = (AppCompatButton) findViewById(R.id.backup_button);
        mRestoreButton = (AppCompatButton) findViewById(R.id.restore_button);

        mPrefs = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
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
        mRealm = RealmController.with(this).getRealm();
    }

    /**
     * Set up User Interface
     */
    private void setUI() {
        if (hasValidFolder) {
            getBackupsFromDrive(DriveId.decodeFromString(mBackupFolder).asDriveFolder());
        }

        mSummary.setText(getString(R.string.backup_summary));

        final Context mContext = this;
        mRestoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickBackup(mContext);
                setUI();
            }
        });
        mRestoreButton.setVisibility(hasValidFolder ? View.VISIBLE : View.GONE);

        int mBackButtonText;
        if (hasValidFolder) {
            mBackButtonText = R.string.backup_button_backup;
        } else {
            mBackButtonText = mBackup == null ?
                    R.string.backup_button_login : R.string.backup_button_pick;
        }

        mBackupButton.setText(getString(mBackButtonText));
        mBackupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBackup == null) {
                    initBackup();
                }
                openFolderPicker();
                setUI();
            }
        });
    }

    /**
     * Show a dialog with backups list and let user pick one
     *
     * @param mContext used to show the dialog
     */
    private void pickBackup(final Context mContext) {
        List<String> mBackupsTitles = new ArrayList<>();
        for (BackupData mData : mBackupList) {
            mBackupsTitles.add(String.format("%1$s (%2$s)", backupDate(mData.getDate()),
                    backupSize(mData.getSize())));
        }

        new MaterialDialog.Builder(mContext)
                .title(R.string.backup_dialog_list_title)
                .items(mBackupsTitles)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView,
                                            int position, CharSequence text) {
                        dialog.hide();
                        restoreBackupDialog(mContext, mBackupList.get(position).getId(),
                                backupDate(mBackupList.get(position).getDate()),
                                backupSize(mBackupList.get(position).getSize()));
                    }
                })
                .neutralText(android.R.string.cancel)
                .show();
    }

    /**
     * Ask for confirmation when restoring a dialog
     *
     * @param mContext used to show the dialog
     * @param mId      backup id
     */
    private void restoreBackupDialog(final Context mContext, final DriveId mId,
                                     String mDate, String mSize) {
        new MaterialDialog.Builder(mContext)
                .title(R.string.restore_dialog_title)
                .content(String.format(
                        getString(R.string.restore_dialog_message), mDate, mSize))
                .positiveText(android.R.string.yes)
                .neutralText(R.string.backup_dialog_pick_another)
                .negativeText(android.R.string.no)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog,
                                        @NonNull DialogAction which) {
                        dialog.hide();
                        ((BackupActivity) mContext).downloadFromDrive(mId != null ?
                                mId.asDriveFile() : null);
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog,
                                        @NonNull DialogAction which) {
                        pickBackup(mContext);
                    }
                })
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
                if (android.support.compat.BuildConfig.DEBUG) {
                    Log.e("Backup", e.getMessage());
                }
            }
        } else {
            uploadToDrive(DriveId.decodeFromString(mBackupFolder));
        }
    }

    /**
     * Download the mBackup file from GDrive
     *
     * @param file selected file
     */
    private void downloadFromDrive(DriveFile file) {
        file.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null)
                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(@NonNull DriveApi.DriveContentsResult result) {
                        mStatus = 8;
                        if (!result.getStatus().isSuccess()) {
                            showResult(false);
                            mStatus = 0;
                            return;
                        }
                        restoreRealmBackup(result);
                    }
                });
    }

    /**
     * Restore mRealm mBackup from a
     * file downloaded from GDrive.
     * Once it's done, restart the app
     *
     * @param result GDrive content result
     */
    private void restoreRealmBackup(DriveApi.DriveContentsResult result) {
        DriveContents contents = result.getDriveContents();
        InputStream inputStream = contents.getInputStream();

        try {
            File mFile = new File(mRealm.getPath());
            OutputStream outputStream = new FileOutputStream(mFile);

            byte[] buffer = new byte[4 * 1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            if (android.support.compat.BuildConfig.DEBUG) {
                Log.e("Backup", e.getMessage());
            }
        }

        mStatus = 3;
        showResult(true);
        mStatus = 0;

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
     * Upload mBackup file to GDrive
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
                                showResult(false);
                                return;
                            }
                            uploadRealmBackup(result, folder);
                        }
                    });
        }
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
        final DriveContents contents = result.getDriveContents();

        new Thread() {
            @Override
            public void run() {
                OutputStream outputStream = contents.getOutputStream();

                FileInputStream inputStream;
                try {
                    inputStream = new FileInputStream(new File(mRealm.getPath()));
                    byte[] buffer = new byte[1024];
                    int read;

                    while ((read = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, read);
                    }
                } catch (IOException e) {
                    if (android.support.compat.BuildConfig.DEBUG) {
                        Log.e("Backup", e.getMessage());
                    }
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
                                showResult(result.getStatus().isSuccess());
                            }
                        });
                mStatus = 2;
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
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1:
                    mBackup.start();
                    break;
                case 2:
                    mIntentPicker = null;
                    DriveId mFolderDriveId = data.getParcelableExtra(
                            OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                    mBackupFolder = mFolderDriveId.encodeToString();
                    mPrefs.edit().putString(BACKUP_FOLDER, mBackupFolder).apply();
                    hasValidFolder = true;
                    break;
                case 3:
                    DriveId driveId = data.getParcelableExtra(
                            OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                    downloadFromDrive(driveId.asDriveFile());
                    break;
                case 4:
                    openFolderPicker();
                    break;
            }
        }

        setUI();
        showResult(resultCode == RESULT_OK);
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
                            mBackupList.add(data);
                        }
                    }
                });
    }

    /**
     * Convert long bytes format to a human-friendly format (eg: 12kb)
     *
     * @param mBytes bytes size
     * @return file size: sth{kMGTPE}b
     */
    private static String backupSize(long mBytes) {
        int unit = 1000;
        if (mBytes < unit) {
            return mBytes + " B";
        }
        int exp = (int) (Math.log(mBytes) / Math.log(unit));
        char pre = "kMGTPE".charAt(exp - 1);
        return String.format(Locale.ITALIAN, "%.1f %sB", mBytes / Math.pow(unit, exp), pre);
    }

    /**
     * Convert backup date to human-friendly string
     *
     * @param mDate: backup date
     * @return backup creation date
     */
    private String backupDate(Date mDate) {
        Calendar mCal = Calendar.getInstance();
        mCal.setTime(mDate);
        @SuppressWarnings("deprecation") String mDateStr = new SimpleDateFormat(getString(R.string.date_formatting),
                getResources().getConfiguration().locale).format(mCal.getTime());

        int mPosition = 0;
        boolean mWorking = true;
        while (mWorking) {
            if (Character.isDigit(mDateStr.charAt(mPosition))) {
                mPosition++;
            } else {
                mWorking = false;
            }
        }

        return mDateStr.substring(0, mPosition) +
                String.valueOf(mDateStr.charAt(mPosition)).toUpperCase() +
                mDateStr.substring(mPosition + 1, mDateStr.length());
    }

    /**
     * Inform the user with a SnackBar that the action has successfully been completed
     *
     * @param isSuccess success or failure
     */
    private void showResult(final boolean isSuccess) {
        int mMessage = 0;
        switch (mStatus) {
            case 2:
                mMessage = isSuccess ?
                        R.string.backup_created_message : R.string.backup_failed_message;
                break;
            case 3:
                mMessage = isSuccess ?
                        R.string.restore_success_message : R.string.restore_failed_message;
                break;
        }

        if (mMessage == 0) {
            return;
        }

        Snackbar mSnack = Snackbar.make(mCoordinatorLayout, getString(mMessage),
                Snackbar.LENGTH_LONG);
        mSnack.show();
    }
}
