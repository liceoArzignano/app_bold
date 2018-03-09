package it.liceoarzignano.bold.utils

import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import it.liceoarzignano.bold.BuildConfig

object SystemUtils {
    private const val TAG = "SystemUtils"

    val isNotLegacy: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
    val hasApi23: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

    fun hasGDrive(context: Context): Boolean =
            try {
                val info = context.packageManager
                        .getPackageInfo("com.google.android.apps.docs", 0)
                info.applicationInfo.enabled
            } catch (e: PackageManager.NameNotFoundException) {
                // Gotta catch 'em all
                false
            }

    fun hasNoInternetConnection(context: Context): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return manager.activeNetworkInfo == null
    }

    fun hasNoGMS(context: Context): Boolean =
            GoogleApiAvailability.getInstance()
                    .isGooglePlayServicesAvailable(context) != ConnectionResult.SUCCESS

    fun getSafetyNetApiKey(context: Context): String = try {
        val info = context.packageManager
                .getApplicationInfo(BuildConfig.APPLICATION_ID, PackageManager.GET_META_DATA)
        val metadata = info.metaData
        val apiKey = metadata.getString("com.google.android.safetynet.ATTEST_API_KEY")

        apiKey ?: ""
    } catch (e: PackageManager.NameNotFoundException) {
        Log.e(TAG, e.message)
        ""
    }
}
