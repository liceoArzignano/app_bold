package it.liceoarzignano.bold.home;

import java.util.List;

public class HomeCard {
    private final int mSize;
    private final String mName;
    private final List<String> mTitle;
    private final List<String> mContent;

    HomeCard(int size, String name, List<String> title, List<String> content) {
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
}
