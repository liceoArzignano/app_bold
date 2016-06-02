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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final int position = getArguments().getInt("section_number");

        View view = inflater.inflate(R.layout.fragment_benefits_contents, container, false);
        TextView mTitle = (TextView) view.findViewById(R.id.title);
        TextView mDescription = (TextView) view.findViewById(R.id.message);
        ImageView mImage = (ImageView) view.findViewById(R.id.section_img);


        switch (position) {
            case 1:
                mTitle.setText(getString(R.string.slide0_title));
                mDescription.setText(getString(R.string.slide0_message));
                mImage.setBackgroundResource(R.drawable.slide_0);
                break;
            case 2:
                mTitle.setText(getString(R.string.slide1_title));
                mDescription.setText(getString(R.string.slide1_message));
                mImage.setBackgroundResource(R.drawable.slide_1);
                break;
            case 3:
                mTitle.setText(getString(R.string.slide2_title));
                mDescription.setText(getString(R.string.slide2_message));
                mImage.setBackgroundResource(R.drawable.slide_2);
                break;

        }


        return view;
    }
}
