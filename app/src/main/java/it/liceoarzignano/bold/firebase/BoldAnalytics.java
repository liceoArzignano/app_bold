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
    private final Context mContext;

    @SuppressLint("HardwareIds")
    public BoldAnalytics(Context mContext) {
        this.mContext = mContext;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(mContext);
        mFirebaseAnalytics.setUserId(strToSHA(
                Secure.getString(mContext.getContentResolver(), Secure.ANDROID_ID)));
        configUser();
    }

    /**
     * Send firebase analytics when settings are opened
     *
     * @param mBundle event data
     */
    public void sendConfig(Bundle mBundle) {
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, mBundle);
    }

    /**
     * Setup user props basing on sharedPreferences
     */
    private void configUser() {
        mFirebaseAnalytics.setUserProperty(Utils.IS_TEACHER,
                String.valueOf(Utils.isTeacher(mContext)));
        mFirebaseAnalytics.setUserProperty(Utils.SUGGESTIONS,
                String.valueOf(Utils.hasSuggestions(mContext)));
        mFirebaseAnalytics.setUserProperty(Utils.NOTIF_EVENT,
                String.valueOf(Utils.hasEventsNotification(mContext)));
        mFirebaseAnalytics.setUserProperty(Utils.NOTIF_NEWS,
                String.valueOf(Utils.hasNewsNotification(mContext)));
        mFirebaseAnalytics.setUserProperty(Utils.SAFE_DONE,
                String.valueOf(Utils.hasSafe(mContext)));
        if (!Utils.isTeacher(mContext)) {
            mFirebaseAnalytics.setUserProperty(Utils.ADDRESS,
                    Utils.getAddress(mContext));
        }
    }
}
