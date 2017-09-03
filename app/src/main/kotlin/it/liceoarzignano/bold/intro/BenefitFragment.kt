package it.liceoarzignano.bold.intro

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.widget.AppCompatButton
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.gms.safetynet.SafetyNet
import it.liceoarzignano.bold.BuildConfig
import it.liceoarzignano.bold.R
import it.liceoarzignano.bold.safe.mod.Encryption
import it.liceoarzignano.bold.settings.AppPrefs
import it.liceoarzignano.bold.utils.SystemUtils
import it.liceoarzignano.bold.utils.UiUtils
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.security.SecureRandom

class BenefitFragment : Fragment() {

    lateinit private var mTitle: TextView
    lateinit private var mMessage: TextView
    lateinit private var mIntroImage: ImageView
    lateinit private var mButton: AppCompatButton
    lateinit private var mViewPager: BenefitViewPager

    lateinit private var mPrefs: AppPrefs
    private var mAnimThread: Thread? = null
    private var isWorking = false

    internal fun newInstance(sectionNumber: Int): BenefitFragment {
        val fragment = BenefitFragment()
        val args = Bundle()
        args.putInt(POSITION, sectionNumber)
        fragment.arguments = args
        return fragment
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstance: Bundle?): View? {
        val position = arguments.getInt(POSITION)
        val view = inflater!!.inflate(R.layout.fragment_benefits_contents, container, false)
        mTitle = view.findViewById(R.id.intro_title)
        mMessage = view.findViewById(R.id.intro_message)
        mViewPager = (activity as BenefitsActivity).viewPager

        mPrefs = AppPrefs(activity.baseContext)

        // Step 1
        val step1 = view.findViewById<LinearLayout>(R.id.step_1)
        mIntroImage = view.findViewById(R.id.intro_animation)

        // Step 2
        val step2 = view.findViewById<RadioGroup>(R.id.step_2)
        val radio1 = view.findViewById<RadioButton>(R.id.intro_address_1)
        val radio2 = view.findViewById<RadioButton>(R.id.intro_address_2)
        val radio3 = view.findViewById<RadioButton>(R.id.intro_address_3)
        val radio4 = view.findViewById<RadioButton>(R.id.intro_address_4)
        val radio5 = view.findViewById<RadioButton>(R.id.intro_address_5)
        val radio6 = view.findViewById<RadioButton>(R.id.intro_address_6)
        mButton = (activity as BenefitsActivity).button

        when (position) {
            0 -> {
                mTitle.text = getString(R.string.slide0_title)
                mMessage.text = getString(R.string.slide0_message_loading)
                mTitle.alpha = 0f
                mMessage.alpha = 0f
            }
            1 -> {
                mTitle.text = getString(R.string.slide1_title)
                mMessage.text = getString(R.string.slide1_message)
                step1.visibility = View.GONE
                step2.visibility = View.VISIBLE
                radio1.setOnClickListener { _ -> setAddress("1") }
                radio2.setOnClickListener { _ -> setAddress("2") }
                radio3.setOnClickListener { _ -> setAddress("3") }
                radio4.setOnClickListener { _ -> setAddress("4") }
                radio5.setOnClickListener { _ -> setAddress("5") }
                radio6.setOnClickListener { _ -> setAddress(null) }
                mButton.visibility = View.INVISIBLE
                mButton.setOnClickListener(null)
            }
        }

        return view
    }

    private fun setAddress(value: String?) {
        mViewPager.setScrollAllowed(true)
        if (value == null) {
            mPrefs.set(AppPrefs.KEY_IS_TEACHER, true)
        } else {
            mPrefs.set(AppPrefs.KEY_ADDRESS, value)
        }

        mButton.visibility = View.VISIBLE
        mButton.text = getString(R.string.intro_btn_done)
        mButton.setOnClickListener { _ -> (activity as BenefitsActivity).onPageChanged(2) }
    }

