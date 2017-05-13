package it.liceoarzignano.bold.intro;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class BenefitViewPager extends ViewPager {
    private boolean isScrollAllowed = false;

    public BenefitViewPager(Context context) {
        super(context);
    }

    public BenefitViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    void setScrollAllowed(boolean isScrollAllowed) {
        this.isScrollAllowed = isScrollAllowed;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return isScrollAllowed && super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return isScrollAllowed && super.onTouchEvent(event);
    }
}
