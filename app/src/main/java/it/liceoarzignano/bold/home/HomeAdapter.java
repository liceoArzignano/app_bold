package it.liceoarzignano.bold.home;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import it.liceoarzignano.bold.R;


public class HomeAdapter extends RecyclerView.Adapter<HomeHolder> {
    private final List<HomeCard> mObjects;

    public HomeAdapter(List<HomeCard> mObjects) {
        this.mObjects = mObjects;
    }

    @Override
    public HomeHolder onCreateViewHolder(ViewGroup mParent, int mType) {
        View mItem = LayoutInflater.from(mParent.getContext())
                .inflate(R.layout.item_home, mParent, false);

        return new HomeHolder(mItem);
    }

    @Override
    public void onBindViewHolder(HomeHolder mHolder, int mPosition) {
        HomeCard mObj = mObjects.get(mPosition);
        mHolder.init(mObj);
    }

    @Override
    public int getItemCount() {
        return mObjects.size();
    }


}