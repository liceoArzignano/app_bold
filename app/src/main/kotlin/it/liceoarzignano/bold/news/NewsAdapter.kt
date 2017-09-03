package it.liceoarzignano.bold.news

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.zhukic.sectionedrecyclerview.SectionedRecyclerViewAdapter
import it.liceoarzignano.bold.R
import it.liceoarzignano.bold.ui.recyclerview.HeaderViewHolder
import it.liceoarzignano.bold.utils.HelpToast
import it.liceoarzignano.bold.utils.Time

internal class NewsAdapter(private var mNewsList: List<News>, private val mContext: Context) :
        SectionedRecyclerViewAdapter<HeaderViewHolder, NewsAdapter.NewsHolder>() {

    override fun onCreateItemViewHolder(parent: ViewGroup, type: Int): NewsHolder =
            NewsHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_news, parent, false))


    override fun onBindItemViewHolder(holder: NewsHolder, position: Int) =
            holder.setData(mNewsList[position])


    override fun onCreateSubheaderViewHolder(parent: ViewGroup, type: Int): HeaderViewHolder =
            HeaderViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_subheader, parent, false))


    override fun onBindSubheaderViewHolder(holder: HeaderViewHolder, position: Int) {
        val time = Time(mNewsList[position].date)
        val diff = time.diff(Time())

        val title = when (diff) {
            -1 -> mContext.getString(R.string.events_time_yesterday)
            0 -> mContext.getString(R.string.events_time_today)
            1 -> mContext.getString(R.string.events_time_tomorrow)
            else -> time.asString(mContext)
        }

        holder.setTitle(title)
    }

    override fun getItemSize(): Int = mNewsList.size

    override fun onPlaceSubheaderBetweenItems(itemPosition: Int): Boolean {
        val a = Time(mNewsList[itemPosition].date)
        val b = Time(mNewsList[itemPosition + 1].date)

        return a.diff(b) >= 1
    }


    fun updateList(list: List<News>) {
        mNewsList = list
        notifyDataChanged()
    }

    internal inner class NewsHolder(private val mView: View) : RecyclerView.ViewHolder(mView) {
        private val mTitle: TextView = mView.findViewById(R.id.row_news_title)
        private val mMessage: TextView = mView.findViewById(R.id.row_news_message)

        fun setData(news: News) {
            mTitle.text = news.title
            mMessage.text = news.description

            val url = news.url
            mView.setOnClickListener { _ ->
                if (!url.isEmpty()) {
                    (mContext as NewsListActivity).showUrl(url)
                }
                HelpToast(mContext, R.string.intro_toast_news_long_press,
                        HelpToast.KEY_NEWS_LONG_PRESS)
            }

            mView.setOnLongClickListener { _ -> (mContext as NewsListActivity).newsActions(news) }
        }
    }
}
