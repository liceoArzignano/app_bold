package it.liceoarzignano.bold;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;

@SuppressWarnings("deprecation")
public class IntroActivity extends AppIntro2 {

    public void init(Bundle savedInstanceState) {
        addSlide(AppIntroFragment.newInstance(getString(R.string.slide0_title),
                getString(R.string.slide0_message), R.drawable.slide_0,
                getResources().getColor(R.color.slide0_color)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.slide1_title),
                getString(R.string.slide1_message), R.drawable.slide_1,
                getResources().getColor(R.color.slide1_color)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.slide2_title),
                getString(R.string.slide2_message), R.drawable.slide_2,
                getResources().getColor(R.color.slide2_color)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.slide3_title),
                getString(R.string.slide3_message), R.drawable.slide_3,
                getResources().getColor(R.color.slide3_color)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.slide4_title),
                getString(R.string.slide4_message), R.drawable.slide_4,
                getResources().getColor(R.color.slide4_color)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.slide5_title),
                getString(R.string.slide5_message), R.drawable.slide_5,
                getResources().getColor(R.color.slide5_color)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.slide6_title),
                getString(R.string.slide6_message), R.drawable.slide_6,
                getResources().getColor(R.color.slide6_color)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.slide7_title),
                getString(R.string.slide7_message), R.drawable.slide_7,
                getResources().getColor(R.color.slide7_color)));

        setProgressButtonEnabled(true);

        showStatusBar(false);
    }

    @Override
    public void onDonePressed() {
        SharedPreferences.Editor editor = getSharedPreferences(MainActivity.INTRO_PREF, MODE_PRIVATE).edit();
        editor.putBoolean(MainActivity.PREF_KEY_INTRO, true).apply();
        Intent i = new Intent(IntroActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onSlideChanged() {
    }

    @Override
    public void onNextPressed() {
    }
}