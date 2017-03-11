package it.liceoarzignano.bold.settings;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.safetynet.SafetyNet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;

import it.liceoarzignano.bold.BuildConfig;
import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.Utils;
import it.liceoarzignano.bold.backup.BackupActivity;
import it.liceoarzignano.bold.safe.SafeActivity;
import it.liceoarzignano.bold.safe.mod.Encryption;

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
    }

    public static class MyPreferenceFragment extends PreferenceFragmentCompat {
        private Context mContext;

        @Override
        public void onCreatePreferences(Bundle savedInstance, String key) {
            addPreferencesFromResource(R.xml.settings);

            mContext = getActivity();

            Preference changeLog = findPreference("changelog_key");
            final Preference analytics = findPreference("analytics_key");
            Preference backup = findPreference("backup_key");
            final Preference name = findPreference("username_key");
            final Preference safe = findPreference("safe_key");

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

            safe.setSummary(getString(Utils.hasPassedSafetyNetTest(mContext) ?
                    R.string.pref_safe_status_message_enabled :
                    R.string.pref_safe_status_message_disabled));
            safe.setOnPreferenceClickListener(preference -> {
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
                } else {
                    safetyNetTest();
                }
                return true;
            });
        }

        private void safetyNetTest() {
            if (!Utils.hasInternetConnection(mContext)) {
                Toast.makeText(mContext, getString(R.string.pref_secret_safe_test_connection),
                        Toast.LENGTH_LONG).show();
                return;
            }

            MaterialDialog dialog = new MaterialDialog.Builder(mContext)
                    .title(R.string.pref_secret_safe_test_title)
                    .content(R.string.pref_secret_safe_test_running)
                    .cancelable(false)
                    .progress(true, 100)
                    .progressIndeterminateStyle(false)
                    .build();

            dialog.show();

            // Don't run SafetyNet test on devices without GMS
            if (Utils.hasNoGMS(mContext)) {
                Utils.setSafetyNetResults(mContext, Encryption.validateRespose(mContext,
                        null, BuildConfig.DEBUG));
                return;
            }

            GoogleApiClient client = new GoogleApiClient.Builder(mContext)
                    .addApi(SafetyNet.API)
                    .build();
            client.connect();

            String nonce = String.valueOf(System.currentTimeMillis());
            ByteArrayOutputStream oStream = new ByteArrayOutputStream();
            byte[] randBytes = new byte[24];
            new SecureRandom().nextBytes(randBytes);

            try {
                oStream.write(randBytes);
                oStream.write(nonce.getBytes());
            } catch (IOException e) {
                Log.e("SafetyNetTest", e.getMessage());
            }

            SafetyNet.SafetyNetApi.attest(client, oStream.toByteArray())
                    .setResultCallback((result) -> {
                        dialog.dismiss();
                        boolean hasPassed = Encryption.validateRespose(mContext,
                                result.getJwsResult(), BuildConfig.DEBUG);
                        Utils.setSafetyNetResults(mContext, hasPassed);
                        new MaterialDialog.Builder(mContext)
                                .title(R.string.pref_secret_safe_test_title)
                                .content(hasPassed ? R.string.pref_secret_safe_test_success :
                                        R.string.pref_secret_safe_test_fail)
                                .neutralText(android.R.string.ok)
                                .show();
                    });
        }
    }
}
