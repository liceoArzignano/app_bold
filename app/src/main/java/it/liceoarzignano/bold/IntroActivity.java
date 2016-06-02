package it.liceoarzignano.bold;

import android.content.Intent;
import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;

@SuppressWarnings("deprecation")
public class IntroActivity extends AppIntro2 {

    public void init(Bundle savedInstanceState) {
        addSlide(AppIntroFragment.newInstance(getString(R.string.slide0_title),
                getString(R.string.slide0_message), R.drawable.slide_0,
                getResources().getColor(R.color.slide_color)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.slide1_title),
                getString(R.string.slide1_message), R.drawable.slide_1,
                getResources().getColor(R.color.slide_color)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.slide2_title),
                getString(R.string.slide2_message), R.drawable.slide_2,
                getResources().getColor(R.color.slide_color)));

        setProgressButtonEnabled(true);

        showStatusBar(false);
    }

    @Override
    public void onDonePressed() {
        getSharedPreferences("HomePrefs", MODE_PRIVATE).edit().putBoolean("introKey", true).apply();
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