package it.liceoarzignano.bold.settings;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.analytics.FirebaseAnalytics;

import it.liceoarzignano.bold.BoldApp;
import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.Utils;
import it.liceoarzignano.bold.backup.BackupActivity;
import it.liceoarzignano.bold.safe.SafeActivity;

public class SettingsActivity extends AppCompatActivity {
    private static int mCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_settings);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, new MyPreferenceFragment()).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragment {
        Context mContext;

        @Override
        public void onCreate(Bundle savedInstance) {
            super.onCreate(savedInstance);
            addPreferencesFromResource(R.xml.settings);

            mContext = getActivity();

            Preference mChangelog = findPreference("changelog_key");
            final Preference mAnalytics = findPreference("analytics_key");
            Preference mBackup = findPreference("backup_key");
            final Preference mName = findPreference("username_key");
            final Preference mSecret = findPreference("secret_key");

            mChangelog.setOnPreferenceClickListener(preference -> {
                new MaterialDialog.Builder(mContext)
                        .title(getString(R.string.pref_changelog))
                        .content(getString(R.string.dialog_updated_content))
                        .positiveText(getString(android.R.string.ok))
                        .negativeText(R.string.dialog_updated_changelog)
                        .onNegative((dialog, which) -> {
                            dialog.hide();
                            Intent mIntent = new Intent(Intent.ACTION_VIEW);
                            mIntent.setData(
                                    Uri.parse(getString(R.string.config_url_changelog)));
                            startActivity(mIntent);
                        })
                        .show();
                return true;
            });

            mAnalytics.setEnabled(!getResources().getBoolean(R.bool.force_tracker));
            if (!mAnalytics.isEnabled()) {
                mAnalytics.setSummary(getString(R.string.pref_metrics_summary_forced));
            }

            mBackup.setEnabled(GoogleApiAvailability.getInstance()
                    .isGooglePlayServicesAvailable(mContext) == ConnectionResult.SUCCESS &&
                    Utils.hasPackage(mContext, "com.google.android.apps.docs"));
            mBackup.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(mContext, BackupActivity.class));
                return true;
            });

            mName.setSummary(Utils.userNameKey(mContext));
            mName.setOnPreferenceChangeListener((preference, newValue) -> {
                mName.setSummary(newValue.toString());
                return true;
            });

            mSecret.setOnPreferenceClickListener(preference -> {
                mCounter++;
                if (mCounter == 9) {
                    if (SafeActivity.hasSharedPassword(mContext)) {
                        Toast.makeText(mContext, getString(
                                R.string.pref_secret_export_already_shared), Toast.LENGTH_LONG)
                                .show();
                    } else {
                        new MaterialDialog.Builder(mContext)
                                .title(getString(R.string.pref_secret_export_safe_title))
                                .content(getString(R.string.pref_secret_export_safe_message))
                                .negativeText(getString(android.R.string.cancel))
                                .positiveText(getString(R.string.viewer_share))
                                .onPositive((dialog, which) -> {
                                    Intent mIntent = new Intent(Intent.ACTION_SEND);
                                    mIntent.setType("text/plain");
                                    mIntent.putExtra(Intent.EXTRA_TEXT, String.format(
                                            getString(R.string.pref_secret_export_message),
                                            SafeActivity.getEncryptedPassword(mContext)));
                                    startActivity(Intent.createChooser(mIntent,
                                            getString(R.string.pref_secret_export_title)));
                                    SafeActivity.setSharedPassword(mContext);
                                })
                                .show();
                    }
                }
                return true;
            });
        }
    }
}
