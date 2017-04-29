package it.liceoarzignano.bold.home;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import it.liceoarzignano.bold.R;


public class HomeAdapter extends RecyclerView.Adapter<HomeHolder> {
    private final List<HomeCard> mObjects;

    public HomeAdapter(List<HomeCard> objects) {
        this.mObjects = objects;
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
    }

    @Override
    public int getItemCount() {
        return mObjects.size();
    }
}