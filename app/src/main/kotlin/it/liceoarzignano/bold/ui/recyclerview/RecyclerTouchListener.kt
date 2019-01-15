package it.liceoarzignano.bold.ui.recyclerview

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent

class RecyclerTouchListener(context: Context, private val mListener: RecyclerClickListener?) :
        androidx.recyclerview.widget.RecyclerView.OnItemTouchListener {
    private val mDetector = GestureDetector(context,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapUp(event: MotionEvent): Boolean = true
            })


    override fun onInterceptTouchEvent(view: androidx.recyclerview.widget.RecyclerView, event: MotionEvent): Boolean {
        val child = view.findChildViewUnder(event.x, event.y)
        if (child != null && mListener != null && mDetector.onTouchEvent(event)) {
            mListener.onClick(child, view.getChildAdapterPosition(child))
            return true
        }
        return false
    }

    override fun onTouchEvent(view: androidx.recyclerview.widget.RecyclerView, event: MotionEvent) = Unit

    override fun onRequestDisallowInterceptTouchEvent(shouldDisallow: Boolean) = Unit
}
