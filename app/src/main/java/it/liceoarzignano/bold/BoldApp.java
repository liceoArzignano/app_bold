package it.liceoarzignano.bold;

import android.app.Application;
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

    private RealmConfiguration mConfig;

    @Override
    public void onCreate() {
        super.onCreate();

        mConfig = new RealmConfiguration.Builder(this)
                .name(Realm.DEFAULT_REALM_NAME)
                .schemaVersion(0)
                .deleteRealmIfMigrationNeeded()
                .build();

        Realm.setDefaultConfiguration(mConfig);

        FirebaseMessaging.getInstance().subscribeToTopic("global");
        FirebaseMessaging.getInstance().subscribeToTopic(Utils.getTopic(this));
        if (!Utils.isTeacher(this)) {
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
        Utils.enableTrackerIfOverlayRequests(this, getResources().getBoolean(R.bool.force_tracker));

        if (Utils.hasAnalytics(this)) {
            BoldAnalytics analytics = new BoldAnalytics(this);
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.LEVEL, "App");
            analytics.sendConfig(bundle);
        }
    }

    public RealmConfiguration getConfig() {
        return mConfig;
    }
}
