package it.liceoarzignano.bold.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import it.liceoarzignano.bold.MainActivity
import it.liceoarzignano.bold.R

class ShortcutAdapter(activity: MainActivity) : androidx.recyclerview.widget.RecyclerView.Adapter<ShortcutHolder>() {

    private val mContext = activity.baseContext
    private val mListener = object : ShortcutHolder.ShortcutListener {
        override fun onClick(position: Int) = activity.showUrl(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): ShortcutHolder {
        val item = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_home_shortcut, parent, false)
        return ShortcutHolder(item)
    }

    override fun onBindViewHolder(holder: ShortcutHolder, position: Int) =
            holder.bind(mContext, TITLES[position], ICONS[position], COLORS[position], mListener)

    override fun getItemCount(): Int = TITLES.size

    companion object {
        @StringRes
        private val TITLES = intArrayOf(R.string.shortcut_site, R.string.shortcut_register,
                R.string.shortcut_moodle, R.string.shortcut_copybook, R.string.shortcut_teacherzone)
        @DrawableRes
        private val ICONS = intArrayOf(R.drawable.ic_website, R.drawable.ic_register,
                R.drawable.ic_moodle, R.drawable.ic_copybook, R.drawable.ic_teacherzone)
        @ColorRes
        private val COLORS = intArrayOf(R.color.shortcut_website, R.color.shortcut_register,
                R.color.shortcut_moodle, R.color.shortcut_copybook, R.color.shortcut_teacherzone)
    }
}