package it.liceoarzignano.bold.ui.recyclerview

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView

import it.liceoarzignano.bold.R

class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val mTitle: TextView = view.findViewById(R.id.subheader_title)

    fun setTitle(title: String) {
        mTitle.text = title
    }
}
