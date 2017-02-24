package it.liceoarzignano.bold.ui.recyclerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

public class RecyclerViewExt extends RecyclerView {
    private OnItemTouchListener mItemTouchListener = null;

    public RecyclerViewExt(Context context) {
        super(context);
    }

    public RecyclerViewExt(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerViewExt(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);
    }

    @Override
    public void addOnItemTouchListener(OnItemTouchListener listener) {
        if (mItemTouchListener != null) {
            removeOnItemTouchListener(mItemTouchListener);

        }
        mItemTouchListener = listener;

        super.addOnItemTouchListener(listener);
    }
}
