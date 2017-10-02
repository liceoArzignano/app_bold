package it.liceoarzignano.bold.events

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.zhukic.sectionedrecyclerview.SectionedRecyclerViewAdapter
import it.liceoarzignano.bold.R
import it.liceoarzignano.bold.ui.recyclerview.HeaderViewHolder
import it.liceoarzignano.bold.utils.ContentUtils
import it.liceoarzignano.bold.utils.HelpToast
import it.liceoarzignano.bold.utils.Time

internal class EventsAdapter(private var mEvents: List<Event>, private val mContext: Context) :
        SectionedRecyclerViewAdapter<HeaderViewHolder, EventsAdapter.EventHolder>() {

    override fun onCreateItemViewHolder(parent: ViewGroup, type: Int): EventHolder =
            EventHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_event, parent, false))

    override fun onBindItemViewHolder(holder: EventHolder, position: Int) =
            holder.setData(mEvents[position])

    override fun onCreateSubheaderViewHolder(parent: ViewGroup, type: Int): HeaderViewHolder =
            HeaderViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_subheader, parent, false))

    override fun onBindSubheaderViewHolder(holder: HeaderViewHolder, position: Int) {
        val time = Time(mEvents[position].date)
        holder.bind(ContentUtils.getHeaderTitle(mContext.resources, time))
    }

    override fun getItemSize(): Int = mEvents.size

    override fun onPlaceSubheaderBetweenItems(itemPosition: Int): Boolean {
        val a = Time(mEvents[itemPosition].date)
        val b = Time(mEvents[itemPosition + 1].date)

        return a.diff(b) >= 1
    }

    fun updateList(newList: List<Event>) {
        mEvents = newList
        notifyDataChanged()
    }

    internal inner class EventHolder(private val mView: View) : RecyclerView.ViewHolder(mView) {
        private val mTitle: TextView = mView.findViewById(R.id.row_event_title)
        private val mValue: TextView = mView.findViewById(R.id.row_event_value)
        private val mTag: TextView = mView.findViewById(R.id.row_event_tag)

        fun setData(event: Event) {
            mTitle.text = event.title

            if (!TextUtils.isEmpty(event.description)) {
                mValue.text = event.description
                mValue.visibility = View.VISIBLE
            }

            mTag.text = ContentUtils.eventCategoryToString(mContext, event.category)
            mView.setOnClickListener { _ ->
                mValue.maxLines =
                        if (mValue.maxLines == 1)
                            Integer.MAX_VALUE
                        else
                            1
                HelpToast(mContext, HelpToast.KEY_EVENT_LONG_PRESS)
            }
            mView.setOnLongClickListener { _ ->
                (mContext as EventListActivity).eventActions(event)
            }
        }
    }
}
