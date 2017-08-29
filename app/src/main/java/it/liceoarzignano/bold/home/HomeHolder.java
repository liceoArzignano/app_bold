package it.liceoarzignano.bold.home;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import it.liceoarzignano.bold.R;

class HomeHolder extends RecyclerView.ViewHolder {
    private final TextView mNameView;
    private final CardView mCardView;
    private final LinearLayout[] mLayout;
    private final TextView[] mTitleView;
    private final TextView[] mContentView;

    HomeHolder(View view) {
        super(view);
        mLayout = new LinearLayout[3];
        mTitleView = new TextView[3];
        mContentView = new TextView[3];

        mNameView = view.findViewById(R.id.home_item_name);
        mCardView = view.findViewById(R.id.home_item_card);
        mLayout[0] = view.findViewById(R.id.home_item_layout_0);
        mLayout[1] = view.findViewById(R.id.home_item_layout_1);
        mLayout[2] = view.findViewById(R.id.home_item_layout_2);
        mTitleView[0] = view.findViewById(R.id.home_item_title_0);
        mTitleView[1] = view.findViewById(R.id.home_item_title_1);
        mTitleView[2] = view.findViewById(R.id.home_item_title_2);
        mContentView[0] = view.findViewById(R.id.home_item_sec_0);
        mContentView[1] = view.findViewById(R.id.home_item_sec_1);
        mContentView[2] = view.findViewById(R.id.home_item_sec_2);
    }

    void init(HomeCard obj) {
        int size = obj.getSize();
        String name = obj.getName();
        List<String> titles = obj.getTitle();
        List<String> contents = obj.getContent();

        for (int counter = 0; counter < size; counter++) {
            mContentView[counter].setText(contents.get(counter));
            mLayout[counter].setVisibility(View.VISIBLE);
            if (titles.get(counter) != null && !titles.get(counter).isEmpty()) {
                mTitleView[counter].setText(titles.get(counter));
                mTitleView[counter].setVisibility(View.VISIBLE);
            }
        }

        mNameView.setText(name);
        mCardView.setOnClickListener(obj::doClickAction);
    }
}
