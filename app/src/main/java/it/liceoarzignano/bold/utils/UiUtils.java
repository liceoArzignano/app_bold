package it.liceoarzignano.bold.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.util.DisplayMetrics;
import android.view.View;

import it.liceoarzignano.bold.R;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

public final class UiUtils {

    private UiUtils() {
    }

    /**
     * Animate fab and showcase it
     *
     * @param context: used to create materialshowcase
     * @param fab:     fab that will be animated and exposed
     * @param title:   showcase title
     * @param message: showcase message
     * @param key:     showcase key to show it only the first time
     */
    @SuppressWarnings("SameParameterValue")
    public static void animFabIntro(final Activity context, final FloatingActionButton fab,
                                    final String title, final String message, final String key) {
        final SharedPreferences prefs = context.getSharedPreferences(PrefsUtils.EXTRA_PREFS,
                Context.MODE_PRIVATE);
        final boolean isFirstTime = prefs.getBoolean(key, true);
        if (PrefsUtils.isNotLegacy()) {
            fab.show();
        }
        new Handler().postDelayed(() -> {
            fab.setVisibility(View.VISIBLE);
            if (isFirstTime) {
                prefs.edit().putBoolean(key, false).apply();
                new MaterialTapTargetPrompt.Builder(context)
                        .setTarget(fab)
                        .setPrimaryText(title)
                        .setSecondaryText(message)
                        .setBackgroundColourFromRes(R.color.colorAccentDark)
                        .show();
            }
        }, 500);
    }

    /**
     * Start an avd animation using proper class basing on api level
     *
     * @param drawable animated drawable
     */
    public static void animateAVD(Drawable drawable) {
        if (!PrefsUtils.isNotLegacy()) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ((AnimatedVectorDrawable) drawable).start();
        } else {
            ((AnimatedVectorDrawableCompat) drawable).start();
        }
    }

    public static boolean isPhone(Context context) {
        return context.getResources().getBoolean(R.bool.is_phone);
    }

    public static float dpToPx(Resources res, float dp) {
        return dp * ((float) res.getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
}
