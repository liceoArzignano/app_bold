package it.liceoarzignano.bold.marks

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import it.liceoarzignano.bold.R
import it.liceoarzignano.bold.editor.EditorActivity
import it.liceoarzignano.bold.settings.AppPrefs
import it.liceoarzignano.bold.ui.recyclerview.DividerDecoration
import it.liceoarzignano.bold.ui.recyclerview.RecyclerClickListener
import it.liceoarzignano.bold.ui.recyclerview.RecyclerTouchListener
import it.liceoarzignano.bold.ui.recyclerview.RecyclerViewExt
import it.liceoarzignano.bold.utils.ContentUtils
import it.liceoarzignano.bold.utils.Time

class MarksActivity : AppCompatActivity() {
    lateinit private var mList: RecyclerViewExt
    lateinit private var mEmptyLayout: LinearLayout
    lateinit private var mFab: FloatingActionButton

    lateinit private var mAdapter: AverageAdapter
    lateinit private var mPrefs: AppPrefs
    private var mQuarter = 0
    private var isFirstEnabled = false
    private var isSecondEnabled = false

    override fun onCreate(savedInstance: Bundle?) {
        super.onCreate(savedInstance)

        setContentView(R.layout.activity_marks)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back)
        toolbar.setNavigationOnClickListener { _ -> finish() }

        mList = findViewById(R.id.marks_list)
        mEmptyLayout = findViewById(R.id.marks_empty_layout)

        mFab = findViewById(R.id.fab)
        mFab.setOnClickListener { _ ->
            startActivity(Intent(this, EditorActivity::class.java))
        }

        mPrefs = AppPrefs(baseContext)

        mList.addItemDecoration(DividerDecoration(this))
        mAdapter = AverageAdapter(this, ContentUtils.getAverageElements(this, mQuarter))
        mList.adapter = mAdapter

        Handler().postDelayed({ mFab.show() }, 400)
    }

    public override fun onResume() {
        super.onResume()

        refresh()
    }

    override fun finish() {
        mFab.hide()
        Handler().postDelayed({ super.finish() }, 170)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (!Time().isFirstQuarter(baseContext)) {
            menuInflater.inflate(R.menu.marks, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.menu_filter) {
            createFilterDialog()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun refresh() {
        val marks = ContentUtils.getAverageElements(this, mQuarter)

        if (marks.isEmpty()) {
            mList.visibility = View.GONE
            mEmptyLayout.visibility = View.VISIBLE
        } else {
            mEmptyLayout.visibility = View.GONE
            mList.visibility = View.VISIBLE

            val listener = object : RecyclerClickListener {
                override fun onClick(view: View, position: Int) {
                    val intent = Intent(baseContext, SubjectActivity::class.java)
                    intent.putExtra(SubjectActivity.EXTRA_TITLE, marks[position])
                    intent.putExtra(SubjectActivity.EXTRA_FILTER, mQuarter)
                    startActivity(intent)
                }
            }
            mList.addOnItemTouchListener(RecyclerTouchListener(this, listener))
            mAdapter.updateList(marks)
        }
    }

    private fun createFilterDialog() {
        MaterialDialog.Builder(this)
                .title(R.string.marks_menu_filter)
                .customView(filterView, false)
                .canceledOnTouchOutside(false)
                .autoDismiss(false)
                .positiveText(R.string.marks_filter_action)
                .onPositive { dialog, _ ->
                    if (isFirstEnabled || isSecondEnabled) {
                        mQuarter = if (isFirstEnabled) if (isSecondEnabled) 0 else 1 else 2
                        mPrefs.set(AppPrefs.KEY_QUARTER_SELECTOR, mQuarter)
                        dialog.dismiss()
                        refresh()
                    } else {
                        Toast.makeText(this, getString(R.string.marks_filter_error),
                                Toast.LENGTH_LONG).show()
                    }
                }
                .show()
    }

    private val filterView: View
        get() {
            val quarterVal = mPrefs.get(AppPrefs.KEY_QUARTER_SELECTOR, 0)
            isFirstEnabled = quarterVal < 2
            isSecondEnabled = quarterVal != 1

            val noBg = ColorDrawable(Color.TRANSPARENT)
            val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val group = findViewById<ViewGroup>(R.id.dialog_root)
            val view = inflater.inflate(R.layout.dialog_filter, group)
            val firstSel = view.findViewById<ImageView>(R.id.filter_first)
            val secondSel = view.findViewById<ImageView>(R.id.filter_second)

            firstSel.background = if (isFirstEnabled)
                ContextCompat.getDrawable(this, R.drawable.ic_filter_bg_enabled)
            else
                noBg
            secondSel.background = if (isSecondEnabled)
                ContextCompat.getDrawable(this, R.drawable.ic_filter_bg_enabled)
            else
                noBg

            firstSel.setOnClickListener { _ ->
                isFirstEnabled = !isFirstEnabled
                firstSel.background = if (isFirstEnabled)
                    ContextCompat.getDrawable(this, R.drawable.ic_filter_bg_enabled)
                else
                    noBg
            }
            secondSel.setOnClickListener { _ ->
                isSecondEnabled = !isSecondEnabled
                secondSel.background = if (isSecondEnabled)
                    ContextCompat.getDrawable(this, R.drawable.ic_filter_bg_enabled)
                else
                    noBg
            }

            return view
        }
}
