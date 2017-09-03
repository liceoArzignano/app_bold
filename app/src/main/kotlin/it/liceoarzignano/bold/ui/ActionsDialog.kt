package it.liceoarzignano.bold.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.support.design.widget.BottomSheetDialog
import android.view.LayoutInflater
import android.view.View
import it.liceoarzignano.bold.R
import it.liceoarzignano.bold.editor.EditorActivity

class ActionsDialog @SuppressLint("InflateParams")
constructor(context: Context, isEvent: Boolean, id: Long) {
    private val mDialog = BottomSheetDialog(context)
    private var mListener: OnActionsDialogListener? = null

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.dialog_actions, null)

        val share = view.findViewById<View>(R.id.actions_dialog_share)
        val remove = view.findViewById<View>(R.id.actions_dialog_remove)
        val edit = view.findViewById<View>(R.id.actions_dialog_edit)
        val toEvent = view.findViewById<View>(R.id.actions_dialog_to_event)

        if (isEvent) {
            edit.visibility = View.VISIBLE
        } else {
            toEvent.visibility = View.VISIBLE
        }

        share.setOnClickListener { _ ->
            mDialog.hide()
            mListener!!.onShare()
        }

        remove.setOnClickListener { _ ->
            mDialog.hide()
            mListener!!.onDelete()
        }

        edit.setOnClickListener { _ ->
            mDialog.hide()
            context.startActivity(Intent(context, EditorActivity::class.java)
                    .putExtra(EditorActivity.EXTRA_IS_MARK, false)
                    .putExtra(EditorActivity.EXTRA_ID, id))
        }

        toEvent.setOnClickListener { _ ->
            mDialog.hide()
            context.startActivity(Intent(context, EditorActivity::class.java)
                    .putExtra(EditorActivity.EXTRA_IS_NEWS, true)
                    .putExtra(EditorActivity.EXTRA_ID, id))
        }

        mDialog.setContentView(view)
    }

    fun setOnActionsListener(listener: OnActionsDialogListener) {
        mListener = listener
    }

    fun show() = mDialog.show()

    interface OnActionsDialogListener {
        fun onShare()
        fun onDelete()
    }
}