    fun doDeviceCheck(context: Context) {
        mButton.visibility = View.INVISIBLE

        // Check for internet connection
        if (SystemUtils.hasNoInternetConnection(context)) {
            if (BuildConfig.DEBUG) {
                (activity as BenefitsActivity).viewPager.setScrollAllowed(true)
                return
            }

            mButton.visibility = View.VISIBLE
            mButton.setOnClickListener { _ -> doDeviceCheck(context) }
            mMessage.text = getString(R.string.slide0_message_failed)
            if (SystemUtils.isNotLegacy) {
                isWorking = false
                mIntroImage.setImageResource(R.drawable.avd_intro_failed)
                UiUtils.animateAVD(mIntroImage.drawable)
            }
            return
        }

        /*
         * Warning: contains hackery because avd animation callbacks are
         * api23+ only. Animations will be shown on api21+.
         */
        if (SystemUtils.isNotLegacy) {
            mAnimThread = Thread {
                do { // At least once
                    // Run on UI thread
                    Handler(context.mainLooper).post { UiUtils.animateAVD(mIntroImage.drawable) }
                    try {
                        // Wait for current animation to end
                        Thread.sleep(800)
                    } catch (e: InterruptedException) {
                        Log.e(TAG, e.message)
                    }

                } while (isWorking)
            }

            isWorking = true
            // Let the previous animation end
            Handler().postDelayed({
                mIntroImage.setImageResource(R.drawable.avd_intro_load)
                mAnimThread!!.start()
            }, 500)
        }

        (activity as BenefitsActivity).viewPager.setScrollAllowed(false)

        // Don't run SafetyNet test on devices without GMS
        if (SystemUtils.hasNoGMS(context)) {
            postDeviceCheck(Encryption.validateResponse(context, null, BuildConfig.DEBUG))
            return
        }

        // Safety net + integrity check
        val nonce = System.currentTimeMillis().toString()
        val oStream = ByteArrayOutputStream()
        val randBytes = ByteArray(24)
        SecureRandom().nextBytes(randBytes)

        try {
            oStream.write(randBytes)
            oStream.write(nonce.toByteArray())
        } catch (e: IOException) {
            Log.e(TAG, e.message)
        }


        SafetyNet.getClient(activity).attest(oStream.toByteArray(),
                SystemUtils.getSafetyNetApiKey(context))
                .addOnCompleteListener { task ->
                    postDeviceCheck(Encryption.validateResponse(context,
                            task.result.jwsResult, BuildConfig.DEBUG))
                }
    }

    private fun postDeviceCheck(hasPassed: Boolean) {
        // Save safetyNet results
        mPrefs.set(AppPrefs.KEY_SAFE_PASSED, hasPassed)

        if (SystemUtils.isNotLegacy) {
            isWorking = false
            try {
                mAnimThread!!.join()
            } catch (e: InterruptedException) {
                Log.e(TAG, e.message)
            }

            Handler().postDelayed({
                mIntroImage.setImageResource(R.drawable.avd_intro_done)
                UiUtils.animateAVD(mIntroImage.drawable)

                // Allow scrolling when animation ends
                Handler().postDelayed({
                    mViewPager.setScrollAllowed(true)
                    mMessage.text = getString(R.string.slide0_message_done)
                }, 2800)
            }, 800)
        } else {
            mViewPager.setScrollAllowed(true)
            mMessage.text = getString(R.string.slide0_message_done)
        }
    }

    fun animateIntro() {
        mTitle.animate().alpha(1f).setStartDelay(400).start()
        mMessage.animate().alpha(1f).setStartDelay(420).start()
        mIntroImage.setImageResource(if (SystemUtils.isNotLegacy)
            R.drawable.avd_intro_start
        else
            R.drawable.ic_hat)
        UiUtils.animateAVD(mIntroImage.drawable)
    }

    companion object {
        private val TAG = BenefitFragment::class.java.simpleName
        private val POSITION = "section_number"
    }
}
