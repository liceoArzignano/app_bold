package it.liceoarzignano.bold.settings;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, new MyPreferenceFragment()).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragment {
        private Context mContext;

        @Override
        public void onCreate(Bundle savedInstance) {
            super.onCreate(savedInstance);
            addPreferencesFromResource(R.xml.settings);

            mContext = getActivity();

            Preference changeLog = findPreference("changelog_key");
            final Preference analytics = findPreference("analytics_key");
            Preference backup = findPreference("backup_key");
            final Preference name = findPreference("username_key");
            final Preference secret = findPreference("secret_key");

            changeLog.setOnPreferenceClickListener(preference -> {
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

            analytics.setEnabled(!getResources().getBoolean(R.bool.force_tracker));
            if (!analytics.isEnabled()) {
                analytics.setSummary(getString(R.string.pref_metrics_summary_forced));
            }

            backup.setEnabled(GoogleApiAvailability.getInstance()
                    .isGooglePlayServicesAvailable(mContext) == ConnectionResult.SUCCESS &&
                    Utils.hasPackage(mContext, "com.google.android.apps.docs"));
            backup.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(mContext, BackupActivity.class));
                return true;
            });

            name.setSummary(Utils.userNameKey(mContext));
            name.setOnPreferenceChangeListener((preference, newValue) -> {
                name.setSummary(newValue.toString());
                return true;
            });

            secret.setOnPreferenceClickListener(preference -> {
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
                                    Intent intent = new Intent(Intent.ACTION_SEND);
                                    intent.setType("text/plain");
                                    intent.putExtra(Intent.EXTRA_TEXT, String.format(
                                            getString(R.string.pref_secret_export_message),
                                            SafeActivity.getEncryptedPassword(mContext)));
                                    startActivity(Intent.createChooser(intent,
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
