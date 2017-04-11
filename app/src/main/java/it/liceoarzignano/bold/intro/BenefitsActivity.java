package it.liceoarzignano.bold.intro;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;

import it.liceoarzignano.bold.MainActivity;
import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.utils.PrefsUtils;
import it.liceoarzignano.bold.ui.InkPageIndicator;

public class BenefitsActivity extends AppCompatActivity {

    private BenefitViewPager mViewPager;
    private BenefitPageAdapter mAdapter;

    private int setupLevel = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_benefits);

        InkPageIndicator indicator = (InkPageIndicator) findViewById(R.id.indicator);
        mViewPager = (BenefitViewPager) findViewById(R.id.container);

        mAdapter = new BenefitPageAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);
        indicator.setViewPager(mViewPager);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {
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
    }

    void onPageChanged(int position) {
        switch (position) {
            case 0:
                if (setupLevel != 0) {
                    break;
                }
                mViewPager.setScrollAllowed(false);
                BenefitFragment fragment = mAdapter.getFirstFragment();
                fragment.animateIntro();
                new Handler().postDelayed(() -> fragment.doDeviceCheck(this), 500);
                setupLevel++;
                break;
            case 1:
                if (setupLevel != 1) {
                    break;
                }
                mViewPager.setScrollAllowed(false);
                setupLevel++;
                break;
            case 2:
                if (setupLevel != 2) {
                    break;
                }
                getSharedPreferences(PrefsUtils.EXTRA_PREFS, MODE_PRIVATE).edit()
                        .putBoolean(PrefsUtils.KEY_INTRO_SCREEN, true).apply();
                startActivity(new Intent(BenefitsActivity.this, MainActivity.class));
                finish();
                break;
        }
    }

    AppCompatButton getButton() {
        return (AppCompatButton) findViewById(R.id.benefit_button);
    }

    BenefitViewPager getViewPager() {
        return mViewPager;
    }
}