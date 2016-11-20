package it.liceoarzignano.bold.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import it.liceoarzignano.bold.BoldApp;
import it.liceoarzignano.bold.R;

public class DividerDecoration extends RecyclerView.ItemDecoration {
    private final Drawable mDivider;

    public DividerDecoration(Context mContext) {
        TypedArray mTyped = mContext.obtainStyledAttributes(
                new int[] { android.R.attr.listDivider });
        mDivider = mTyped.getDrawable(0);
        mTyped.recycle();
    }

    @Override
    public void onDrawOver(Canvas mCanvas, RecyclerView mParent, RecyclerView.State mState) {
        int mLeft = mParent.getPaddingLeft();
        int mRight = mParent.getWidth() - mParent.getPaddingRight();

        int mChildCount = mParent.getChildCount();
        for (int mCounter = 0; mCounter < mChildCount; mCounter++) {
            View mChild = mParent.getChildAt(mCounter);
            RecyclerView.LayoutParams mParams = (RecyclerView.LayoutParams) mChild
                    .getLayoutParams();
            int mTop = mChild.getBottom() + mParams.bottomMargin;
            int mTottom = mTop + mDivider.getIntrinsicHeight();
            mDivider.setBounds(mLeft, mTop, mRight, mTottom);
            mDivider.setColorFilter(ContextCompat.getColor(BoldApp.getContext(),
                    R.color.list_divider), PorterDuff.Mode.SRC_ATOP);
            mDivider.draw(mCanvas);
        }
    }


    @Override
    public void getItemOffsets(Rect mOut, View mView, RecyclerView mParent,
                               RecyclerView.State mState) {
        mOut.set(0, 0, 0, mDivider.getIntrinsicHeight());
    }
}
