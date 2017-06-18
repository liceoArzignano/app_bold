package it.liceoarzignano.bold.home;

import android.view.View;

import java.util.List;

public class HomeCard {
    private final int mSize;
    private final String mName;
    private final List<String> mTitle;
    private final List<String> mContent;
    private final HomeCardClickListener mClickListner;

    HomeCard(int size, String name, List<String> title, List<String> content,
             HomeCardClickListener clickListener) {
        this.mSize = size;
        this.mName = name;
        this.mTitle = title;
        this.mContent = content;
        this.mClickListner = clickListener;
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

    void doClickAction(View view) {
        if (mClickListner == null) {
            return;
        }

        mClickListner.onClick(view);
    }

    public interface HomeCardClickListener {
        void onClick(View view);
    }
}
