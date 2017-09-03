package it.liceoarzignano.bold.safe

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import it.liceoarzignano.bold.R

internal class SafeLoginDialog
@SuppressLint("InflateParams")
constructor(context: Context, isFirstTime: Boolean) {
    private var mDialog: MaterialDialog? = null
    var view: View
    private var mPasswordEditText: EditText
    private var mHintTextView: TextView
    private val mRes = context.resources

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view = inflater.inflate(R.layout.dialog_safe_unlock, null)
        mPasswordEditText = view.findViewById(R.id.safe_dialog_input)
        mHintTextView = view.findViewById(R.id.safe_dialog_hint_text)
        mHintTextView.text = mRes.getString(if (isFirstTime)
            R.string.safe_dialog_first_hint
        else
            R.string.safe_dialog_hint)

        if (isFirstTime) {
            mPasswordEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(charSequence: CharSequence, i: Int,
                                               i1: Int, i2: Int) = Unit

                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) =
                        Unit

                override fun afterTextChanged(editable: Editable) {
                    val input = mPasswordEditText.text.toString()
                    val message: Int
                    if (!input.isEmpty()) {
                        message = if (input.length < 8) {
                            R.string.safe_dialog_first_hint_low
                        } else if (input.matches(".*\\d.*".toRegex()) &&
                                input.matches(".*[a-zA-Z].*".toRegex())) {
                            // Has at least 1 number, 1 letter and more than 8 chars
                            R.string.safe_dialog_first_hint_good
                        } else {
                            // More than 8 chars but only numbers / letters
                            R.string.safe_dialog_first_hint_medium
                        }

                        mHintTextView.text = mRes.getString(message)
                    }

                }
            })
        }
    }

    val input: String get() = mPasswordEditText.text.toString()

    fun build(mBuilder: MaterialDialog.Builder) {
        mDialog = mBuilder.build()
        mDialog!!.show()
    }

    fun dismiss() = mDialog!!.dismiss()
}
