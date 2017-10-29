package it.liceoarzignano.bold.safe

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.support.annotation.StringRes
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import it.liceoarzignano.bold.R

@SuppressLint("InflateParams")
internal class SafeLoginDialog constructor(context: Context, private val mFirstTime: Boolean,
                                           hasFingerprint: Boolean,
                                           startFpListener: (calback: Callback,
                                                             ImageView) -> Unit) {
    private var mDialog: MaterialDialog? = null
    private var mView: View
    private var mPasswordEditText: EditText
    private var mFingerprintindicator: ImageView
    private var mHintTextView: TextView
    private val mRes = context.resources

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        mView = inflater.inflate(R.layout.dialog_safe_unlock, null)
        mPasswordEditText = mView.findViewById(R.id.safe_dialog_input)
        mFingerprintindicator = mView.findViewById(R.id.safe_dialog_fingerprint_auth)
        mHintTextView = mView.findViewById(R.id.safe_dialog_hint_text)
        mHintTextView.text = mRes.getString(when {
                    mFirstTime -> R.string.safe_dialog_first_hint
                    hasFingerprint -> R.string.safe_dialog_hint_fingerprint
                    else -> R.string.safe_dialog_hint
                })

        if (mFirstTime) {
            mPasswordEditText.addTextChangedListener(listener)
        } else if (hasFingerprint) {
            mFingerprintindicator.visibility = View.VISIBLE
            startFpListener(object : Callback {
                override fun dismiss() {
                    this@SafeLoginDialog.dismiss()
                }
            }, mFingerprintindicator)
        }
    }

    val input: String get() = mPasswordEditText.text.toString()

    private val listener: TextWatcher
        get() = object : TextWatcher {
            override fun beforeTextChanged(c: CharSequence, i: Int, i1: Int, i2: Int) = Unit
            override fun onTextChanged(c: CharSequence, i: Int, i1: Int, i2: Int) = Unit

            override fun afterTextChanged(e: Editable) {
                val input = mPasswordEditText.text.toString()
                @StringRes val message: Int

                if (input.isEmpty()) {
                    return
                }

                message = when {
                    input.length < 8 -> R.string.safe_dialog_first_hint_low
                    // Has at least 1 number, 1 letter and more than 8 chars
                    isPasswordGood(input) -> R.string.safe_dialog_first_hint_good
                    // More than 8 chars but only numbers / letters
                    else -> R.string.safe_dialog_first_hint_medium
                }
                mHintTextView.text = mRes.getString(message)
            }
        }

    private fun isPasswordGood(input: String) =
            input.matches(".*\\d.*".toRegex()) && input.matches(".*[a-zA-Z].*".toRegex())

    fun build(activity: Activity, onPositive: (SafeLoginDialog) -> Unit, onNegative: () -> Unit) {
        val builder = MaterialDialog.Builder(activity)
                .customView(mView, false)
                .canceledOnTouchOutside(false)
                .positiveText(
                        if (mFirstTime) R.string.safe_dialog_first_positive
                        else R.string.safe_dialog_positive)
                .onPositive { _, _ -> onPositive(this) }
                .negativeText(android.R.string.cancel)
                .onNegative { _, _ -> onNegative() }

        mDialog = builder.build()
        mDialog?.show()
    }

    fun dismiss() = mDialog?.dismiss()

    internal interface Callback {
        fun dismiss()
    }
}
