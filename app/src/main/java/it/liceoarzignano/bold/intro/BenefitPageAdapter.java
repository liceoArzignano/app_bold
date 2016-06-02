package it.liceoarzignano.bold.intro;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class BenefitPageAdapter extends FragmentPagerAdapter {

    BenefitPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return BenefitFragment.newInstance(position + 1);
    }

    @Override
    public int getCount() {
        return 3;
    }
}