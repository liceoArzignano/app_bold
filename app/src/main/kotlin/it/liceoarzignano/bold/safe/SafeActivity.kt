package it.liceoarzignano.bold.safe

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import it.liceoarzignano.bold.BuildConfig
import it.liceoarzignano.bold.R
import it.liceoarzignano.bold.safe.mod.Encryption
import it.liceoarzignano.bold.settings.AppPrefs
import it.liceoarzignano.bold.utils.SystemUtils
import it.liceoarzignano.bold.utils.UiUtils
import java.io.UnsupportedEncodingException
import java.security.GeneralSecurityException

class SafeActivity : SecureActivity() {
    lateinit private var mLoadingLayout: LinearLayout
    lateinit private var mContentLayout: View
    lateinit private var mLoadingImage: ImageView
    lateinit private var mLoadingText: TextView
    lateinit private var mUserEdit: EditText
    lateinit private var mRegEdit: EditText
    lateinit private var mPcEdit: EditText
    lateinit private var mInternetEdit: EditText
    lateinit private var mFab: FloatingActionButton
    private var mMenu: Menu? = null
    private var mSecretKeys: Encryption.SecretKeys? = null

    lateinit private var mPrefs: AppPrefs
    private var mCrUserName: String? = null
    private var mCrReg: String? = null
    private var mCrPc: String? = null
    private var mCrInternet: String? = null
    private var isWorking = true
    lateinit private var mWorkingTask: WorkingTask<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!BuildConfig.DEBUG) {
            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE)
        }

        setContentView(R.layout.activity_safe)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back)
        toolbar.setNavigationOnClickListener { _ -> finish() }

        mPrefs = AppPrefs(baseContext)

        mLoadingLayout = findViewById(R.id.safe_loading_layout)
        mContentLayout = findViewById(R.id.safe_layout_content)
        mLoadingImage = findViewById(R.id.safe_loading_image)
        mLoadingText = findViewById(R.id.safe_loading_text)
        mUserEdit = findViewById(R.id.safe_username)
        mRegEdit = findViewById(R.id.safe_register)
        mPcEdit = findViewById(R.id.safe_pc)
        mInternetEdit = findViewById(R.id.safe_internet)
        mFab = findViewById(R.id.fab)

        mLoadingLayout.visibility = View.VISIBLE
        isWorking = true

        mFab.setOnClickListener { _ -> Handler().postDelayed({ quitActivity() }, 1000) }
        prepareDevice()
    }

    public override fun onResume() {
        super.onResume()

        // Reload everything since it everything was destroyed onPause();
        if (!isWorking) {
            showPasswordDialog()
        }
    }

    public override fun onPause() {
        if (!isWorking) {
            // Remove all the private data from memory
            mCrUserName = null
            mCrInternet = null
            mCrPc = null
            mCrReg = null
            mLoadingImage.setImageResource(R.drawable.ic_empty_safe_locked)
            mLoadingText.text = getString(R.string.safe_onpause_locked)
            mUserEdit.setText("")
            mInternetEdit.setText("")
            mPcEdit.setText("")
            mRegEdit.setText("")
            mFab.visibility = View.GONE
            mContentLayout.visibility = View.GONE
            mLoadingLayout.visibility = View.VISIBLE
            mMenu!!.findItem(R.id.action_reset).isVisible = false
            mMenu!!.findItem(R.id.action_info).isVisible = false
        }

        super.onPause()
    }

    override fun onDestroy() {
        mWorkingTask.cancel(true)
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.safe, menu)
        mMenu = menu
        mMenu?.findItem(R.id.action_info)?.isVisible = false
        mMenu?.findItem(R.id.action_reset)?.isVisible = false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_reset -> safeReset()
            R.id.action_info -> MaterialDialog.Builder(this)
                    .content(getString(R.string.safe_info_content))
                    .neutralText(getString(android.R.string.ok))
                    .show()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (isWorking) {
            super.onBackPressed()
        } else {
            MaterialDialog.Builder(this)
                    .content(R.string.safe_back_message)
                    .positiveText(R.string.actions_exit)
                    .negativeText(android.R.string.cancel)
                    .onPositive { _, _ -> finish() }
                    .show()
        }
    }

    override fun onAuthSucceded() {
        mLoadingImage.setImageResource(R.drawable.ic_empty_safe_unlocked)
        isWorking = false

        mWorkingTask = DecryptTask(mPrefs, mSecretKeys, { result ->
            if (result != null && result.size == 4) {
                mCrUserName = result[0]
                mCrReg = result[1]
                mCrPc = result[2]
                mCrInternet = result[3]
            }
            onCreateContinue()
        })
        mWorkingTask.execute()
    }

    private fun showPasswordDialog() {
        val hasCompletedSetup = mPrefs.get(AppPrefs.KEY_SAFE_SETUP, false)
        val loginDialog = SafeLoginDialog(this, !hasCompletedSetup, hasFingerprint,
                { callback, icon -> setupFingerprint(callback, icon) })
        loginDialog.build(this, { dialog -> onLoginPositive(dialog) }, { finish() })
    }

    private fun encrypt(string: String): String {
        // Don't waste time if there's nothing to do
        if (string.isBlank()) {
            return ""
        }

        return try {
            Encryption.encrypt(string, mSecretKeys).toString()
        } catch (e: UnsupportedEncodingException) {
            Log.e(TAG, e.message, e)
            ""
        } catch (e: GeneralSecurityException) {
            Log.e(TAG, e.message, e)
            ""
        }

    }

    private fun decrypt(string: String): String {
        // Don't waste time if there's nothing to do
        if (string.isBlank()) {
            return ""
        }

        return try {
            Encryption.decrypt(
                    Encryption.CipherTextIvMac(string), mSecretKeys)
        } catch (e: UnsupportedEncodingException) {
            Log.e(TAG, e.message, e)
            ""
        } catch (e: GeneralSecurityException) {
            Log.e(TAG, e.message, e)
            ""
        }
    }

    private fun setupFingerprint(callback: SafeLoginDialog.Callback, icon: ImageView) {
        if (SystemUtils.hasApi23 && hasFingerprint) {
            startListeningToFp(callback, icon)
        }
    }

    private fun onLoginPositive(loginDialog: SafeLoginDialog) {
        val hasCompletedSetup = mPrefs.get(AppPrefs.KEY_SAFE_SETUP, false)
        loginDialog.dismiss()
        mLoadingText.text = getString(R.string.safe_decrypting)
        Handler().postDelayed({
            val input = loginDialog.input
            if (hasCompletedSetup) {
                validateLogin(input)
            } else {
                mPrefs.set(AppPrefs.KEY_SAFE_SETUP, true)
                mPrefs.set(AppPrefs.KEY_SAFE_ACCESS, encrypt(input))
                onCreateContinue()
            }
        }, 240)
    }

    private fun validateLogin(input: String) {
        val isPasswordCorrect = input == decrypt(mPrefs.get(AppPrefs.KEY_SAFE_ACCESS))
        if (!isPasswordCorrect) {
            mLoadingText.text = getString(R.string.safe_nomatch)
        }

        // Do things with some delay
        Handler().postDelayed({
            if (isPasswordCorrect) {
                onAuthSucceded()
            } else {
                finish()
            }
        }, (if (isPasswordCorrect) 800 else 2000).toLong())
    }

    private fun onCreateContinue() {
        mMenu!!.findItem(R.id.action_reset).isVisible = true
        mMenu!!.findItem(R.id.action_info).isVisible = true
        mUserEdit.setText(mCrUserName)
        mRegEdit.setText(mCrReg)
        mPcEdit.setText(mCrPc)
        mInternetEdit.setText(mCrInternet)

        mLoadingLayout.animate().alpha(0f).duration = 250

        // Animations timing
        Handler().postDelayed({
            mLoadingLayout.visibility = View.GONE
            mContentLayout.visibility = View.VISIBLE
            mContentLayout.alpha = 0f
            mContentLayout.animate().alpha(1f).duration = 750
        }, 250)

        UiUtils.animFabIntro(this, mFab, getString(R.string.intro_fab_save_safe_title),
                getString(R.string.intro_fab_save_safe), AppPrefs.KEY_INTRO_SAFE)
    }

    private fun safeReset() {
        MaterialDialog.Builder(this)
                .title(getString(R.string.safe_reset_title))
                .content(getString(R.string.safe_reset_content))
                .negativeText(getString(android.R.string.no))
                .positiveText(getString(R.string.safe_reset_positive))
                .onPositive { _, _ ->
                    mPrefs.remove(AppPrefs.KEY_SAFE_ACCESS)
                    mPrefs.remove(AppPrefs.KEY_SAFE_USERNAME)
                    mPrefs.remove(AppPrefs.KEY_SAFE_REG)
                    mPrefs.remove(AppPrefs.KEY_SAFE_PC)
                    mPrefs.remove(AppPrefs.KEY_SAFE_INTERNET)
                    mPrefs.remove(AppPrefs.KEY_SAFE_SHARED)
                    mPrefs.remove(AppPrefs.KEY_SAFE_SETUP)

                    Handler().postDelayed({
                        startActivity(Intent(this, SafeActivity::class.java))
                        finish()
                    }, 700)
                }
                .show()
    }

    private fun prepareDevice() {
        // There's no need of doing a SafetyNet test now, just check app integrity
        if (mPrefs.get(AppPrefs.KEY_SAFE_PASSED) &&
                Encryption.validateResponse(this, null, BuildConfig.DEBUG) == 0) {
            mLoadingText.setText(R.string.safe_first_load)
            Handler().postDelayed({
                mWorkingTask = GenerateKeyTask({ keys ->
                    mSecretKeys = keys
                    showPasswordDialog()
                })
                mWorkingTask.execute()
            }, 100)
        } else {
            mPrefs.set(AppPrefs.KEY_SAFE_PASSED, false)
            mLoadingText.setText(R.string.safe_error_security)
        }
    }

    private fun quitActivity() {
        mMenu!!.findItem(R.id.action_reset).isVisible = false
        mMenu!!.findItem(R.id.action_info).isVisible = false
        mFab.hide()
        mContentLayout.visibility = View.GONE
        mLoadingImage.setImageResource(R.drawable.ic_empty_safe_locked)
        mLoadingText.setText(R.string.safe_encrypting)
        mLoadingLayout.visibility = View.VISIBLE

        mWorkingTask = EncryptTask(mPrefs, mSecretKeys, arrayOf(
                mUserEdit.text.toString(),
                mRegEdit.text.toString(),
                mPcEdit.text.toString(),
                mInternetEdit.text.toString()
        ), { finish() })
        mWorkingTask.execute()
    }

    private class GenerateKeyTask(private val onDone: (Encryption.SecretKeys?) -> Unit) :
            WorkingTask<Encryption.SecretKeys?>() {
        override fun doInBackground(vararg p0: Unit?): Encryption.SecretKeys? =
                Encryption.generateKey()

        override fun onPostExecute(result: Encryption.SecretKeys?) {
            if (result != null) {
                onDone(result)
            }
        }
    }

    private class DecryptTask(private val mPrefs: AppPrefs,
                              private val mSecretKeys: Encryption.SecretKeys?,
                              private val onDone: (Array<String>?) -> Unit) :
            WorkingTask<Array<String>>() {

        override fun doInBackground(vararg p0: Unit?): Array<String> =
                arrayOf(
                        decrypt(mPrefs.get(AppPrefs.KEY_SAFE_USERNAME, "")),
                        decrypt(mPrefs.get(AppPrefs.KEY_SAFE_REG, "")),
                        decrypt(mPrefs.get(AppPrefs.KEY_SAFE_PC, "")),
                        decrypt(mPrefs.get(AppPrefs.KEY_SAFE_INTERNET, "")))

        override fun onPostExecute(result: Array<String>?) {
            Handler().postDelayed({ onDone(result) }, 600)
        }

        fun decrypt(string: String): String {
            // Don't waste time if there's nothing to do
            if (string.isBlank()) {
                return ""
            }

            return try {
                Encryption.decrypt(
                        Encryption.CipherTextIvMac(string), mSecretKeys)
            } catch (e: UnsupportedEncodingException) {
                Log.e(TAG, e.message, e)
                ""
            } catch (e: GeneralSecurityException) {
                Log.e(TAG, e.message, e)
                ""
            }
        }
    }

    private class EncryptTask(private val mPrefs: AppPrefs,
                              private val mSecretKeys: Encryption.SecretKeys?,
                              private val strings: Array<String>,
                              private val onDone: () -> Unit) : WorkingTask<Unit>() {

        override fun doInBackground(vararg p0: Unit?) {
            mPrefs.set(AppPrefs.KEY_SAFE_USERNAME, encrypt(strings[0]))
            mPrefs.set(AppPrefs.KEY_SAFE_REG, encrypt(strings[1]))
            mPrefs.set(AppPrefs.KEY_SAFE_PC, encrypt(strings[2]))
            mPrefs.set(AppPrefs.KEY_SAFE_INTERNET, encrypt(strings[3]))
        }

        override fun onPostExecute(result: Unit?) = onDone()

        private fun encrypt(string: String): String {
            // Don't waste time if there's nothing to do
            if (string.isBlank()) {
                return ""
            }

            return try {
                Encryption.encrypt(string, mSecretKeys).toString()
            } catch (e: UnsupportedEncodingException) {
                Log.e(TAG, e.message, e)
                ""
            } catch (e: GeneralSecurityException) {
                Log.e(TAG, e.message, e)
                ""
            }

        }
    }


    private abstract class WorkingTask<T> : AsyncTask<Unit, Unit, T>()

    companion object {
        private val TAG = "SafeActivity"
    }
}
