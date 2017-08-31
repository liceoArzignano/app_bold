package it.liceoarzignano.bold.home;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.utils.UiUtils;

class ShortcutHolder extends RecyclerView.ViewHolder {
    private final CardView mCardView;
    private final ImageView mIcon;
    private final TextView mTitle;

    ShortcutHolder(View view) {
        super(view);
        mCardView = view.findViewById(R.id.home_item_shortcut_card);
        mIcon = view.findViewById(R.id.home_item_shortcut_icon);
        mTitle = view.findViewById(R.id.home_item_shortcut_title);
    }

    void bind(Context context, @StringRes int title, @DrawableRes int icon,
              @ColorRes int color, ShortcutListener listener) {
        int position = getAdapterPosition();
        mTitle.setText(context.getString(title));
        mIcon.setImageResource(icon);
        mCardView.setCardBackgroundColor(ContextCompat.getColor(context, color));
        mCardView.setOnClickListener(v -> listener.onClick(position));

        if (position != 0 && position != 4) {
            return;
        }

        // Fix margins to match the default grid
        Resources r = context.getResources();
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) mCardView.getLayoutParams();
        int defaultMargin = params.topMargin;
        int extraMargin = (int) UiUtils.dpToPx(r, 8);
        boolean isFirst = position == 0;
        params.setMargins(isFirst ? extraMargin : defaultMargin, defaultMargin,
                isFirst ? defaultMargin : extraMargin, defaultMargin);
    }

    interface ShortcutListener {
        void onClick(int position);
    }
}