package it.liceoarzignano.bold;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class BoldApp extends android.app.Application {

    private static RealmConfiguration configuration;

    @Override
    public void onCreate() {
        super.onCreate();

        configuration = new RealmConfiguration.Builder(this)
                .name(Realm.DEFAULT_REALM_NAME)
                .schemaVersion(0)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(configuration);
    }

    public static RealmConfiguration getAppRealmConfiguration() {
        return configuration;
    }
}
