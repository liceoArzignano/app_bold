package it.liceoarzignano.bold.poster

import android.app.DatePickerDialog
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.google.firebase.database.*
import it.liceoarzignano.bold.R
import it.liceoarzignano.bold.ui.recyclerview.RecyclerViewExt
import it.liceoarzignano.bold.utils.ContentUtils
import it.liceoarzignano.bold.utils.SystemUtils
import it.liceoarzignano.bold.utils.Time
import it.liceoarzignano.bold.utils.UiUtils
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import java.util.*


class PosterActivity : AppCompatActivity() {
    private lateinit var mCoordinator: CoordinatorLayout
    private lateinit var mHeaderText: TextView
    private lateinit var mList: RecyclerViewExt
    private lateinit var mEmptyLayout: LinearLayout
    private lateinit var mEmptyText: TextView
    private lateinit var mProgressBar: MaterialProgressBar

    private lateinit var mAdapter: PosterAdapter
    private lateinit var mDatabase: DatabaseReference
    private lateinit var mDateSetListener: DatePickerDialog.OnDateSetListener

    private val mDataList: MutableList<Post> = arrayListOf()
    private var mSelectedDay = Time(1)
    private var mVisibility: Visibility = Visibility.LOADING

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_poster)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back)
        toolbar.setNavigationOnClickListener { _ -> finish() }
        if (SystemUtils.isNotLegacy) {
            toolbar.elevation = UiUtils.dpToPx(resources, 6f)
        }

        mCoordinator = findViewById(R.id.coordinator_layout)
        mHeaderText = findViewById(R.id.poster_seeker_day)
        val headerLayout = findViewById<LinearLayout>(R.id.poster_seeker_layout)
        mList = findViewById(R.id.poster_list)
        mEmptyLayout = findViewById(R.id.poster_empty_layout)
        mEmptyText = findViewById(R.id.poster_empty_text)
        mProgressBar = findViewById(R.id.poster_loading_bar)

        mAdapter = PosterAdapter()
        mList.adapter = mAdapter

        headerLayout.setOnClickListener { showDatePicker() }
        mDateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            mSelectedDay = Time(year, month, dayOfMonth)
            fetch()
        }

        mDatabase = FirebaseDatabase.getInstance().reference
        fetch()
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        calendar.time = mSelectedDay
        DatePickerDialog(this, mDateSetListener, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                .show()
    }

    private fun fetch() {
        setVisibility(Visibility.LOADING)
        val queryPath = mSelectedDay.toString().split("-")
        if (queryPath.size != 3) {
            return
        }

        mHeaderText.text = ContentUtils.getHeaderTitleSimple(resources, mSelectedDay)

        val query = mDatabase.child("poster")
                .child(queryPath[0])
                .child(queryPath[1])
                .child(queryPath[2])
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) = Unit
            override fun onDataChange(postSnapshot: DataSnapshot?) {
                if (postSnapshot == null) {
                    return
                }

                Handler().postDelayed({ onDaySelected(postSnapshot) }, 2000)
            }
        })
    }

    private fun onDaySelected(postSnapshot: DataSnapshot) {
        mDataList.clear()
        for (item in postSnapshot.children) {
            val post = Post()
            post.name = item.key
            for (child in item.children) {
                post.data.add(child.getValue(Post.Item::class.java) ?: Post.Item())
            }
            mDataList.add(post)
        }

        mAdapter.updateList(mDataList)
        if (mDataList.isEmpty()) {
            setVisibility(Visibility.EMPTY)
            if (SystemUtils.hasNoInternetConnection(this)) {
                Snackbar.make(mCoordinator, getString(R.string.poster_connection_error),
                        Snackbar.LENGTH_LONG).show()
            }
        } else {
            setVisibility(Visibility.LIST)
        }
    }


    private fun setVisibility(visibility: Visibility) {
        mVisibility = visibility
        when (visibility) {
            Visibility.LIST -> {
                mEmptyLayout.visibility = View.GONE
                mList.visibility = View.VISIBLE
                mProgressBar.visibility = View.GONE
            }
            Visibility.LOADING,
            Visibility.EMPTY -> {
                mList.visibility = View.GONE
                mEmptyLayout.visibility = View.VISIBLE
            }
        }

        if (visibility != Visibility.LIST) {
            val isLoading = visibility == Visibility.LOADING
            mProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            mEmptyText.text = getString(if (isLoading)
                R.string.poster_loading else R.string.poster_empty)
        }
    }


    enum class Visibility {
        LIST,
        LOADING,
        EMPTY
    }
}
