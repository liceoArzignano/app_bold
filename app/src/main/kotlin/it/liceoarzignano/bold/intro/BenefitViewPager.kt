package it.liceoarzignano.bold.intro

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent

class BenefitViewPager : ViewPager {
    private var isScrollAllowed = false

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    fun setScrollAllowed(isScrollAllowed: Boolean) {
        this.isScrollAllowed = isScrollAllowed
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean =
            isScrollAllowed && super.onInterceptTouchEvent(event)

    override fun onTouchEvent(event: MotionEvent): Boolean =
            isScrollAllowed && super.onTouchEvent(event)
}
