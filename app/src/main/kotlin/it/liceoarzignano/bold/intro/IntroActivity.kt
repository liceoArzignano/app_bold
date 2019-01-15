package it.liceoarzignano.bold.intro

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.InkPageIndicator
import it.liceoarzignano.bold.BuildConfig
import it.liceoarzignano.bold.R
import it.liceoarzignano.bold.safe.mod.Encryption
import it.liceoarzignano.bold.settings.AppPrefs
import it.liceoarzignano.bold.utils.SystemUtils
import it.liceoarzignano.bold.utils.UiUtils

class IntroActivity : AppCompatActivity() {
    private lateinit var mTitle: TextView
    private lateinit var mMessage: TextView
    private lateinit var mStep1: LinearLayout
    private lateinit var mStep2: LinearLayout
    private lateinit var mImageAnimation: ImageView
    private lateinit var mRetryButton: AppCompatButton
    private lateinit var mSelectorPager: androidx.viewpager.widget.ViewPager
    private lateinit var mIndicator: InkPageIndicator

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

        mRetryButton.setOnClickListener { step1() }
        val adapter = SelectorPagerAdapter(supportFragmentManager)
        mSelectorPager.adapter = adapter
        mSelectorPager.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
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

        mImageAnimation.setImageResource(R.drawable.avd_intro_start)
        UiUtils.animateAVD(mImageAnimation.drawable)

        // Check for internet
        if (SystemUtils.hasNoInternetConnection(baseContext)) {
            mMessage.text = getString(R.string.slide0_message_failed)
            if (SystemUtils.isNotLegacy) {
                isWorking = false
                mImageAnimation.setImageResource(R.drawable.avd_intro_failed)
                UiUtils.animateAVD(mImageAnimation.drawable)
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
                    runOnUiThread { UiUtils.animateAVD(mImageAnimation.drawable) }
                    Thread.sleep(800)
                } while (isWorking)
            }

            isWorking = true
            // Let the previous animation end
            Handler().postDelayed({
                mImageAnimation.setImageResource(R.drawable.avd_intro_load)
                mAnimThread!!.start()
            }, 500)
        }

        postDeviceCheck(Encryption.validateResponse(baseContext, null, BuildConfig.DEBUG))
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
                mImageAnimation.setImageResource(R.drawable.avd_intro_done)
                UiUtils.animateAVD(mImageAnimation.drawable)

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
