package it.liceoarzignano.bold.ui.recyclerview

import android.content.Context
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import it.liceoarzignano.bold.R
import it.liceoarzignano.bold.utils.UiUtils

class RecyclerViewExt : RecyclerView {
    private var mItemTouchListener: RecyclerView.OnItemTouchListener? = null

    constructor(context: Context) : super(context) {
        setup(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setup(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, style: Int) : super(context, attrs, style) {
        setup(context, attrs)
    }

    override fun addOnItemTouchListener(listener: RecyclerView.OnItemTouchListener) {
        if (mItemTouchListener != null) {
            removeOnItemTouchListener(mItemTouchListener)
        }
        mItemTouchListener = listener

        super.addOnItemTouchListener(listener)
    }

    private fun setup(context: Context, attrs: AttributeSet?) {
        itemAnimator = DefaultItemAnimator()
        if (attrs == null) {
            return
        }

        val array = context.obtainStyledAttributes(attrs,
                R.styleable.RecyclerViewExt, 0, 0)
        val horizontalMode = array.getBoolean(R.styleable.RecyclerViewExt_horizontalMode, false)
        val twoColumnsGrid = array.getBoolean(R.styleable.RecyclerViewExt_twoColumsGrid, false)
        val isPhone = UiUtils.isPhone(context)
        val tabletOnlyGrid = array.getBoolean(R.styleable.RecyclerViewExt_tabletUI, false)

        layoutManager = try {
            when {
                horizontalMode -> LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                twoColumnsGrid  -> GridLayoutManager(context, 2)
                isPhone -> LinearLayoutManager(context)
                tabletOnlyGrid -> GridLayoutManager(context, 2)
                else -> LinearLayoutManager(context)
            }
        } finally {
            array.recycle()
        }
    }
}
