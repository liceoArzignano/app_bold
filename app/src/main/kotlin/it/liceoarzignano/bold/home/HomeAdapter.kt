package it.liceoarzignano.bold.home

import android.content.Context
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView

import it.liceoarzignano.bold.R

class HomeAdapter(private val mContext: Context, private val mObjects: List<HomeCard>,
                  private val mShouldAnimate: Boolean) :
        RecyclerView.Adapter<HomeAdapter.HomeHolder>() {
    private var mLast = -1

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): HomeHolder =
            HomeHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_home,
                    parent, false))

    override fun onBindViewHolder(holder: HomeHolder, position: Int) {
        val obj = mObjects[position]
        holder.init(obj)
        if (mShouldAnimate && position > mLast) {
            holder.itemView.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_up))
        }
        mLast = holder.adapterPosition
    }

    override fun getItemCount(): Int = mObjects.size

    override fun onViewDetachedFromWindow(homeHolder: HomeHolder?) {
        super.onViewDetachedFromWindow(homeHolder)
        homeHolder!!.itemView.clearAnimation()
    }

    class HomeHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val mNameView: TextView = view.findViewById(R.id.home_item_name)
        private val mCardView: CardView = view.findViewById(R.id.home_item_card)
        private val mLayouts = arrayOf<LinearLayout>(
                view.findViewById(R.id.home_item_layout_0),
                view.findViewById(R.id.home_item_layout_1),
                view.findViewById(R.id.home_item_layout_2)
        )
        private val mTitles = arrayOf<TextView>(
                view.findViewById(R.id.home_item_title_0),
                view.findViewById(R.id.home_item_title_1),
                view.findViewById(R.id.home_item_title_2)
        )
        private val mSummary = arrayOf<TextView>(
                view.findViewById(R.id.home_item_sec_0),
                view.findViewById(R.id.home_item_sec_1),
                view.findViewById(R.id.home_item_sec_2)
        )

        internal fun init(obj: HomeCard) {
            val size = obj.size
            val name = obj.name
            val titles = obj.title
            val contents = obj.content

            for (i in 0 until size) {
                mLayouts[i].visibility = View.VISIBLE
                mSummary[i].text = contents[i]
                val title = titles[i]
                if (title.isNotBlank()) {
                    mTitles[i].text = title
                    mTitles[i].visibility = View.VISIBLE
                }
            }
            mNameView.text = name
            mCardView.setOnClickListener({ obj.doClickAction() })
        }
    }
}