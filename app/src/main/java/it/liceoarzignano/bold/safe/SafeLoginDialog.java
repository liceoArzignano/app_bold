package it.liceoarzignano.bold.safe;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import it.liceoarzignano.bold.R;

class SafeLoginDialog {
    private final Resources mRes;

    private MaterialDialog mDialog;

    private View mDialogView;
    private EditText mPasswordEditText;
    private TextView mHintTextView;

    /**
     * Log in dialog with password input and hints
     *
     * @param mContext    used to fetch resources
     * @param isFirstTime adapt ui for first login
     */
    @SuppressLint("InflateParams")
    SafeLoginDialog(Context mContext, final boolean isFirstTime) {
        mRes = mContext.getResources();

        LayoutInflater mInflater = (LayoutInflater)
                mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mDialogView = mInflater.inflate(R.layout.dialog_safe_unlock, null);

        mPasswordEditText = (EditText) mDialogView.findViewById(R.id.safe_dialog_input);
        mHintTextView = (TextView) mDialogView.findViewById(R.id.safe_dialog_hint_text);

        mHintTextView.setText(mRes.getString(isFirstTime ?
                R.string.safe_dialog_first_hint : R.string.safe_dialog_hint));

        if (isFirstTime) {
            mPasswordEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    String mInput = mPasswordEditText.getText().toString();
                    int mMessage;
                    if (!mInput.isEmpty()) {
                        if (mInput.length() < 8) {
                            mMessage = R.string.safe_dialog_first_hint_low;
                        } else if (mInput.matches(".*\\d.*") &&
                                mInput.matches(".*[a-zA-Z].*")) {
                            // Has at least 1 number, 1 letter and more than 8 chars
                            mMessage = R.string.safe_dialog_first_hint_good;
                        } else {
                            // More than 8 chars but only numbers / letters
                            mMessage = R.string.safe_dialog_first_hint_medium;
                        }

                        mHintTextView.setText(mRes.getString(mMessage));
                    }

                }
            });
        }
    }

    /**
     * @return dialog view
     */
    View getView() {
        return mDialogView;
    }

    /**
     * Password input
     */
    @NonNull
    String getInput() {
        return mPasswordEditText.getText().toString();
    }

    /**
     * Build dialog and show it
     *
     * @param mBuilder dialog builder
     */
    void build(MaterialDialog.Builder mBuilder) {
        mDialog = mBuilder.build();
        mDialog.show();
    }

    /**
     * Dismiss dialog but keep this alive
     */
    void dismiss() {
        mDialog.dismiss();
    }

    /**
     * Destroy all the unneeded things,
     * this way we'll make sure input
     * is not stored in memory anymore
     */
    void destroy() {
        mHintTextView = null;
        mPasswordEditText = null;
        mDialogView = null;
        mDialog = null;
    }
}
