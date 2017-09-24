package it.liceoarzignano.bold.intro

import android.os.Bundle
import android.os.Handler
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatButton
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.InkPageIndicator
import com.google.android.gms.safetynet.SafetyNet
import it.liceoarzignano.bold.BuildConfig
import it.liceoarzignano.bold.R
import it.liceoarzignano.bold.safe.mod.Encryption
import it.liceoarzignano.bold.settings.AppPrefs
import it.liceoarzignano.bold.utils.SystemUtils
import it.liceoarzignano.bold.utils.UiUtils
import java.io.ByteArrayOutputStream
import java.security.SecureRandom

class IntroActivity : AppCompatActivity() {
    lateinit private var mTitle: TextView
    lateinit private var mMessage: TextView
    lateinit private var mStep1: LinearLayout
    lateinit private var mStep2: LinearLayout
    lateinit private var mImageAnimation: ImageView
    lateinit private var mRetryButton: AppCompatButton
    lateinit private var mSelectorPager: ViewPager
    lateinit private var mIndicator: InkPageIndicator

    private var mAnimThread: Thread? = null
    private var isWorking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_benefits)

        mTitle = findViewById(R.id.intro_title)
        mMessage = findViewById(R.id.intro_message)
        mStep1 = findViewById(R.id.intro_step_1)
        mStep2 = findViewById(R.id.intro_step_2)
        mImageAnimation = findViewById(R.id.intro_animation)
        mRetryButton = findViewById(R.id.intro_retry_button)
        mSelectorPager = findViewById(R.id.intro_selector)
        mIndicator = findViewById(R.id.intro_indicator)

        mRetryButton.setOnClickListener({ _ -> step1() })
        val adapter = SelectorPagerAdapter(supportFragmentManager)
        mSelectorPager.adapter = adapter
        mSelectorPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) = Unit
            override fun onPageScrolled(position: Int, positionOffset: Float,
                                        positionOffsetPixels: Int) = Unit

            override fun onPageSelected(position: Int) = adapter.setSeletedFragment(position)
        })
        mIndicator.setViewPager(mSelectorPager)

        step1()
    }

    private fun step1() {
        mTitle.text = getString(R.string.slide0_title)
        mMessage.text = getString(R.string.slide0_message_loading)

        if (SystemUtils.isNotLegacy) {
            UiUtils.animateAVD(mImageAnimation, R.drawable.avd_intro_start)
        } else {
            mImageAnimation.setImageResource(R.drawable.ic_intro_hat)
        }

        // Check for internet
        if (SystemUtils.hasNoInternetConnection(baseContext)) {
            mMessage.text = getString(R.string.slide0_message_failed)
            if (SystemUtils.isNotLegacy) {
                isWorking = false
                if (SystemUtils.isNotLegacy) {
                    UiUtils.animateAVD(mImageAnimation, R.drawable.avd_intro_failed)
                }
                mRetryButton.visibility = View.VISIBLE
            }
            return
        }
        mRetryButton.visibility = View.INVISIBLE

        // Warning: contains hackery because avd animation callbacks are
        // api23+ only. Animations will be shown on api21+.
        if (SystemUtils.isNotLegacy) {
            mAnimThread = Thread {
                do { // Show the animation least once
                    runOnUiThread { UiUtils.animateAVD(mImageAnimation, R.drawable.avd_intro_load) }
                    Thread.sleep(800)
                } while (isWorking)
            }

            isWorking = true
            // Let the previous animation end
            Handler().postDelayed({ mAnimThread!!.start() }, 500)
        }

        // Don't run SafetyNet test on devices without GMS
        if (SystemUtils.hasNoGMS(baseContext)) {
            postDeviceCheck(Encryption.validateResponse(baseContext, null, BuildConfig.DEBUG))
            return
        }

        // Safety net + integrity check
        val nonce = System.currentTimeMillis().toString()
        val oStream = ByteArrayOutputStream()
        val randBytes = ByteArray(24)
        SecureRandom().nextBytes(randBytes)
        oStream.write(randBytes)
        oStream.write(nonce.toByteArray())

        SafetyNet.getClient(this)
                .attest(oStream.toByteArray(), SystemUtils.getSafetyNetApiKey(baseContext))
                .addOnCompleteListener { task ->
                    postDeviceCheck(Encryption.validateResponse(baseContext, task.result.jwsResult,
                            BuildConfig.DEBUG))
                }
    }

    private fun step2() =
            mStep1.animate()
                    .alpha(0f)
                    .withEndAction {
                        mTitle.text = getString(R.string.slide1_title)
                        mMessage.text = getString(R.string.slide1_message)
                        mStep1.visibility = View.GONE
                        mStep2.visibility = View.VISIBLE
                    }
                    .start()

    private fun postDeviceCheck(result: Int) {
        AppPrefs(baseContext).set(AppPrefs.KEY_SAFE_PASSED, result == 0)

        if (SystemUtils.isNotLegacy) {
            isWorking = false
            mAnimThread?.join()

            Handler().postDelayed({
                UiUtils.animateAVD(mImageAnimation, R.drawable.avd_intro_done)

                // Step 2 once animation ends
                Handler().postDelayed({
                    step2()
                }, 4000)
            }, 800)
        } else {
            step2()
        }
    }
}
