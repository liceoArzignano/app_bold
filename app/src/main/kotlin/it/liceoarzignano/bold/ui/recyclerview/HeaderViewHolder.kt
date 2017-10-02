package it.liceoarzignano.bold.ui.recyclerview

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView

import it.liceoarzignano.bold.R

class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val mTitle: TextView = view.findViewById(R.id.subheader_title)
    private val mDescription: TextView = view.findViewById(R.id.subheader_description)

    fun bind(data: Pair<String, String>) {
        mTitle.text = data.first
        mDescription.text = data.second
    }
}
