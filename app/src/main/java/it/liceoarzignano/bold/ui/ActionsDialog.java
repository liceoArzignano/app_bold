package it.liceoarzignano.bold.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.BottomSheetDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.google.firebase.analytics.FirebaseAnalytics;

import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.editor.EditorActivity;
import it.liceoarzignano.bold.firebase.BoldAnalytics;

public class ActionsDialog {
    private final BottomSheetDialog mDialog;

    private OnActionsDialogListener mListener;

    @SuppressLint("InflateParams")
    public ActionsDialog(Context context, boolean isEvent, long id) {

        mDialog = new BottomSheetDialog(context);
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_actions, null);

        View share = view.findViewById(R.id.actions_dialog_share);
        View remove = view.findViewById(R.id.actions_dialog_remove);
        View edit = view.findViewById(R.id.actions_dialog_edit);
        View toEvent = view.findViewById(R.id.actions_dialog_to_event);

        if (isEvent) {
            edit.setVisibility(View.VISIBLE);
        } else {
            toEvent.setVisibility(View.VISIBLE);
        }

        share.setOnClickListener(v -> {
            new BoldAnalytics(context).log(FirebaseAnalytics.Event.VIEW_ITEM, "Share");
            mDialog.hide();
            mListener.onShare();
        });

        remove.setOnClickListener(v -> {
            new BoldAnalytics(context).log(FirebaseAnalytics.Event.VIEW_ITEM, "Remove");
            mDialog.hide();
            mListener.onDelete();
        });

        edit.setOnClickListener(v -> {
            new BoldAnalytics(context).log(FirebaseAnalytics.Event.VIEW_ITEM, "Edit");
            mDialog.hide();
            context.startActivity(new Intent(context, EditorActivity.class)
                    .putExtra(EditorActivity.EXTRA_IS_MARK, false)
                    .putExtra(EditorActivity.EXTRA_ID, id));
        });

        toEvent.setOnClickListener(v -> {
            new BoldAnalytics(context).log(FirebaseAnalytics.Event.VIEW_ITEM, "Convert");
            mDialog.hide();
            context.startActivity(new Intent(context, EditorActivity.class)
                    .putExtra(EditorActivity.EXTRA_IS_NEWS, true)
                    .putExtra(EditorActivity.EXTRA_ID, id));
        });

        mDialog.setContentView(view);
    }

    public void setOnActionsListener(OnActionsDialogListener listener) {
        mListener = listener;
    }

    public void show() {
        mDialog.show();
    }

    public interface OnActionsDialogListener {
        void onShare();
        void onDelete();
    }
}
