package it.liceoarzignano.bold.intro;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class BenefitPageAdapter extends FragmentPagerAdapter {

    BenefitPageAdapter(FragmentManager mFragmentManager) {
        super(mFragmentManager);
    }

    @Override
    public Fragment getItem(int mPosition) {
        return BenefitFragment.newInstance(mPosition + 1);
    }

    @Override
    public int getCount() {
        return 3;
    }
}