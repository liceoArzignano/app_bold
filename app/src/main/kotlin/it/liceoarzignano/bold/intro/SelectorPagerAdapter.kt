package it.liceoarzignano.bold.intro

import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import it.liceoarzignano.bold.utils.SystemUtils

class SelectorPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {
    private val mFragments = ArrayList<AddressFragment>()

    override fun getCount() = 6

    override fun getItem(position: Int): AddressFragment {
        val fragment = AddressFragment()
        fragment.mPosition = position
        mFragments.add(position, fragment)
        return fragment
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val fragment = super.instantiateItem(container, position) as AddressFragment
        fragment.mPosition = position
        return fragment
    }

    fun setSeletedFragment(position: Int) {
        if (!SystemUtils.isNotLegacy) {
            return
        }

        for ((i, fragment) in mFragments.withIndex()) {
            fragment.setSelected(i == position)
        }
    }
}
