package it.liceoarzignano.bold.marks

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import it.liceoarzignano.bold.R
import it.liceoarzignano.bold.utils.Time
import it.liceoarzignano.bold.utils.UiUtils
import java.util.*

internal class SubjectAdapter(private var mMarks: List<Mark>, private val mContext: Context) :
        RecyclerView.Adapter<SubjectAdapter.SubjectHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): SubjectHolder =
            SubjectHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_mark, parent, false))

    override fun onBindViewHolder(holder: SubjectHolder, position: Int) =
            holder.setData(mMarks[position])

    override fun getItemCount(): Int = mMarks.size

    fun updateList(marks: List<Mark>) {
        mMarks = marks
        notifyDataSetChanged()
    }

    internal inner class SubjectHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val mView: View = view.findViewById(R.id.row_mark_root)
        private val mValue: TextView = view.findViewById(R.id.row_mark_value)
        private val mDate: TextView = view.findViewById(R.id.row_mark_date)
        private val mSummary: TextView = view.findViewById(R.id.row_mark_summary)
        private val mExpand: ImageView = view.findViewById(R.id.row_mark_expand)
        private val mActions: View = view.findViewById(R.id.row_mark_actions)
        private val mShare: ImageButton = view.findViewById(R.id.row_mark_share)
        private val mEdit: ImageButton = view.findViewById(R.id.row_mark_edit)
        private val mDelete: ImageButton = view.findViewById(R.id.row_mark_delete)

        fun setData(mark: Mark) {
            mDate.text = Time(mark.date).asString(mContext)

            val value = mark.value.toDouble() / 100
            if (value < 6) {
                mValue.setTextColor(Color.RED)
            }
            mValue.text = String.format(Locale.ENGLISH, "%.2f", value)

            val summary = mark.description
            val hasSummary = !summary.isEmpty()
            if (hasSummary) {
                mSummary.text = summary
            }

            if (mContext is SubjectActivity) {
                mShare.setOnClickListener { _ -> mContext.shareItem(mShare, mark) }
                mEdit.setOnClickListener { _ -> mContext.editItem(mEdit, mark) }
                mDelete.setOnClickListener { _ -> mContext.deleteItem(mDelete, mark,
                        layoutPosition) }
            }

            mView.setOnClickListener { _ ->
                val shouldExpand = mActions.visibility == View.GONE
                val elevation = UiUtils.dpToPx(mContext.resources, 4f)
                val animator = ValueAnimator.ofFloat(if (shouldExpand) 0F else elevation,
                        if (shouldExpand) elevation else 0F)
                animator.duration = 350
                animator.addUpdateListener { valueAnimator ->
                    val progress = valueAnimator.animatedValue as Float
                    ViewCompat.setElevation(mView, progress)
                    mExpand.rotation = progress * 180 / elevation
                    mSummary.alpha = progress / elevation
                    mActions.alpha = progress / elevation
                }
                animator.start()

                Handler().postDelayed({
                    if (hasSummary) {
                        mSummary.visibility = if (shouldExpand) View.VISIBLE else View.GONE
                    }
                    mActions.visibility = if (shouldExpand) View.VISIBLE else View.GONE
                    mView.setBackgroundColor(ContextCompat.getColor(mContext,
                            if (shouldExpand) R.color.cardview_light_background else R.color.white))
                }, 250)
            }
        }
    }
}
