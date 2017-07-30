package it.liceoarzignano.bold.realm;

import android.content.Context;
import android.content.SharedPreferences;

import io.realm.RealmConfiguration;
import it.liceoarzignano.bold.BoldApp;
import it.liceoarzignano.bold.events.Event;
import it.liceoarzignano.bold.events.Event2;
import it.liceoarzignano.bold.events.EventsController;
import it.liceoarzignano.bold.events.EventsHandler;
import it.liceoarzignano.bold.marks.Mark;
import it.liceoarzignano.bold.marks.Mark2;
import it.liceoarzignano.bold.marks.MarksController;
import it.liceoarzignano.bold.marks.MarksHandler;
import it.liceoarzignano.bold.news.News;
import it.liceoarzignano.bold.news.News2;
import it.liceoarzignano.bold.news.NewsController;
import it.liceoarzignano.bold.news.NewsHandler;
import it.liceoarzignano.bold.utils.DateUtils;
import it.liceoarzignano.bold.utils.PrefsUtils;

public class MigrationTool {
    private static final String KEY_MIGRATE_SQL = "migrateSQL_key";

    public void migrate(BoldApp app) {
        SharedPreferences prefs = app.getSharedPreferences(PrefsUtils.EXTRA_PREFS,
                Context.MODE_PRIVATE);

        if (hasMigrated(app)) {
            return;
        }

        migrateMarks(app);
        migrateEvents(app);
        migrateNews(app);

        prefs.edit().putBoolean(KEY_MIGRATE_SQL, true).apply();
    }

    public boolean hasMigrated(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PrefsUtils.EXTRA_PREFS,
                Context.MODE_PRIVATE);
        RealmConfiguration config = ((BoldApp) context.getApplicationContext()).getConfig();
        int sum = new MarksController(config).getAll().size() +
                new EventsController(config).getAll().size() +
                new NewsController(config).getAll().size();
        return prefs.getBoolean(KEY_MIGRATE_SQL, false) || sum == 0;
    }

    private void migrateMarks(BoldApp app) {
        MarksController controller = new MarksController(app.getConfig());
        MarksHandler handler = MarksHandler.getInstance(app);
        for (Mark mark : controller.getAll()) {
            handler.add(new Mark2(
                    mark.getTitle(),
                    mark.getValue(),
                    mark.getDate().getTime(),
                    mark.getNote(),
                    mark.getIsFirstQuarter())
            );
            controller.delete(mark.getId());
        }
    }

    private void migrateEvents(BoldApp app) {
        EventsController controller = new EventsController(app.getConfig());
        EventsHandler handler = EventsHandler.getInstance(app);
        for (Event event : controller.getAll()) {
            handler.add(new Event2(
                    event.getTitle(),
                    event.getDate().getTime(),
                    event.getNote(),
                    event.getIcon())
            );
            controller.delete(event.getId());
        }
    }

    private void migrateNews(BoldApp app) {
        NewsController controller = new NewsController(app.getConfig());
        NewsHandler handler = NewsHandler.getInstance(app);
        for (News news : controller.getAll()) {
            handler.add(new News2(
                    news.getTitle(),
                    DateUtils.stringToDate(news.getDate()).getTime(),
                    news.getMessage(),
                    news.getUrl())
            );
            controller.delete(news.getId());
        }
    }
}
