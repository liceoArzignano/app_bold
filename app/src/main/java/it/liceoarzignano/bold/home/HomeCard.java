package it.liceoarzignano.bold.home;

import android.content.Context;
import android.content.Intent;

import java.util.List;

public class HomeCard {
    private final Context mContext;

    private final int mSize;
    private final String mName;
    private final List<String> mTitle;
    private final List<String> mContent;
    private final Intent mIntent;

    HomeCard(int size, String name, List<String> title, List<String> content,
             Context context, Intent intent) {
        this.mSize = size;
        this.mName = name;
        this.mTitle = title;
        this.mContent = content;
        this.mContext = context;
        this.mIntent = intent;
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

    void startIntent() {
        if (mContext == null) {
            return;
        }

        mContext.startActivity(mIntent);
    }
}
