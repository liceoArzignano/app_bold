package it.liceoarzignano.bold.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.PreferenceFragmentCompat
import com.afollestad.materialdialogs.MaterialDialog
import it.liceoarzignano.bold.R

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstance: Bundle?) {
        super.onCreate(savedInstance)
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<Toolbar>(R.id.toolbar_include)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back)
        toolbar.setNavigationOnClickListener { finish() }
    }

    class MyPreferenceFragment : PreferenceFragmentCompat() {
        private lateinit var mContext: Context
        private lateinit var mPrefs: AppPrefs

        override fun onCreatePreferences(savedInstance: Bundle?, key: String?) {
            addPreferencesFromResource(R.xml.settings)

            mContext = activity ?: return
            mPrefs = AppPrefs(mContext)

            val changeLog = findPreference("changelog_key")
            val name = findPreference("username_key")

            changeLog.setOnPreferenceClickListener {
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
                true
            }

            name.summary = mPrefs.get(AppPrefs.KEY_USERNAME, "")
            name.setOnPreferenceChangeListener { _, newValue ->
                name.summary = newValue.toString()
                true
            }
        }
    }
}
