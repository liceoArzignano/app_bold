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
}
