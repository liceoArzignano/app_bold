package it.liceoarzignano.bold.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.PreferenceFragmentCompat
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.safetynet.SafetyNet
import it.liceoarzignano.bold.BuildConfig
import it.liceoarzignano.bold.R
import it.liceoarzignano.bold.backup.BackupActivity
import it.liceoarzignano.bold.safe.mod.Encryption
import it.liceoarzignano.bold.utils.SystemUtils
import it.liceoarzignano.bold.utils.UiUtils
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.security.SecureRandom

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstance: Bundle?) {
        super.onCreate(savedInstance)
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<Toolbar>(R.id.toolbar_include)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back)
        toolbar.setNavigationOnClickListener { _ -> finish() }
    }

    class MyPreferenceFragment : PreferenceFragmentCompat() {
        private lateinit var mContext: Context
        private lateinit var mPrefs: AppPrefs

        private var bunnyCounter = 0

        override fun onCreatePreferences(savedInstance: Bundle?, key: String?) {
            addPreferencesFromResource(R.xml.settings)

            mContext = activity ?: return
            mPrefs = AppPrefs(mContext)

            val changeLog = findPreference("changelog_key")
            val backup = findPreference("backup_key")
            val name = findPreference("username_key")
            val safe = findPreference("safe_key")

            changeLog.setOnPreferenceClickListener { _ ->
                bunnyCounter++
                if (bunnyCounter == 2 && SystemUtils.isNotLegacy) {
                    bouncingBunny()
                    bunnyCounter = 0
                } else {
                    changelogDialog()
                }
                true
            }

            backup.isEnabled = GoogleApiAvailability.getInstance()
                    .isGooglePlayServicesAvailable(mContext) == ConnectionResult.SUCCESS &&
                    SystemUtils.hasGDrive(mContext)
            backup.setOnPreferenceClickListener { _ ->
                startActivity(Intent(mContext, BackupActivity::class.java))
                true
            }

            name.summary = mPrefs.get(AppPrefs.KEY_USERNAME, "")
            name.setOnPreferenceChangeListener { _, newValue ->
                name.summary = newValue.toString()
                true
            }

            safe.summary = getString(if (mPrefs.get(AppPrefs.KEY_SAFE_PASSED))
                R.string.pref_safe_status_message_enabled
            else
                R.string.pref_safe_status_message_disabled)
            safe.setOnPreferenceClickListener { _ ->
                safetyNetTest()
                true
            }
        }

        private fun safetyNetTest() {
            if (SystemUtils.hasNoInternetConnection(mContext)) {
                Toast.makeText(mContext, getString(R.string.pref_secret_safe_test_connection),
                        Toast.LENGTH_LONG).show()
                return
            }

            val dialog = MaterialDialog.Builder(mContext)
                    .content(R.string.pref_secret_safe_test_running)
                    .cancelable(false)
                    .progress(true, 100)
                    .progressIndeterminateStyle(true)
                    .build()

            dialog.show()

            // Don't run SafetyNet test on devices without GMS
            if (SystemUtils.hasNoGMS(mContext)) {
                mPrefs.set(AppPrefs.KEY_SAFE_PASSED, Encryption.validateResponse(mContext,
                        null, BuildConfig.DEBUG))
                return
            }

            val nonce = System.currentTimeMillis().toString()
            val oStream = ByteArrayOutputStream()
            val randBytes = ByteArray(24)
            SecureRandom().nextBytes(randBytes)

            try {
                oStream.write(randBytes)
                oStream.write(nonce.toByteArray())
            } catch (e: IOException) {
                Log.e("SafetyNetTest", e.message)
            }

            val ctx = context ?: return

            SafetyNet.getClient(ctx).attest(oStream.toByteArray(),
                    SystemUtils.getSafetyNetApiKey(mContext))
                    .addOnCompleteListener { task ->
                        dialog.dismiss()
                        val result = Encryption.validateResponse(mContext,
                                task.result.jwsResult, BuildConfig.DEBUG)
                        mPrefs.set(AppPrefs.KEY_SAFE_PASSED, result == 0)
                        MaterialDialog.Builder(mContext)
                                .title(R.string.pref_secret_safe_test_title)
                                .content(when (result) {
                                    1 -> R.string.pref_secret_safe_test_fail_1
                                    2 -> R.string.pref_secret_safe_test_fail_2
                                    3 -> R.string.pref_secret_safe_test_fail_3
                                    4 -> R.string.pref_secret_safe_test_fail_4
                                    5 -> R.string.pref_secret_safe_test_fail_5
                                    else -> R.string.pref_secret_safe_test_success
                                })
                                .neutralText(android.R.string.ok)
                                .show()
                    }
        }

        @RequiresApi(21)
        private fun bouncingBunny() {
            val ctx = context ?: return
            val size = UiUtils.dpToPx(resources, 256f).toInt()

            val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                    size)
            params.gravity = Gravity.CENTER_HORIZONTAL

            val layout = FrameLayout(context)
            layout.layoutParams = params
            val imageView = ImageView(context)
            imageView.layoutParams = params
            imageView.adjustViewBounds = true
            layout.addView(imageView)

            val dialog = MaterialDialog.Builder(ctx)
                    .customView(layout, false)
                    .build()

            dialog.show()
            UiUtils.animateAVD(imageView, R.drawable.avd_intro_start)
            Handler().postDelayed({
                UiUtils.animateAVD(imageView, R.drawable.avd_intro_load)
                Handler().postDelayed({
                    UiUtils.animateAVD(imageView, R.drawable.avd_intro_done)
                    Handler().postDelayed({
                        dialog.dismiss()
                    }, 3500)
                }, 1800)
            }, 800)
        }

        private fun changelogDialog() {
            MaterialDialog.Builder(mContext)
                    .title(getString(R.string.pref_changelog))
                    .content(getString(R.string.dialog_updated_content))
                    .positiveText(getString(android.R.string.ok))
                    .negativeText(R.string.dialog_updated_changelog)
                    .onNegative { dialog, _ ->
                        dialog.hide()
                        val mIntent = Intent(Intent.ACTION_VIEW)
                        mIntent.data = Uri.parse(getString(R.string.config_url_changelog))
                        startActivity(mIntent)
                    }
                    .show()
        }
    }
}
