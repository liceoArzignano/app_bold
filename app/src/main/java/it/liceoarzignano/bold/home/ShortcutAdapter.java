package it.liceoarzignano.bold.home;

import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.liceoarzignano.bold.MainActivity;
import it.liceoarzignano.bold.R;

public class ShortcutAdapter extends RecyclerView.Adapter<ShortcutHolder> {
    @StringRes
    private static final int[] TITLES = {
            R.string.shortcut_site, R.string.shortcut_register,
            R.string.shortcut_moodle, R.string.shortcut_copybook,
            R.string.shortcut_teacherzone
    };
    @DrawableRes
    private static final int[] ICONS = {
            R.drawable.ic_website, R.drawable.ic_register,
            R.drawable.ic_moodle, R.drawable.ic_copybook,
            R.drawable.ic_teacherzone
    };
    @ColorRes
    private static final int[] COLORS = {
            R.color.shortcut_website, R.color.shortcut_register,
            R.color.shortcut_moodle, R.color.shortcut_copybook,
            R.color.shortcut_teacherzone
    };

    private final Context mContext;
    private final ShortcutHolder.ShortcutListener mListener;

    public ShortcutAdapter(MainActivity activity) {
        mContext = activity.getBaseContext();
        mListener = activity::showUrl;
    }

    @Override
    public ShortcutHolder onCreateViewHolder(ViewGroup parent, int type) {
        View item = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_shortcut, parent, false);
        return new ShortcutHolder(item);
    }

    @Override
    public void onBindViewHolder(ShortcutHolder holder, int position) {
        holder.bind(mContext, TITLES[position], ICONS[position], COLORS[position], mListener);
    }

    @Override
    public int getItemCount() {
        return TITLES.length;
    }
}