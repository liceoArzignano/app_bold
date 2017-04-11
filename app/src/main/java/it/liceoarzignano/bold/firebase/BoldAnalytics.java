package it.liceoarzignano.bold.firebase;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings.Secure;

import com.google.firebase.analytics.FirebaseAnalytics;

import it.liceoarzignano.bold.Utils;
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
            mFirebaseAnalytics.setUserProperty(Utils.ADDRESS, Utils.getAddress(mContext));
        }
    }

    public void log(String tag, String message) {
        if (!Utils.hasAnalytics(mContext)) {
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, message);
        mFirebaseAnalytics.logEvent(tag, bundle);
    }
}
