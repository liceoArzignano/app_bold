package it.liceoarzignano.bold;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatDelegate;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import it.liceoarzignano.bold.firebase.BoldAnalytics;

public class BoldApp extends Application {

    private static RealmConfiguration sConfig;
    private static Context sContext;

    public static RealmConfiguration getAppRealmConfiguration() {
        return sConfig;
    }

    public static Context getContext() {
        return sContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sConfig = new RealmConfiguration.Builder(this)
                .name(Realm.DEFAULT_REALM_NAME)
                .schemaVersion(0)
                .deleteRealmIfMigrationNeeded()
                .build();

        Realm.setDefaultConfiguration(sConfig);

        sContext = getApplicationContext();

        FirebaseMessaging.getInstance().subscribeToTopic("global");
        FirebaseMessaging.getInstance().subscribeToTopic(Utils.getTopic(sContext));
        if (!Utils.isTeacher(sContext)) {
            FirebaseMessaging.getInstance().subscribeToTopic("students");
        }

        // Enable StrictMode
        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectFileUriExposure()
                    .detectCleartextNetwork()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }

        // Turn on support library vectorDrawables supports on legacy devices
        if (!Utils.isNotLegacy()) {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        }

        // Analytics
        Utils.enableTrackerIfOverlayRequests(sContext,
                getResources().getBoolean(R.bool.force_tracker));

        if (Utils.hasAnalytics(sContext)) {
            BoldAnalytics mBoldAnalytics = new BoldAnalytics(sContext);
            Bundle mBundle = new Bundle();
            mBundle.putString(FirebaseAnalytics.Param.LEVEL, "App");
            mBoldAnalytics.sendConfig(mBundle);
        }
    }

}
