package it.liceoarzignano.bold.home;

import java.util.ArrayList;
import java.util.List;

public class HomeCard {
    private final int mSize;
    private final String mName;
    private final List<String> mTitle;
    private final List<String> mContent;

    private HomeCard(int size, String name, List<String> title, List<String> content) {
        this.mSize = size;
        this.mName = name;
        this.mTitle = title;
        this.mContent = content;
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
        private final List<String> mTitleList = new ArrayList<>();
        private final List<String> mContentList = new ArrayList<>();
        private int mCounter;
        private String mName;

        public Builder setName(String name) {
            this.mName = name;
            return this;
        }

        public Builder addEntry(String title, String content) {
            if (mCounter > 2) {
                return this;
            }

            mCounter++;
            mTitleList.add(title);
            mContentList.add(content);
            return this;
        }

        public HomeCard build() {
            return new HomeCard(mCounter, mName, mTitleList, mContentList);
        }
    }
}
