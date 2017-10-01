package it.liceoarzignano.bold.home

class HomeCard internal constructor(val size: Int, val name: String,
                                    val title: List<String>, val content: List<String>,
                                    private val mClickListner: HomeCardClickListener?) {

    internal fun doClickAction() = mClickListner?.onClick()

    interface HomeCardClickListener {
        fun onClick()
    }
}
