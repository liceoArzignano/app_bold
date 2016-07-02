package it.liceoarzignano.bold;

import android.content.Context;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class BoldApp extends android.app.Application {

    private static RealmConfiguration configuration;
    public static Context context;

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
    }

    public static RealmConfiguration getAppRealmConfiguration() {
        return configuration;
    }

    public static Context getBoldContext() {
        return context;
    }
}
