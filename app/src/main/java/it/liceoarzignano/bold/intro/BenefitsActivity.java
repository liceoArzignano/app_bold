package it.liceoarzignano.bold.intro;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.View;

import it.liceoarzignano.bold.MainActivity;
import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.external.inkpageindicator.InkPageIndicator;

public class BenefitsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_benefits);

        final BenefitPageAdapter mSectionsPagerAdapter =
                new BenefitPageAdapter(getSupportFragmentManager());

        AppCompatButton mFinishBtn = (AppCompatButton) findViewById(R.id.intro_btn_finish);

        InkPageIndicator inkPageIndicator = (InkPageIndicator) findViewById(R.id.indicator);

        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        if (mViewPager != null && inkPageIndicator != null) {
            mViewPager.setAdapter(mSectionsPagerAdapter);
            mViewPager.setCurrentItem(0);
            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset,
                                           int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });

            inkPageIndicator.setViewPager(mViewPager);
        }

        if (mFinishBtn != null) {
            mFinishBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getSharedPreferences("HomePrefs", MODE_PRIVATE).edit().putBoolean("introKey",
                            true).apply();
                    Intent intent = new Intent(BenefitsActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }
}
