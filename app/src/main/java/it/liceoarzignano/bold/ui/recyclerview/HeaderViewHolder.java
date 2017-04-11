package it.liceoarzignano.bold.ui.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import it.liceoarzignano.bold.R;

public class HeaderViewHolder extends RecyclerView.ViewHolder {

    private final TextView mTitle;

    public HeaderViewHolder(View view) {
        super(view);
        mTitle = (TextView) view.findViewById(R.id.subheader_title);
    }

    public void setTitle(String title) {
        mTitle.setText(title);
    }
}
