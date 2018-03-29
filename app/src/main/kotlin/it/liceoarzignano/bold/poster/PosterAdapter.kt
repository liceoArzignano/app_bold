package it.liceoarzignano.bold.poster

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import it.liceoarzignano.bold.R

class PosterAdapter : RecyclerView.Adapter<PosterAdapter.PosterHolder>() {
    private var mList: List<Post> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            PosterHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_poster, parent, false))

    override fun onBindViewHolder(holder: PosterHolder, position: Int) {
        holder?.bind(mList[position])
    }

    override fun getItemCount() = mList.size

    fun updateList(list: List<Post>) {
        mList = list
        notifyDataSetChanged()
    }

    inner class PosterHolder(view: View): RecyclerView.ViewHolder(view) {
        private val mName = view.findViewById<TextView>(R.id.row_poster_name)
        private val mContent = view.findViewById<TextView>(R.id.row_poster_content)

        fun bind(post: Post) {
            mName.text = post.name

            val builder = StringBuilder()
            for (item in post.data) {
                if (item.hour.isBlank() || item.location.isBlank()) {
                    continue
                }

                builder.append("%1\$s. %2\$s: %3\$s"
                        .format(item.hour, item.location, item.replacer))
                if (item.flags.isNotBlank()) {
                    builder.append(" (%1\$s)".format(item.flags))
                }
                builder.append('\n')
            }

            mContent.text = builder.toString()
        }
    }
}