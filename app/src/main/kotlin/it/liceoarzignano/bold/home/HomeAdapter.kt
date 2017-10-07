package it.liceoarzignano.bold.home

import android.content.Context
import android.support.v7.widget.AppCompatButton
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import it.liceoarzignano.bold.R

class HomeAdapter(private val mContext: Context, private val mCards: MutableList<HomeCard>,
                  private val mShouldAnimate: Boolean) :
        RecyclerView.Adapter<HomeAdapter.HomeHolder>() {
    private var mLast = -1

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): HomeHolder =
            HomeHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_home,
                    parent, false))

    override fun onBindViewHolder(holder: HomeHolder, position: Int) {
        val obj = mCards[position]
        holder.init(obj)
        if (mShouldAnimate && position > mLast) {
            holder.itemView.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_up))
        }
        mLast = holder.adapterPosition
    }

    override fun getItemCount(): Int = mCards.size

    fun update(card: HomeCard) {
        val updatePosition = mCards.indexOfFirst { it -> it.type == card.type }

        if (updatePosition == -1) {
            add(card)
            return
        }

        mCards[updatePosition] = card
        notifyItemChanged(updatePosition)
    }

    fun add(card: HomeCard) {
        mCards.add(card)
        notifyItemInserted(itemCount)
    }

    fun remove(type: HomeCard.CardType) {
        for ((index, card) in mCards.withIndex()) {
            if (card.type == type) {
                mCards.removeAt(index)
                notifyItemRemoved(index)
                break
            }
        }
    }



    override fun onViewDetachedFromWindow(homeHolder: HomeHolder?) {
        super.onViewDetachedFromWindow(homeHolder)
        homeHolder!!.itemView.clearAnimation()
    }

    class HomeHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val mCardView = view.findViewById<View>(R.id.home_item_card)
        private val mNameView = view.findViewById<TextView>(R.id.home_item_name)
        private val mTextView = view.findViewById<TextView>(R.id.home_item_text)
        private val mActionView = view.findViewById<AppCompatButton>(R.id.home_item_action)

        internal fun init(obj: HomeCard) {
            mNameView.text = obj.title
            mTextView.text = obj.content
            mCardView.setOnClickListener({ obj.onCardClick() })

            val actionText = obj.action
            if (actionText.isNotBlank()) {
                mActionView.text = actionText
                mActionView.setOnClickListener({ obj.onActionClick() })
                mActionView.visibility = View.VISIBLE
            }
        }
    }
}