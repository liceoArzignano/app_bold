package it.liceoarzignano.bold.marks

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import it.liceoarzignano.bold.R
import it.liceoarzignano.bold.utils.HelpToast
import it.liceoarzignano.bold.utils.Time
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

        fun setData(mark: Mark) {
            mDate.text = Time(mark.date).asString(mContext)

            val value = mark.value.toDouble() / 100
            val context = mView.context
            mValue.setTextColor(ContextCompat.getColor(context,
                    if  (value < 6) R.color.red
                    else R.color.main_inverted))

            mValue.text = String.format(Locale.ENGLISH, "%.2f", value)

            val summary = mark.description
            val hasSummary = !summary.isEmpty()
            if (hasSummary) {
                mSummary.text = summary
            }

            mView.setOnClickListener { _ ->
                if (hasSummary) {
                    mSummary.visibility =
                            if (mSummary.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                }
                HelpToast(mContext, HelpToast.KEY_MARK_LONG_PRESS)
            }

            mView.setOnLongClickListener { (mContext as SubjectActivity).marksAction(mark) }
        }
    }
}
