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
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.safetynet.SafetyNet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;

import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.Utils;
import it.liceoarzignano.bold.safe.Encryption;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class BenefitFragment extends Fragment {

    private TextView mTitle;
    private TextView mMessage;

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
    public View onCreateView(LayoutInflater mInflater, ViewGroup mContainer,
                             Bundle mSavedInstance) {
        int mPosition = getArguments().getInt("section_number");
        View mView = mInflater.inflate(R.layout.fragment_benefits_contents, mContainer, false);
        mTitle = (TextView) mView.findViewById(R.id.intro_title);
        mMessage = (TextView) mView.findViewById(R.id.intro_message);

        // Step
        LinearLayout mStep1 = (LinearLayout) mView.findViewById(R.id.step_1);
        mIntroImage = (ImageView) mView.findViewById(R.id.intro_animation);

        // Step 2
        LinearLayout mStep2 = (LinearLayout) mView.findViewById(R.id.step_2);
        RadioButton mRadio1 = (RadioButton) mView.findViewById(R.id.intro_address_1);
        RadioButton mRadio2 = (RadioButton) mView.findViewById(R.id.intro_address_2);
        RadioButton mRadio3 = (RadioButton) mView.findViewById(R.id.intro_address_3);
        RadioButton mRadio4 = (RadioButton) mView.findViewById(R.id.intro_address_4);
        RadioButton mRadio5 = (RadioButton) mView.findViewById(R.id.intro_address_5);
        RadioButton mRadio6 = (RadioButton) mView.findViewById(R.id.intro_address_6);

        // Step 3
        LinearLayout mStep3 = (LinearLayout) mView.findViewById(R.id.step_3);
        mSafeMessage = (TextView) mView.findViewById(R.id.intro_safe_message);
        mSafeProgressBar = (MaterialProgressBar) mView.findViewById(R.id.intro_safe_bar);
        mSafeImage = (ImageView) mView.findViewById(R.id.intro_safe_anim);
        mSafeRetryButton = (AppCompatButton) mView.findViewById(R.id.intro_safe_retry);

        mSafeRetryButton.setOnClickListener((mButtonView) -> doDeviceCheck(getActivity()));

        switch (mPosition) {
            case 0:
                mTitle.setText(getString(R.string.slide0_title));
                mTitle.setAlpha(0f);
                mMessage.setText(getString(R.string.slide0_message));
                mMessage.setAlpha(0f);
                break;
            case 1:
                mTitle.setText(getString(R.string.slide1_title));
                mMessage.setText(getString(R.string.slide1_message));
                mStep1.setVisibility(View.GONE);
                mStep2.setVisibility(View.VISIBLE);
                mStep3.setVisibility(View.GONE);
                mRadio1.setOnClickListener((mRadioView) -> setAddress("1"));
                mRadio2.setOnClickListener((mRadioView) -> setAddress("2"));
                mRadio3.setOnClickListener((mRadioView) -> setAddress("3"));
                mRadio4.setOnClickListener((mRadioView) -> setAddress("4"));
                mRadio5.setOnClickListener((mRadioView) -> setAddress("5"));
                mRadio6.setOnClickListener((mRadioView) -> setAddress(null));
                break;
            case 2:
                mTitle.setText(getString(R.string.slide2_title));
                mMessage.setText(getString(R.string.slide2_message));
                mStep1.setVisibility(View.GONE);
                mStep2.setVisibility(View.GONE);
                mStep3.setVisibility(View.VISIBLE);
                break;
        }

        return mView;
    }

    private void setAddress(String mVal) {
        ((BenefitsActivity) getActivity()).mViewPager.setScrollAllowed(true);
        if (mVal == null) {
            Utils.setTeacherMode(getContext());
        } else {
            Utils.setAddress(getContext(), mVal);
        }
    }

    void doDeviceCheck(Context mContext) {
        mSafeImage.setImageResource(R.drawable.ic_safe);
        mSafeProgressBar.setVisibility(View.VISIBLE);
        mSafeRetryButton.setVisibility(View.GONE);
        mSafeMessage.setText("");

        GoogleApiClient mClient = new GoogleApiClient.Builder(mContext)
                .addApi(SafetyNet.API)
                .build();
        mClient.connect();

        String mNonceData = String.valueOf(System.currentTimeMillis());
        ByteArrayOutputStream mOstream = new ByteArrayOutputStream();
        byte[] mRandBytes = new byte[24];
        new SecureRandom().nextBytes(mRandBytes);

        try {
            mOstream.write(mRandBytes);
            mOstream.write(mNonceData.getBytes());
        } catch (IOException e) {
            Log.e("SafetyNetTest", e.getMessage());
        }

        ((BenefitsActivity) getActivity()).mViewPager.setScrollAllowed(false);
        if (Utils.hasInternetConnection(mContext)) {
            SafetyNet.SafetyNetApi
                    .attest(mClient, mOstream.toByteArray())
                    .setResultCallback((mResult) -> postDeviceCheck(mContext,
                            Encryption.validateRespose(mContext, mResult.getJwsResult())));
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

    void postDeviceCheck(Context mContext, boolean hasPassed) {
        Utils.setSafetyNetResults(mContext, hasPassed);

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
            mSafeMessage.setTextColor(ContextCompat.getColor(mContext, R.color.red));
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
            mTitle.animate().alpha(1f).setStartDelay(800).start();
            mMessage.animate().alpha(1f).setStartDelay(820).start();

            ((AnimatedVectorDrawable) mIntroImage.getDrawable()).start();
        }
    }
}
