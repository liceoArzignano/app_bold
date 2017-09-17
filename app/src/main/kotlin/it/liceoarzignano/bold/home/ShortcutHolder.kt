package it.liceoarzignano.bold.home

import android.content.Context
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import it.liceoarzignano.bold.R
import it.liceoarzignano.bold.utils.UiUtils

class ShortcutHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val mCardView: CardView = view.findViewById(R.id.home_item_shortcut_card)
    private val mIcon: ImageView = view.findViewById(R.id.home_item_shortcut_icon)
    private val mTitle: TextView = view.findViewById(R.id.home_item_shortcut_title)

    fun bind(context: Context, @StringRes title: Int, @DrawableRes icon: Int,
             @ColorRes color: Int, listener: ShortcutListener) {
        val position = adapterPosition
        mTitle.text = context.getString(title)
        mIcon.setImageResource(icon)
        mCardView.setCardBackgroundColor(ContextCompat.getColor(context, color))
        mCardView.setOnClickListener { _ -> listener.onClick(position) }

        if (position != 0 && position != 4) {
            return
        }

        // Fix margins to match the default grid
        val r = context.resources
        val params = mCardView.layoutParams as RecyclerView.LayoutParams
        val defaultMargin = params.topMargin
        val extraMargin = UiUtils.dpToPx(r, 16f).toInt()
        val isFirst = position == 0
        params.setMargins(if (isFirst) extraMargin else defaultMargin, defaultMargin,
                if (isFirst) defaultMargin else extraMargin, defaultMargin)
    }

    interface ShortcutListener {
        fun onClick(position: Int)
    }
}