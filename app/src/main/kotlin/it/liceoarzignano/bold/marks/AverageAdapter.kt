package it.liceoarzignano.bold.marks

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import it.liceoarzignano.bold.R
import java.util.*

internal class AverageAdapter(context: Context, private var mResults: Array<String>?) :
        androidx.recyclerview.widget.RecyclerView.Adapter<AverageAdapter.AverageHolder>() {
    private val mHandler = MarksHandler.getInstance(context)

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): AverageAdapter.AverageHolder =
            AverageHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_average, parent, false))

    override fun onBindViewHolder(holder: AverageAdapter.AverageHolder, position: Int) =
        holder.setData(mResults!![position])

    override fun getItemCount(): Int = if (mResults != null) mResults!!.size else 0

    fun updateList(results: Array<String>) {
        mResults = results
        notifyDataSetChanged()
    }

    inner class AverageHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        private val mTitle: TextView = view.findViewById(R.id.row_avg_title)
        private val mValue: TextView = view.findViewById(R.id.row_avg_value)

        fun setData(result: String) {
            mTitle.text = result

            val value = mHandler.getAverage(result, 0)
            if (value < 6) {
                mValue.setTextColor(Color.RED)
            }
            mValue.text = String.format(Locale.ENGLISH, "%.2f", value)
        }
    }
}
