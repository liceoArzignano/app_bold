package it.liceoarzignano.bold.editor

import android.os.AsyncTask
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import it.liceoarzignano.bold.R

class HashtagsAdapter : RecyclerView.Adapter<HashtagsAdapter.HashtagViewHolder>() {
    var tags: List<String> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            HashtagViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_hashtag, parent, false))

    override fun onBindViewHolder(holder: HashtagViewHolder?, position: Int) {
        holder?.bind(tags[position])
    }

    override fun getItemCount() = tags.size

    fun update(new: String) {
        UpdateTask(new, tags, { list -> tags = list }, { result ->
            result?.dispatchUpdatesTo(this@HashtagsAdapter)})
    }

    fun getTags(): String {
        if (tags.isEmpty()) {
            return ""
        }

        val builder = StringBuilder()
        for (item in tags) {
            builder.append("$item,")
        }

        val hashtags = builder.toString()

        // Remove the last ","
        return hashtags.substring(0, hashtags.length - 1)
    }

    inner class HashtagViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val mTitle = view.findViewById<TextView>(R.id.row_hashtag_title)

        fun bind(string: String) {
            mTitle.text = string
        }
    }

    private class DiffHelper(private val old: List<String>, private val new: List<String>):
            DiffUtil.Callback() {

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                old[oldItemPosition] == new[newItemPosition]

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                old[oldItemPosition] == new[newItemPosition]

        override fun getNewListSize() = new.size

        override fun getOldListSize() = old.size
    }

    private class UpdateTask(private val new: String,
                             private val oldList: List<String>,
                             private val updateList: (List<String>) -> Unit,
                             private val onDone: (DiffUtil.DiffResult?) -> Unit) :
            AsyncTask<Unit, Unit, DiffUtil.DiffResult>() {
        override fun doInBackground(vararg p0: Unit?): DiffUtil.DiffResult {
            val newList = new.split(" ").filter { it.startsWith('#') && it.length > 1 }
            val diff = DiffUtil.calculateDiff(DiffHelper(oldList, newList))
            updateList(newList)
            return diff
        }

        override fun onPostExecute(result: DiffUtil.DiffResult?) = onDone(result)
    }
}
