package it.liceoarzignano.bold.firebase;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings.Secure;

import com.google.firebase.analytics.FirebaseAnalytics;

import it.liceoarzignano.bold.Utils;

import static it.liceoarzignano.bold.safe.Encryption.strToSHA;

public class BoldAnalytics {
    private final FirebaseAnalytics mFirebaseAnalytics;
    private final Context context;

    @SuppressLint("HardwareIds")
    public BoldAnalytics(Context context) {
        this.context = context;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        mFirebaseAnalytics.setUserId(strToSHA(
                Secure.getString(context.getContentResolver(), Secure.ANDROID_ID)));
        configUser();
    }

    /**
     * Send firebase analytics event
     *
     * @param bundle event data
     */
    public void sendEvent(Bundle bundle) {
        bundle.putString(FirebaseAnalytics.Param.GROUP_ID, Utils.isTeacher(context) ?
                "0" : Utils.getAddress(context));
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    /**
     * Send firebase analytics when settings are opened
     *
     * @param bundle event data
     */
    public void sendConfig(Bundle bundle) {
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle);
    }

    /**
     * Setup user props basing on sharedPreferences
     */
    private void configUser() {
        mFirebaseAnalytics.setUserProperty(Utils.ISTEACHER,
                String.valueOf(Utils.isTeacher(context)));
        mFirebaseAnalytics.setUserProperty(Utils.SUGGESTIONS,
                String.valueOf(Utils.hasSuggestions(context)));
        mFirebaseAnalytics.setUserProperty(Utils.NOTIFICATION,
                String.valueOf(Utils.hasNotification(context)));
        mFirebaseAnalytics.setUserProperty(Utils.NOTIF_TIME,
                Utils.getNotificationTime(context));
        mFirebaseAnalytics.setUserProperty(Utils.SAFE_DONE,
                String.valueOf(Utils.hasSafe(context)));
        if (!Utils.isTeacher(context)) {
            mFirebaseAnalytics.setUserProperty(Utils.ADDRESS,
                    Utils.getAddress(context));
        }
    }
}
