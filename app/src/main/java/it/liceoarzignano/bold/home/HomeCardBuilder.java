package it.liceoarzignano.bold.home;

import java.util.ArrayList;
import java.util.List;

public class HomeCardBuilder {
    private final List<String> mTitleList = new ArrayList<>();
    private final List<String> mContentList = new ArrayList<>();
    private int mCounter;
    private String mName;
    private HomeCard.HomeCardClickListener mClickListener;

    public HomeCardBuilder setName(String name) {
        this.mName = name;
        return this;
    }

    public HomeCardBuilder addEntry(String title, String content) {
        if (mCounter > 2) {
            return this;
        }

        mCounter++;
        mTitleList.add(title);
        mContentList.add(content);
        return this;
    }

    public HomeCardBuilder setOnClick(HomeCard.HomeCardClickListener listener) {
        mClickListener = listener;
        return this;
    }

    public HomeCard build() {
        return new HomeCard(mCounter, mName, mTitleList, mContentList, mClickListener);
    }
}
