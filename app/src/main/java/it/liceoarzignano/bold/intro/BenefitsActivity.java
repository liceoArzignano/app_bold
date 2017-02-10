package it.liceoarzignano.bold.intro;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import it.liceoarzignano.bold.MainActivity;
import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.Utils;
import it.liceoarzignano.bold.ui.InkPageIndicator;

public class BenefitsActivity extends AppCompatActivity {

    BenefitViewPager mViewPager;
    private BenefitPageAdapter mAdapter;

    private int setupLevel = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_benefits);

        InkPageIndicator mIndicator = (InkPageIndicator) findViewById(R.id.indicator);
        mViewPager = (BenefitViewPager) findViewById(R.id.container);

        mAdapter = new BenefitPageAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mIndicator.setViewPager(mViewPager);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                onPageChanged(position);
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        mViewPager.setCurrentItem(0);

        new Handler().postDelayed(() -> {
            (mAdapter.getFragment(0)).animateIntro();
            new Handler().postDelayed(() -> mViewPager.setScrollAllowed(true), 1300);
        }, 500);
    }

    void onPageChanged(int position) {
        switch (position) {
            case 1:
                if (setupLevel != 0) {
                    break;
                }
                mViewPager.setScrollAllowed(false);
                setupLevel++;
                break;
            case 2:
                if (setupLevel != 1) {
                    break;
                }
                mViewPager.setScrollAllowed(false);
                new Handler().postDelayed(() -> mAdapter.getFragment(2).doDeviceCheck(this), 1000);
                setupLevel++;
                break;
            case 3:
                if (setupLevel != 2) {
                    break;
                }
                getSharedPreferences(Utils.EXTRA_PREFS, MODE_PRIVATE).edit()
                        .putBoolean(Utils.KEY_INTRO_SCREEN, true).apply();
                startActivity(new Intent(BenefitsActivity.this, MainActivity.class));
                finish();
                break;
        }
    }
}