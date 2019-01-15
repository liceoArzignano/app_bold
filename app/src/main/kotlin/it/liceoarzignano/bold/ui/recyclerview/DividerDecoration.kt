package it.liceoarzignano.bold.ui.recyclerview

import android.content.Context
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import it.liceoarzignano.bold.R

class DividerDecoration(private val mContext: Context) : RecyclerView.ItemDecoration() {
    private lateinit var mDivider: Drawable

    init {
        val typed = mContext.obtainStyledAttributes(
                intArrayOf(android.R.attr.listDivider))
        typed.getDrawable(0)?.run {
            mDivider = this
        }
        typed.recycle()
    }

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight

        val size = parent.childCount
        for (i in 0 until size) {
            val child = parent.getChildAt(i)
            val params = child
                    .layoutParams as androidx.recyclerview.widget.RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin
            val bottom = top + mDivider.intrinsicHeight
            mDivider.setBounds(left, top, right, bottom)
            mDivider.setColorFilter(ContextCompat.getColor(mContext,
                    R.color.list_header), PorterDuff.Mode.SRC_ATOP)
            mDivider.draw(canvas)
        }
    }

    override fun getItemOffsets(out: Rect, view: View, parent: RecyclerView,
                                state: RecyclerView.State) =
            out.set(0, 0, 0, mDivider.intrinsicHeight)
}
