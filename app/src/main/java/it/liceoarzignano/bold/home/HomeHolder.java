package it.liceoarzignano.bold.home;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import it.liceoarzignano.bold.R;

class HomeHolder extends RecyclerView.ViewHolder {
    private TextView mNameView;
    private LinearLayout[] mLayout;
    private TextView[] mTitleView;
    private TextView[] mContentView;

    HomeHolder(View mView) {
        super(mView);
        mLayout = new LinearLayout[3];
        mTitleView = new TextView[3];
        mContentView = new TextView[3];

        mNameView = (TextView) mView.findViewById(R.id.home_item_name);
        mLayout[0] = (LinearLayout) mView.findViewById(R.id.home_item_layout_0);
        mLayout[1] = (LinearLayout) mView.findViewById(R.id.home_item_layout_1);
        mLayout[2] = (LinearLayout) mView.findViewById(R.id.home_item_layout_2);
        mTitleView[0] = (TextView) mView.findViewById(R.id.home_item_title_0);
        mTitleView[1] = (TextView) mView.findViewById(R.id.home_item_title_1);
        mTitleView[2] = (TextView) mView.findViewById(R.id.home_item_title_2);
        mContentView[0] = (TextView) mView.findViewById(R.id.home_item_sec_0);
        mContentView[1] = (TextView) mView.findViewById(R.id.home_item_sec_1);
        mContentView[2] = (TextView) mView.findViewById(R.id.home_item_sec_2);
    }

    void init(HomeCard mObj) {
        int mSize = mObj.getSize();
        String mName = mObj.getName();
        List<String> mTitles = mObj.getTitle();
        List<String> mContents = mObj.getContent();

        for (int mCounter = 0; mCounter < mSize; mCounter++) {
            mContentView[mCounter].setText(mContents.get(mCounter));
            mLayout[mCounter].setVisibility(View.VISIBLE);
            if (mTitles.get(mCounter) != null && !mTitles.get(mCounter).isEmpty()) {
                mTitleView[mCounter].setText(mTitles.get(mCounter));
                mTitleView[mCounter].setVisibility(View.VISIBLE);
            }
        }

        mNameView.setText(mName);
    }

}
