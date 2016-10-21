package it.liceoarzignano.bold.intro;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import it.liceoarzignano.bold.R;

public class BenefitFragment extends Fragment {

    public BenefitFragment() {
    }

    static BenefitFragment newInstance(int sectionNumber) {
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

        TextView mTitle = (TextView) mView.findViewById(R.id.benefits_title);
        TextView mDescription = (TextView) mView.findViewById(R.id.benefits_message);
        ImageView mImage = (ImageView) mView.findViewById(R.id.benefits_img);

        switch (mPosition) {
            case 1:
                mTitle.setText(getString(R.string.slide0_title));
                mDescription.setText(getString(R.string.slide0_message));
                mImage.setImageResource(R.drawable.slide_0);
                break;
            case 2:
                mTitle.setText(getString(R.string.slide1_title));
                mDescription.setText(getString(R.string.slide1_message));
                mImage.setImageResource(R.drawable.slide_1);
                break;
            case 3:
                mTitle.setText(getString(R.string.slide2_title));
                mDescription.setText(getString(R.string.slide2_message));
                mImage.setImageResource(R.drawable.slide_2);
                break;
        }

        return mView;
    }
}
