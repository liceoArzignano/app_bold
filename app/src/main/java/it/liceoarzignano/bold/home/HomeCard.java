package it.liceoarzignano.bold.home;

import java.util.ArrayList;
import java.util.List;

public class HomeCard {
    private final int mSize;
    private final String mName;
    private final List<String> mTitle;
    private final List<String> mContent;

    private HomeCard(int mSize, String mName, List<String> mTitle, List<String> mContent) {
        this.mSize = mSize;
        this.mName = mName;
        this.mTitle = mTitle;
        this.mContent = mContent;
    }

    public int getSize() {
        return mSize;
    }

    public String getName() {
        return mName;
    }

    public List<String> getTitle() {
        return mTitle;
    }

    public List<String> getContent() {
        return mContent;
    }

    public static class Builder {
        private int mCounter;
        private String mName;
        private final List<String> mTitleList = new ArrayList<>();
        private final List<String> mContentList = new ArrayList<>();

        public Builder setName(String mName) {
            this.mName = mName;
            return this;
        }

        public Builder addEntry(String mTitle, String mContent) {
            if (mCounter > 2) {
                return this;
            }

            mCounter++;
            mTitleList.add(mTitle);
            mContentList.add(mContent);
            return this;
        }

        public HomeCard build() {
            return new HomeCard(mCounter, mName, mTitleList, mContentList);
        }
    }
}