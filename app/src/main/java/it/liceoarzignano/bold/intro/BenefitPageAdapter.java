package it.liceoarzignano.bold.intro;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

class BenefitPageAdapter extends FragmentPagerAdapter {
    private final SparseArray<BenefitFragment> mFragments;

    BenefitPageAdapter(FragmentManager mFragmentManager) {
        super(mFragmentManager);
        mFragments = new SparseArray<>();
    }

    @Override
    public Fragment getItem(int mPosition) {
        return new BenefitFragment().newInstance(mPosition);
    }

    @Override
    public Object instantiateItem(ViewGroup mContainer, int mPosition) {
        BenefitFragment mFragment = (BenefitFragment) super.instantiateItem(mContainer, mPosition);
        mFragments.put(mPosition, mFragment);
        return mFragment;
    }

    @Override
    public int getCount() {
        return 3;
    }

    BenefitFragment getFragment(int mPosition) {
        return mFragments.get(mPosition);
    }
}