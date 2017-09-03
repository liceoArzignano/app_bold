package it.liceoarzignano.bold.intro

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatButton
import com.google.InkPageIndicator
import it.liceoarzignano.bold.MainActivity
import it.liceoarzignano.bold.R
import it.liceoarzignano.bold.settings.AppPrefs

class BenefitsActivity : AppCompatActivity() {
    lateinit var viewPager: BenefitViewPager
    lateinit private var mAdapter: BenefitPageAdapter

    private var setupLevel = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_benefits)

        val indicator = findViewById<InkPageIndicator>(R.id.indicator)
        viewPager = findViewById(R.id.container)

        mAdapter = BenefitPageAdapter(supportFragmentManager)
        viewPager.adapter = mAdapter
        indicator.setViewPager(viewPager)
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float,
                                        positionOffsetPixels: Int) = onPageChanged(position)
            override fun onPageSelected(position: Int) = Unit
            override fun onPageScrollStateChanged(state: Int) = Unit
        })
        viewPager.currentItem = 0
    }

    internal fun onPageChanged(position: Int) {
        if (position != setupLevel) {
            return
        }

        when (position) {
            0 -> {
                viewPager.setScrollAllowed(false)
                val fragment = mAdapter.firstFragment
                fragment.animateIntro()
                Handler().postDelayed({ fragment.doDeviceCheck(this) }, 500)
                setupLevel++
            }
            1 -> {
                viewPager.setScrollAllowed(false)
                setupLevel++
            }
            2 -> {
                AppPrefs(baseContext).set(AppPrefs.KEY_INTRO_SCREEN, true)
                startActivity(Intent(this@BenefitsActivity, MainActivity::class.java))
                finish()
            }
        }
    }

    internal val button: AppCompatButton get() = findViewById(R.id.benefit_button)
}