package it.liceoarzignano.bold.home

import android.view.View

class HomeCard internal constructor(val size: Int, val name: String,
                                    val title: List<String>, val content: List<String>,
                                    private val mClickListner: HomeCardClickListener?) {

    internal fun doClickAction(view: View) = mClickListner?.onClick(view)

    interface HomeCardClickListener {
        fun onClick(view: View)
    }
}
