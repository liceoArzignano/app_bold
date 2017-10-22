package it.liceoarzignano.bold.safe

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Handler
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ImageView
import it.liceoarzignano.bold.R
import it.liceoarzignano.bold.utils.SystemUtils
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator

open class SecureActivity : AppCompatActivity() {
    lateinit private var mFingerprintManager: FingerprintManager
    lateinit private var mKeyguardManager: KeyguardManager
    private var mCipher: Cipher? = null
    private var mCryptoObject: FingerprintManager.CryptoObject? = null
    private val mKeyStore = KeyStore.getInstance(KEYSTORE_NAME)
    lateinit private var mGenerator: KeyGenerator
    private val hasAPI23 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (hasAPI23) {
            mFingerprintManager = getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager
            mKeyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            setupFingerprint()
        }
    }

    private fun setupFingerprint() {
        if (!hasAPI23 || !hasFingerprint) {
            return
        }

        mGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_NAME)

        if (hasEnrolled) {
            generateKey()
            if (generateCipher()) {
                mCryptoObject = FingerprintManager.CryptoObject(mCipher)
            }
        } else {
            Log.e(TAG, "No fingerprints")
        }
    }

    protected open fun onAuthSucceded() = Unit

    internal fun startListeningToFp(calback: SafeLoginDialog.Callback, icon: ImageView) =
            AuthCallback(calback, icon).start(mFingerprintManager, mCryptoObject)

    private fun generateKey() {
        mGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        mGenerator.init(KeyGenParameterSpec.Builder(KEY_NAME,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setUserAuthenticationRequired(true)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .build()
        )
        mGenerator.generateKey()
    }

    private fun generateCipher(): Boolean {
        mCipher = Cipher.getInstance("${KeyProperties.KEY_ALGORITHM_AES}/" +
                "${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}")

        try {
            mKeyStore.load(null)
            val key = mKeyStore.getKey(KEY_NAME, null)
            mCipher?.init(Cipher.ENCRYPT_MODE, key)
        } catch (e: Exception) {
            // Hack to prevent VerifyError on older devices
            if (e is KeyPermanentlyInvalidatedException) {
                return false
            }
            Log.e(TAG, e.message)
        }
        return true
    }

    private val hasFpPermission: Boolean
        get() = ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) ==
                PackageManager.PERMISSION_GRANTED

    protected val hasFingerprint: Boolean
        get() = hasAPI23 && mFingerprintManager.isHardwareDetected && hasFpPermission

    private val hasEnrolled: Boolean
        get() = mFingerprintManager.hasEnrolledFingerprints()

    internal inner class AuthCallback(private val mLoginCallback: SafeLoginDialog.Callback,
                                      private val mIcon: ImageView):
            FingerprintManager.AuthenticationCallback() {
        private var mCancellationSignal: CancellationSignal? = null

        init {
            if (hasFingerprint) {
                mIcon.visibility = View.VISIBLE
            }
        }

        fun start(manager: FingerprintManager, cryptoObject: FingerprintManager.CryptoObject?) {
            mCancellationSignal = CancellationSignal()

            if (!hasFpPermission) {
                return
            }

            manager.authenticate(cryptoObject, mCancellationSignal, 0, this, null)
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
            onFailed()
        }

        override fun onAuthenticationFailed() {
            onFailed()
        }

        override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult?) {
            recolorTo(accentColor, Runnable { mLoginCallback.dismiss() })
            onAuthSucceded()
        }

        override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence?) {
            recolorTo(defaultColor, null)
        }

        private fun onFailed() {
            recolorTo(errorColor, Runnable { recolorTo(defaultColor, null) })
        }

        private fun recolorTo(color: Int, onPostAnimation: Runnable?) {
            val oldColor = mIcon.imageTintList.defaultColor
            val anim: ValueAnimator

            if (SystemUtils.isNotLegacy) {
                anim = ValueAnimator.ofArgb(oldColor, color)
            } else {
                anim = ValueAnimator()
                anim.setIntValues(oldColor, color)
                anim.setEvaluator(ArgbEvaluator())
            }

            if (onPostAnimation != null) {
                anim.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        Handler().postDelayed(onPostAnimation, 500)
                    }
                })
            }
            anim.addUpdateListener({ value -> mIcon.setColorFilter(value.animatedValue as Int) })
            anim.setTarget(mIcon)
            anim.start()
        }

        private val defaultColor
            get() = ContextCompat.getColor(baseContext, R.color.icon)
        private val accentColor
            get() = ContextCompat.getColor(baseContext, R.color.colorAccent)
        private val errorColor
            get() = ContextCompat.getColor(baseContext, R.color.red)
    }

    companion object {
        private val TAG = "SecureActivity"
        private val KEYSTORE_NAME = "AndroidKeyStore"
        private val KEY_NAME = "bold_safe_fp_key"
    }
}