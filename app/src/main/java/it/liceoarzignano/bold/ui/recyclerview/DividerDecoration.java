package it.liceoarzignano.bold.ui.recyclerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import it.liceoarzignano.bold.R;

public class DividerDecoration extends RecyclerView.ItemDecoration {
    private final Drawable mDivider;
    private final Context mContext;

    public DividerDecoration(Context context) {
        this.mContext = context;
        TypedArray typed = context.obtainStyledAttributes(
                new int[]{android.R.attr.listDivider});
        mDivider = typed.getDrawable(0);
        typed.recycle();
    }

    @Override
    public void onDrawOver(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int size = parent.getChildCount();
        for (int counter = 0; counter < size; counter++) {
            View child = parent.getChildAt(counter);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + mDivider.getIntrinsicHeight();
            mDivider.setBounds(left, top, right, bottom);
            mDivider.setColorFilter(ContextCompat.getColor(mContext,
                    R.color.list_header), PorterDuff.Mode.SRC_ATOP);
            mDivider.draw(canvas);
        }
    }

    @Override
    public void getItemOffsets(Rect out, View view, RecyclerView parent,
                               RecyclerView.State state) {
        out.set(0, 0, 0, mDivider.getIntrinsicHeight());
    }
}
