package it.liceoarzignano.bold.ui.recyclerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {
    private final GestureDetector mDetector;
    private final RecyclerClickListener mListener;

    public RecyclerTouchListener(Context context, final RecyclerClickListener listener) {
        this.mListener = listener;

        mDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent event) {
                return true;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent event) {
        View child = view.findChildViewUnder(event.getX(), event.getY());
        if (child != null && mListener != null && mDetector.onTouchEvent(event)) {
            mListener.onClick(child, view.getChildAdapterPosition(child));
            return true;
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView view, MotionEvent event) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean shouldDisallow) {
    }
}
