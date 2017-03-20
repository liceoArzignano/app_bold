package it.liceoarzignano.bold.intro;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
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

public class BenefitFragment extends Fragment {

    private TextView mTitle;
    private TextView mMessage;
    private ImageView mIntroImage;
    private AppCompatButton mButton;
    private BenefitViewPager mViewPager;

    private Thread mAnimThread;
    private boolean isWorking;

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
        mTitle = (TextView) view.findViewById(R.id.intro_title);
        mMessage = (TextView) view.findViewById(R.id.intro_message);
        mViewPager = ((BenefitsActivity) getActivity()).getViewPager();

        // Step 1
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
        mButton = ((BenefitsActivity) getActivity()).getButton();

        switch (position) {
            case 0:
                mTitle.setText(getString(R.string.slide0_title));
                mMessage.setText(getString(R.string.slide0_message_loading));
                mTitle.setAlpha(0f);
                mMessage.setAlpha(0f);
                break;
            case 1:
                mTitle.setText(getString(R.string.slide1_title));
                mMessage.setText(getString(R.string.slide1_message));
                step1.setVisibility(View.GONE);
                step2.setVisibility(View.VISIBLE);
                radio1.setOnClickListener((mRadioView) -> setAddress("1"));
                radio2.setOnClickListener((mRadioView) -> setAddress("2"));
                radio3.setOnClickListener((mRadioView) -> setAddress("3"));
                radio4.setOnClickListener((mRadioView) -> setAddress("4"));
                radio5.setOnClickListener((mRadioView) -> setAddress("5"));
                radio6.setOnClickListener((mRadioView) -> setAddress(null));
                mButton.setVisibility(View.INVISIBLE);
                mButton.setOnClickListener(null);
                break;
        }

        return view;
    }

    private void setAddress(String value) {
        mViewPager.setScrollAllowed(true);
        if (value == null) {
            Utils.setTeacherMode(getContext());
        } else {
            Utils.setAddress(getContext(), value);
        }

        mButton.setVisibility(View.VISIBLE);
        mButton.setText(getString(R.string.intro_btn_done));
        mButton.setOnClickListener(view -> ((BenefitsActivity) getActivity()).onPageChanged(2));
    }

    void doDeviceCheck(Context context) {
        mButton.setVisibility(View.INVISIBLE);

        // Check for internet connection
        if (Utils.hasNoInternetConnection(context)) {
            mButton.setVisibility(View.VISIBLE);
            mButton.setOnClickListener(view -> doDeviceCheck(context));
            mMessage.setText(getString(R.string.slide0_message_failed));
            if (Utils.isNotLegacy()) {
                isWorking = false;
                mIntroImage.setImageResource(R.drawable.avd_intro_failed);
                Utils.animateAVD(mIntroImage.getDrawable());
            }
            return;
        }

        /*
         * Warning: contains hackery because avd animation callbacks are
         * api23+ only. Animations will be shown on api21+.
         */
        if (Utils.isNotLegacy()) {
            mAnimThread = new Thread(() -> {
                do { // At least once
                    // Run on UI thread
                    new Handler(getContext().getMainLooper()).post(() ->
                            Utils.animateAVD(mIntroImage.getDrawable()));
                    try {
                        // Wait for current animation to end
                        Thread.sleep(800);
                    } catch (InterruptedException e) {
                        Log.e("Intro", e.getMessage());
                    }
                } while (isWorking);
            });

            isWorking = true;
            // Let the previous animation end
            new Handler().postDelayed(() -> {
                mIntroImage.setImageResource(R.drawable.avd_intro_load);
                mAnimThread.start();
            }, 500);
        }

        ((BenefitsActivity) getActivity()).getViewPager().setScrollAllowed(false);

        // Don't run SafetyNet test on devices without GMS
        if (Utils.hasNoGMS(context)) {
            postDeviceCheck(context, Encryption.validateRespose(context, null, BuildConfig.DEBUG));
            return;
        }

        // Safety net + integrity check
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

        SafetyNet.SafetyNetApi.attest(client, oStream.toByteArray())
                .setResultCallback((mResult) ->
                        postDeviceCheck(context, Encryption.validateRespose(context,
                                mResult.getJwsResult(), BuildConfig.DEBUG)));
    }

    private void postDeviceCheck(Context context, boolean hasPassed) {
        // Save safetynet results
        Utils.setSafetyNetResults(context, hasPassed);

        if (Utils.isNotLegacy()) {
            isWorking = false;
            try {
                mAnimThread.join();
            } catch (InterruptedException e) {
                Log.e("Intro", e.getMessage());
            }
            new Handler().postDelayed(() -> {
                mIntroImage.setImageResource(R.drawable.avd_intro_done);
                Utils.animateAVD(mIntroImage.getDrawable());

                // Allow scrolling when animation ends
                new Handler().postDelayed(() -> {
                    mViewPager.setScrollAllowed(true);
                    mMessage.setText(getString(R.string.slide0_message_done));
                }, 2800);
            }, 800);
        } else {
            mViewPager.setScrollAllowed(true);
            mMessage.setText(getString(R.string.slide0_message_done));
        }
    }

    void animateIntro() {
        mTitle.animate().alpha(1f).setStartDelay(400).start();
        mMessage.animate().alpha(1f).setStartDelay(420).start();
        mIntroImage.setImageResource(Utils.isNotLegacy() ?
                R.drawable.avd_intro_start : R.drawable.ic_hat);
        Utils.animateAVD(mIntroImage.getDrawable());
    }
}
