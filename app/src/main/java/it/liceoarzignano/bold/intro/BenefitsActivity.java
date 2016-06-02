package it.liceoarzignano.bold.intro;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.ImageView;

import it.liceoarzignano.bold.MainActivity;
import it.liceoarzignano.bold.R;

public class BenefitsActivity extends AppCompatActivity {

    private BenefitPageAdapter mSectionsPagerAdapter;
    private AppCompatButton mFinishBtn;
    private ImageView mIndicator0;
    private ImageView mIndicator1;
    private ImageView mIndicator2;
    private ImageView[] mIndicators;
    private int page = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_benefits);

        mSectionsPagerAdapter = new BenefitPageAdapter(getSupportFragmentManager());

        mFinishBtn = (AppCompatButton) findViewById(R.id.intro_btn_finish);

        mIndicator0 = (ImageView) findViewById(R.id.intro_indicator_0);
        mIndicator1 = (ImageView) findViewById(R.id.intro_indicator_1);
        mIndicator2 = (ImageView) findViewById(R.id.intro_indicator_2);

        mIndicators = new ImageView[] {
                mIndicator0,
                mIndicator1,
                mIndicator2
        };

        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        if (mViewPager != null) {
            mViewPager.setAdapter(mSectionsPagerAdapter);
            mViewPager.setCurrentItem(page);
        }

        updateIndicators(page);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                page = position;
                updateIndicators(page);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

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

    private void updateIndicators(int position) {
        for (int i = 0; i < mIndicators.length; i++) {
            mIndicators[i].setBackgroundResource(
                    i == position ?
                            R.drawable.indicator_selected :
                            R.drawable.indicator_unselected);
        }
    }
}
