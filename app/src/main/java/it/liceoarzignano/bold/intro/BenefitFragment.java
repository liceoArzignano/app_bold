package it.liceoarzignano.bold.intro;

import android.content.Context;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.safetynet.SafetyNet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;

import it.liceoarzignano.bold.BuildConfig;
import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.Utils;
import it.liceoarzignano.bold.safe.mod.Encryption;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class BenefitFragment extends Fragment {

    private TextView title;
    private TextView message;

    private ImageView mIntroImage;

    private TextView mSafeMessage;
    private MaterialProgressBar mSafeProgressBar;
    private ImageView mSafeImage;
    private AppCompatButton mSafeRetryButton;

    public BenefitFragment() {
    }

    BenefitFragment newInstance(int sectionNumber) {
        BenefitFragment fragment = new BenefitFragment();
        Bundle args = new Bundle();
        args.putInt("section_number", sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstance) {
        int position = getArguments().getInt("section_number");
        View view = inflater.inflate(R.layout.fragment_benefits_contents, container, false);
        title = (TextView) view.findViewById(R.id.intro_title);
        message = (TextView) view.findViewById(R.id.intro_message);

        // Step
        LinearLayout step1 = (LinearLayout) view.findViewById(R.id.step_1);
        mIntroImage = (ImageView) view.findViewById(R.id.intro_animation);

        // Step 2
        RadioGroup step2 = (RadioGroup) view.findViewById(R.id.step_2);
        RadioButton radio1 = (RadioButton) view.findViewById(R.id.intro_address_1);
        RadioButton radio2 = (RadioButton) view.findViewById(R.id.intro_address_2);
        RadioButton radio3 = (RadioButton) view.findViewById(R.id.intro_address_3);
        RadioButton radio4 = (RadioButton) view.findViewById(R.id.intro_address_4);
        RadioButton radio5 = (RadioButton) view.findViewById(R.id.intro_address_5);
        RadioButton radio6 = (RadioButton) view.findViewById(R.id.intro_address_6);

        // Step 3
        LinearLayout step3 = (LinearLayout) view.findViewById(R.id.step_3);
        mSafeMessage = (TextView) view.findViewById(R.id.intro_safe_message);
        mSafeProgressBar = (MaterialProgressBar) view.findViewById(R.id.intro_safe_bar);
        mSafeImage = (ImageView) view.findViewById(R.id.intro_safe_anim);
        mSafeRetryButton = (AppCompatButton) view.findViewById(R.id.intro_safe_retry);

        mSafeRetryButton.setOnClickListener((mButtonView) -> doDeviceCheck(getActivity()));

        switch (position) {
            case 0:
                title.setText(getString(R.string.slide0_title));
                title.setAlpha(0f);
                message.setText(getString(R.string.slide0_message));
                message.setAlpha(0f);
                break;
            case 1:
                title.setText(getString(R.string.slide1_title));
                message.setText(getString(R.string.slide1_message));
                step1.setVisibility(View.GONE);
                step2.setVisibility(View.VISIBLE);
                step3.setVisibility(View.GONE);
                radio1.setOnClickListener((mRadioView) -> setAddress("1"));
                radio2.setOnClickListener((mRadioView) -> setAddress("2"));
                radio3.setOnClickListener((mRadioView) -> setAddress("3"));
                radio4.setOnClickListener((mRadioView) -> setAddress("4"));
                radio5.setOnClickListener((mRadioView) -> setAddress("5"));
                radio6.setOnClickListener((mRadioView) -> setAddress(null));
                break;
            case 2:
                title.setText(getString(R.string.slide2_title));
                message.setText(getString(R.string.slide2_message));
                step1.setVisibility(View.GONE);
                step2.setVisibility(View.GONE);
                step3.setVisibility(View.VISIBLE);
                break;
        }

        return view;
    }

    private void setAddress(String value) {
        ((BenefitsActivity) getActivity()).mViewPager.setScrollAllowed(true);
        if (value == null) {
            Utils.setTeacherMode(getContext());
        } else {
            Utils.setAddress(getContext(), value);
        }
    }

    void doDeviceCheck(Context context) {
        mSafeImage.setImageResource(R.drawable.ic_safe);
        mSafeProgressBar.setVisibility(View.VISIBLE);
        mSafeRetryButton.setVisibility(View.GONE);
        mSafeMessage.setText("");

        GoogleApiClient client = new GoogleApiClient.Builder(context)
                .addApi(SafetyNet.API)
                .build();
        client.connect();

        String nonce = String.valueOf(System.currentTimeMillis());
        ByteArrayOutputStream oStream = new ByteArrayOutputStream();
        byte[] randBytes = new byte[24];
        new SecureRandom().nextBytes(randBytes);

        try {
            oStream.write(randBytes);
            oStream.write(nonce.getBytes());
        } catch (IOException e) {
            Log.e("SafetyNetTest", e.getMessage());
        }

        ((BenefitsActivity) getActivity()).mViewPager.setScrollAllowed(false);
        if (Utils.hasInternetConnection(context)) {
            SafetyNet.SafetyNetApi
                    .attest(client, oStream.toByteArray())
                    .setResultCallback((mResult) -> postDeviceCheck(context,
                            Encryption.validateRespose(context, mResult.getJwsResult(),
                                    BuildConfig.DEBUG)));
        } else {
            mSafeImage.setImageResource(R.drawable.avd_intro_no_connection);
            mSafeProgressBar.setVisibility(View.GONE);
            mSafeRetryButton.setVisibility(View.VISIBLE);
            mSafeMessage.setText(getString(R.string.intro_safe_retry_message));
            if (Utils.isNotLegacy()) {
                ((AnimatedVectorDrawable) mSafeImage.getDrawable()).start();
            }
            mSafeRetryButton.setVisibility(View.VISIBLE);
        }
    }

    private void postDeviceCheck(Context context, boolean hasPassed) {
        Utils.setSafetyNetResults(context, hasPassed);

        mSafeMessage.setVisibility(View.VISIBLE);
        mSafeRetryButton.setVisibility(View.VISIBLE);
        mSafeRetryButton.setText(getString(R.string.intro_safe_done));
        mSafeRetryButton.setOnClickListener((mButtonView) ->
                ((BenefitsActivity) getActivity()).onPageChanged(3));

        if (hasPassed) {
            mSafeProgressBar.setVisibility(View.GONE);
            mSafeMessage.setText(getString(R.string.intro_safe_retry_success));
        } else {
            mSafeProgressBar.setVisibility(View.GONE);
            mSafeMessage.setVisibility(View.VISIBLE);
            mSafeMessage.setTextColor(ContextCompat.getColor(context, R.color.red));
            mSafeMessage.setText(getString(R.string.intro_safe_failure));
        }

        if (Utils.isNotLegacy()) {
            mSafeImage.setImageResource(hasPassed ?
                    R.drawable.avd_intro_done : R.drawable.avd_intro_failed);
            ((AnimatedVectorDrawable) mSafeImage.getDrawable()).start();
        } else {
            mSafeImage.setImageResource(hasPassed ?
                    R.drawable.ic_done : R.drawable.ic_safe);
        }
    }

    void animateIntro() {
        if (Utils.isNotLegacy()) {
            title.animate().alpha(1f).setStartDelay(800).start();
            message.animate().alpha(1f).setStartDelay(820).start();

            ((AnimatedVectorDrawable) mIntroImage.getDrawable()).start();
        }
    }
}
