package it.liceoarzignano.bold.ui.recyclerview

import android.view.View
import android.widget.TextView
import it.liceoarzignano.bold.R

class HeaderViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
    private val mTitle: TextView = view.findViewById(R.id.subheader_title)

    fun setTitle(title: String) {
        mTitle.text = title
    }
}
