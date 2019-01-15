package it.liceoarzignano.bold.ui.recyclerview

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.liceoarzignano.bold.R
import it.liceoarzignano.bold.utils.UiUtils

class RecyclerViewExt : RecyclerView {
    private lateinit var mItemTouchListener: RecyclerView.OnItemTouchListener

    constructor(context: Context) : super(context) {
        setup(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setup(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, style: Int) : super(context, attrs, style) {
        setup(context, attrs)
    }

    override fun addOnItemTouchListener(listener: androidx.recyclerview.widget.RecyclerView.OnItemTouchListener) {
        if (::mItemTouchListener.isInitialized) {
            removeOnItemTouchListener(mItemTouchListener)
        }
        mItemTouchListener = listener

        super.addOnItemTouchListener(listener)
    }

    private fun setup(context: Context, attrs: AttributeSet?) {
        itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
        if (attrs == null) {
            return
        }

        val array = context.obtainStyledAttributes(attrs,
                R.styleable.RecyclerViewExt, 0, 0)

        layoutManager = try {
            when {
                array.getBoolean(R.styleable.RecyclerViewExt_horizontalMode, false) ->
                    LinearLayoutManager(context, HORIZONTAL, false)
                UiUtils.isPhone(context) ->
                    LinearLayoutManager(context)
                else ->
                    if (array.getBoolean(R.styleable.RecyclerViewExt_tabletUI, false))
                        GridLayoutManager(context, 2)
                    else
                        LinearLayoutManager(context)
            }
        } finally {
            array.recycle()
        }
    }
}
