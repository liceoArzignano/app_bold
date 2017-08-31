package it.liceoarzignano.bold.ui.recyclerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.utils.UiUtils;

public class RecyclerViewExt extends RecyclerView {
    private OnItemTouchListener mItemTouchListener = null;

    public RecyclerViewExt(Context context) {
        super(context);
        setup(context, null);
    }

    public RecyclerViewExt(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context, attrs);
    }

    public RecyclerViewExt(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);
        setup(context, attrs);
    }

    @Override
    public void addOnItemTouchListener(OnItemTouchListener listener) {
        if (mItemTouchListener != null) {
            removeOnItemTouchListener(mItemTouchListener);

        }
        mItemTouchListener = listener;

        super.addOnItemTouchListener(listener);
    }

    private void setup(Context context, AttributeSet attrs) {
        setItemAnimator(new DefaultItemAnimator());
        if (attrs == null) {
            return;
        }

        TypedArray array = context.obtainStyledAttributes(attrs,
                R.styleable.RecyclerViewExt, 0, 0);

        try {
            if (array.getBoolean(R.styleable.RecyclerViewExt_horizontalMode, false)) {
                setLayoutManager(new LinearLayoutManager(context, HORIZONTAL, false));
            } else if (UiUtils.isPhone(context)) {
                setLayoutManager(new LinearLayoutManager(context));
            } else {
                setLayoutManager(array.getBoolean(R.styleable.RecyclerViewExt_tabletUI, false) ?
                        new GridLayoutManager(context, 2) : new LinearLayoutManager(context));
            }
        } finally {
            array.recycle();
        }
    }
}
