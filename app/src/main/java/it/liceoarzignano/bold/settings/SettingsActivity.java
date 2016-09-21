package it.liceoarzignano.bold.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.analytics.FirebaseAnalytics;

import it.liceoarzignano.bold.BoldApp;
import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.Utils;
import it.liceoarzignano.bold.backup.BackupActivity;
import it.liceoarzignano.bold.safe.SafeActivity;

public class SettingsActivity extends AppCompatActivity {
    private static int counter = 0;

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
        Context context;

        @Override
        public void onCreate(Bundle savedInstance) {
            super.onCreate(savedInstance);
            addPreferencesFromResource(R.xml.settings);

            context = getActivity();

            Preference changelogPref = findPreference("changelog_key");
            final Preference trackerPref = findPreference("analytics_key");
            Preference backupPref = findPreference("backup_key");
            final Preference namePref = findPreference("username_key");
            final Preference secretPref = findPreference("secret_key");

            changelogPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new MaterialDialog.Builder(context)
                            .title(getString(R.string.pref_changelog))
                            .content(getString(R.string.dialog_updated_content))
                            .positiveText(getString(android.R.string.ok))
                            .show();
                    return true;
                }
            });

            trackerPref.setEnabled(!getResources().getBoolean(R.bool.force_tracker));
            if (!trackerPref.isEnabled()) {
                trackerPref.setSummary(getString(R.string.pref_metrics_summary_forced));
            }

            backupPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(context, BackupActivity.class);
                    startActivity(intent);
                    return true;
                }
            });

            namePref.setSummary(Utils.userNameKey(context));
            namePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    namePref.setSummary(newValue.toString());
                    return true;
                }
            });

            secretPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    counter++;
                    if (counter == 9) {
                        if (SafeActivity.hasSharedPassword(context)) {
                            Toast.makeText(context, getString(
                                    R.string.pref_secret_export_already_shared), Toast.LENGTH_LONG)
                                    .show();
                        } else {
                            new MaterialDialog.Builder(context)
                                    .title(getString(R.string.pref_secret_export_safe_title))
                                    .content(getString(R.string.pref_secret_export_safe_message))
                                    .negativeText(getString(android.R.string.cancel))
                                    .positiveText(getString(R.string.viewer_share))
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog,
                                                            @NonNull DialogAction which) {
                                            final Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                            shareIntent.setType("text/plain");
                                            shareIntent.putExtra(Intent.EXTRA_TEXT, String.format(
                                                    getString(R.string.pref_secret_export_message),
                                                    SafeActivity.getEncryptedPassword(context)));
                                            startActivity(Intent.createChooser(shareIntent,
                                                    getString(R.string.pref_secret_export_title)));
                                            SafeActivity.setSharedPassword(context);
                                        }
                                    })
                                    .show();
                        }

                    }
                    return true;
                }
            });

            if (Utils.hasAnalytics(context)) {
                Bundle mBundle = new Bundle();
                mBundle.putString(FirebaseAnalytics.Param.LEVEL, "Settings");
                BoldApp.getBoldAnalytics().sendConfig(mBundle);
            }
        }
    }
}
