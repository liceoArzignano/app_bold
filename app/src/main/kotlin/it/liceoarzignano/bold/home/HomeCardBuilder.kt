package it.liceoarzignano.bold.home

import java.util.*

class HomeCardBuilder {
    private val mTitleList = ArrayList<String>()
    private val mContentList = ArrayList<String>()
    private var mCounter: Int = 0
    private var mName = ""
    private var mClickListener: HomeCard.HomeCardClickListener? = null

    fun setName(name: String): HomeCardBuilder {
        this.mName = name
        return this
    }

    fun addEntry(title: String, content: String): HomeCardBuilder {
        if (mCounter > 2) {
            return this
        }

        mCounter++
        mTitleList.add(title)
        mContentList.add(content)
        return this
    }

    fun setOnClick(listener: HomeCard.HomeCardClickListener): HomeCardBuilder {
        mClickListener = listener
        return this
    }

    fun build(): HomeCard = HomeCard(mCounter, mName, mTitleList, mContentList, mClickListener)
}
