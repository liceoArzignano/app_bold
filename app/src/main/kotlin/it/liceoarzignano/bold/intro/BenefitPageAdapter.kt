package it.liceoarzignano.bold.intro

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.util.SparseArray
import android.view.ViewGroup

internal class BenefitPageAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {
    private val mFragments = SparseArray<BenefitFragment>()

    override fun getItem(position: Int): Fragment = BenefitFragment().newInstance(position)

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val fragment = super.instantiateItem(container, position) as BenefitFragment
        mFragments.put(position, fragment)
        return fragment
    }

    override fun getCount(): Int = 2

    val firstFragment: BenefitFragment
        get() = mFragments.get(0)
}