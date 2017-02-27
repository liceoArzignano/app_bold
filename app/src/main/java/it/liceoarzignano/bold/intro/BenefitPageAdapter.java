package it.liceoarzignano.bold.intro;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

class BenefitPageAdapter extends FragmentPagerAdapter {
    private final SparseArray<BenefitFragment> mFragments;

    BenefitPageAdapter(FragmentManager manager) {
        super(manager);
        mFragments = new SparseArray<>();
    }

    @Override
    public Fragment getItem(int position) {
        return new BenefitFragment().newInstance(position);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        BenefitFragment fragment = (BenefitFragment) super.instantiateItem(container, position);
        mFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    BenefitFragment getFragment(int position) {
        return mFragments.get(position);
    }
}