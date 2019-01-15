package it.liceoarzignano.bold.marks

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import com.google.android.material.snackbar.Snackbar
import it.liceoarzignano.bold.R
import it.liceoarzignano.bold.settings.AppPrefs
import it.liceoarzignano.bold.ui.ActionsDialog
import it.liceoarzignano.bold.ui.CircularProgressBar
import it.liceoarzignano.bold.ui.recyclerview.RecyclerViewExt


class SubjectActivity : AppCompatActivity() {
    private lateinit var mCoordinator: androidx.coordinatorlayout.widget.CoordinatorLayout
    private lateinit var mProgressBar: CircularProgressBar
    private lateinit var mTextHint: TextView
    private lateinit var mNestedView: NestedScrollView

    private lateinit var mMarksHandler: MarksHandler
    private lateinit var mAdapter: SubjectAdapter

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
        toolbar.setNavigationOnClickListener { finish() }

        mCoordinator = findViewById(R.id.coordinator_layout)
        mNestedView = findViewById(R.id.subject_nested_view)
        mProgressBar = findViewById(R.id.subject_hint_bar)
        mTextHint = findViewById(R.id.subject_hint_text)
        val marksList = findViewById<RecyclerViewExt>(R.id.subject_list)

        mAdapter = SubjectAdapter(mMarksHandler.getFilteredMarks(mTitle, mFilter), this)
        marksList.adapter = mAdapter
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
        internal const val EXTRA_TITLE = "extra_subject_position"
        internal const val EXTRA_FILTER = "extra_subject_filter"
    }
}
