package it.liceoarzignano.bold.marks

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v4.widget.NestedScrollView
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.widget.TextView
import it.liceoarzignano.bold.R
import it.liceoarzignano.bold.settings.AppPrefs
import it.liceoarzignano.bold.ui.ActionsDialog
import it.liceoarzignano.bold.ui.CircularProgressBar
import it.liceoarzignano.bold.ui.recyclerview.RecyclerViewExt
import it.liceoarzignano.bold.utils.SystemUtils
import it.liceoarzignano.bold.utils.UiUtils


class SubjectActivity : AppCompatActivity() {
    lateinit private var mCoordinator: CoordinatorLayout
    lateinit private var mProgressBar: CircularProgressBar
    lateinit private var mTextHint: TextView
    lateinit private var mNestedView: NestedScrollView

    lateinit private var mMarksHandler: MarksHandler
    lateinit private var mAdapter: SubjectAdapter

    private var mTitle = ""
    private var mFilter: Int = 0

    override fun onCreate(savedInstance: Bundle?) {
        super.onCreate(savedInstance)

        setContentView(R.layout.activity_subject)

        mMarksHandler = MarksHandler.getInstance(baseContext)

        val callingIntent = intent
        mTitle = callingIntent.getStringExtra(EXTRA_TITLE)
        mFilter = callingIntent.getIntExtra(EXTRA_FILTER, 0)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = mTitle
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back)
        toolbar.setNavigationOnClickListener { _ -> finish() }

        mCoordinator = findViewById(R.id.coordinator_layout)
        mNestedView = findViewById(R.id.subject_nested_view)
        mProgressBar = findViewById(R.id.subject_hint_bar)
        mTextHint = findViewById(R.id.subject_hint_text)
        val marksList = findViewById<RecyclerViewExt>(R.id.subject_list)

        mAdapter = SubjectAdapter(mMarksHandler.getFilteredMarks(mTitle, mFilter), this)
        marksList.adapter = mAdapter

        if (SystemUtils.isNotLegacy) {
            mNestedView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int,
                                                    _: Int, _: Int ->
                toolbar.elevation = UiUtils.dpToPx(resources, if (scrollY == 0)
                    0f else
                    resources.getDimension(R.dimen.scroll_toolbar_elevation))
            }
        }

        mProgressBar.requestFocus()
    }

    public override fun onResume() {
        super.onResume()

        refresh()
    }

    private fun refresh() {
        val marks = mMarksHandler.getFilteredMarks(mTitle, mFilter)
        mAdapter.updateList(marks)
        // Scroll to top
        mNestedView.scrollTo(0, 0)

        setHint(mMarksHandler.getAverage(mTitle, mFilter),
                mMarksHandler.whatShouldIGet(mTitle, mFilter))
    }

    private fun setHint(average: Double, expected: Double) {
        mTextHint.text = String.format("%1\$s %2\$s", getString(R.string.hint_content_common),
                String.format(getString(if (expected < 6)
                    R.string.hint_content_above
                else
                    R.string.hint_content_under),
                        expected))
        val color = when {
            average < 5.5 -> R.color.red
            average < 6.0 -> R.color.yellow
            else -> R.color.green
        }
        mProgressBar.setProgressColor(ContextCompat.getColor(baseContext, color))
        mProgressBar.setProgress(average)
    }

    internal fun marksAction(mark: Mark): Boolean {
        val dialog = ActionsDialog(this, true, true, mark.id)
        dialog.setOnActionsListener(object : ActionsDialog.OnActionsDialogListener {
            override fun onShare() {
                val message = if (AppPrefs(baseContext).get(AppPrefs.KEY_IS_TEACHER, false))
                    getString(R.string.marks_share_teacher, mark.subject, mark.value / 100)
                else
                    getString(R.string.marks_share_student, mark.value / 100, mark.subject)

                startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND)
                        .setType("text/plain")
                        .putExtra(Intent.EXTRA_TEXT, message),
                        getString(R.string.share_title)))
            }

            override fun onDelete() {
                mMarksHandler.delete(mark.id)
                Snackbar.make(mCoordinator, getString(R.string.actions_removed), Snackbar.LENGTH_LONG)
                        .show()
                mAdapter.notifyDataSetChanged()
            }
        })
        dialog.show()

        return true
    }

    companion object {
        internal val EXTRA_TITLE = "extra_subject_position"
        internal val EXTRA_FILTER = "extra_subject_filter"
    }
}
