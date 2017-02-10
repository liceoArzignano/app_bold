package it.liceoarzignano.bold.intro;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class BenefitViewPager extends ViewPager {
    private boolean isScrollAllowed = false;

    public BenefitViewPager(Context mContext) {
        super(mContext);
    }

    public BenefitViewPager(Context mContext, AttributeSet mAttrs) {
        super(mContext, mAttrs);
    }

    void setScrollAllowed(boolean isScrollAllowed) {
        this.isScrollAllowed = isScrollAllowed;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent mEvent) {
        return isScrollAllowed && super.onInterceptTouchEvent(mEvent);
    }

    @Override
    public boolean onTouchEvent(MotionEvent mEvent) {
        return isScrollAllowed && super.onTouchEvent(mEvent);
    }
}
