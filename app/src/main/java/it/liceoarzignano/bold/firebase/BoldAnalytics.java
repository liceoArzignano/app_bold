package it.liceoarzignano.bold.firebase;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings.Secure;

import com.google.firebase.analytics.FirebaseAnalytics;

import it.liceoarzignano.bold.utils.PrefsUtils;
import it.liceoarzignano.bold.safe.mod.Encryption;


public class BoldAnalytics {
    private final FirebaseAnalytics mFirebaseAnalytics;
    private final Context mContext;

    @SuppressLint("HardwareIds")
    public BoldAnalytics(Context context) {
        mContext = context;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        mFirebaseAnalytics.setUserId(Encryption.strToSHA(
                Secure.getString(context.getContentResolver(), Secure.ANDROID_ID)));
        configUser();
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
        mFirebaseAnalytics.setUserProperty(PrefsUtils.IS_TEACHER,
                String.valueOf(PrefsUtils.isTeacher(mContext)));
        mFirebaseAnalytics.setUserProperty(PrefsUtils.SUGGESTIONS,
                String.valueOf(PrefsUtils.hasSuggestions(mContext)));
        mFirebaseAnalytics.setUserProperty(PrefsUtils.NOTIF_EVENT,
                String.valueOf(PrefsUtils.hasEventsNotification(mContext)));
        mFirebaseAnalytics.setUserProperty(PrefsUtils.NOTIF_NEWS,
                String.valueOf(PrefsUtils.hasNewsNotification(mContext)));
        mFirebaseAnalytics.setUserProperty(PrefsUtils.SAFE_DONE,
                String.valueOf(PrefsUtils.hasSafe(mContext)));
        if (!PrefsUtils.isTeacher(mContext)) {
            mFirebaseAnalytics.setUserProperty(PrefsUtils.ADDRESS, PrefsUtils.getAddress(mContext));
        }
    }

    public void log(String tag, String message) {
        if (!PrefsUtils.hasAnalytics(mContext)) {
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, message);
        mFirebaseAnalytics.logEvent(tag, bundle);
    }
}
