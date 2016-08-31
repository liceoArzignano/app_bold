package it.liceoarzignano.bold;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.StrictMode;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import it.liceoarzignano.bold.firebase.BoldAnalytics;

public class BoldApp extends Application {

    private static RealmConfiguration configuration;
    private static Context context;
    private static BoldAnalytics mBoldAnalytics;

    public static RealmConfiguration getAppRealmConfiguration() {
        return configuration;
    }

    public static Context getBoldContext() {
        return context;
    }

    public static BoldAnalytics getBoldAnalytics() {
        return mBoldAnalytics;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        configuration = new RealmConfiguration.Builder(this)
                .name(Realm.DEFAULT_REALM_NAME)
                .schemaVersion(0)
                .deleteRealmIfMigrationNeeded()
                .build();

        Realm.setDefaultConfiguration(configuration);

        context = getApplicationContext();

        mBoldAnalytics = new BoldAnalytics(context);

        // Enable StrictMode
        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectFileUriExposure()
                    .detectCleartextNetwork()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }
    }

}
