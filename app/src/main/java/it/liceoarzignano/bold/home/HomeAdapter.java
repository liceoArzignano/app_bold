package it.liceoarzignano.bold.home;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.util.List;

import it.liceoarzignano.bold.R;


public class HomeAdapter extends RecyclerView.Adapter<HomeHolder> {
    private final Context mContext;
    private final List<HomeCard> mObjects;

    private final boolean mShouldAnimate;
    private int mLast = -1;

    public HomeAdapter(Context context, List<HomeCard> objects, boolean shouldAnimate) {
        mContext = context;
        mObjects = objects;
        mShouldAnimate = shouldAnimate;
    }

    @Override
    public HomeHolder onCreateViewHolder(ViewGroup parent, int type) {
        View item = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home, parent, false);

        return new HomeHolder(item);
    }

    @Override
    public void onBindViewHolder(HomeHolder holder, int position) {
        HomeCard obj = mObjects.get(position);
        holder.init(obj);
        if (mShouldAnimate && position > mLast) {
            holder.itemView.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_up));
        }
        mLast = holder.getAdapterPosition();
    }

    @Override
    public int getItemCount() {
        return mObjects.size();
    }

    @Override
    public void onViewDetachedFromWindow(HomeHolder homeHolder) {
        super.onViewDetachedFromWindow(homeHolder);
        homeHolder.itemView.clearAnimation();
    }
}