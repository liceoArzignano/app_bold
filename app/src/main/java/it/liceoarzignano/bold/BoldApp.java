package it.liceoarzignano.bold;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.StrictMode;

import com.google.firebase.messaging.FirebaseMessaging;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import it.liceoarzignano.bold.firebase.BoldAnalytics;

public class BoldApp extends Application {

    private static RealmConfiguration sConfig;
    private static Context sContext;
    private static BoldAnalytics sBoldAnalytics;

    public static RealmConfiguration getAppRealmConfiguration() {
        return sConfig;
    }

    public static Context getBoldContext() {
        return sContext;
    }

    public static BoldAnalytics getBoldAnalytics() {
        return sBoldAnalytics;
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

        sBoldAnalytics = new BoldAnalytics(sContext);
        FirebaseMessaging.getInstance().subscribeToTopic("global");
        FirebaseMessaging.getInstance().subscribeToTopic(Utils.getTopic(sContext));

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
