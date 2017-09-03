package it.liceoarzignano.bold.utils

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.support.design.widget.FloatingActionButton
import android.support.graphics.drawable.AnimatedVectorDrawableCompat
import android.util.DisplayMetrics
import android.view.View
import it.liceoarzignano.bold.R
import it.liceoarzignano.bold.settings.AppPrefs
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt

object UiUtils {

    fun animFabIntro(activity: Activity, fab: FloatingActionButton,
                     title: String, message: String, key: String) {
        val prefs = AppPrefs(activity.baseContext)
        val isFirstTime = prefs.get(key, true)
        if (SystemUtils.isNotLegacy) {
            fab.show()
        }
        Handler().postDelayed({
            fab.visibility = View.VISIBLE
            if (isFirstTime) {
                prefs.set(key, false)
                MaterialTapTargetPrompt.Builder(activity, R.style.AppTheme_TapTarget)
                        .setTarget(fab)
                        .setPrimaryText(title)
                        .setSecondaryText(message)
                        .show()
            }
        }, 500)
    }

    fun animateAVD(drawable: Drawable) {
        if (!SystemUtils.isNotLegacy) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            (drawable as AnimatedVectorDrawable).start()
        } else {
            (drawable as AnimatedVectorDrawableCompat).start()
        }
    }

    fun isPhone(context: Context): Boolean = context.resources.getBoolean(R.bool.is_phone)

    fun dpToPx(res: Resources, dp: Float): Float =
            dp * (res.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}
