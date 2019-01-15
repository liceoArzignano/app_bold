package it.liceoarzignano.bold.ui.recyclerview

import android.content.res.Resources
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import it.liceoarzignano.bold.R
import it.liceoarzignano.bold.utils.SystemUtils
import it.liceoarzignano.bold.utils.UiUtils

class ScrollListener(private val mToolbar: Toolbar, private val mResources: Resources,
                     private val mTop: Int) : RecyclerView.OnScrollListener() {

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        val firstChild = recyclerView.getChildAt(0)
        val shouldElevate = firstChild != null && firstChild.top < mTop

        if (SystemUtils.isNotLegacy) {
            mToolbar.elevation = if (shouldElevate)
                UiUtils.dpToPx(mResources, mResources.getDimension(R.dimen.scroll_toolbar_elevation))
            else
                0F
        }
    }
}