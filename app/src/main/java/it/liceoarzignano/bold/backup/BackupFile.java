package it.liceoarzignano.bold.backup;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import it.liceoarzignano.bold.events.Event2;
import it.liceoarzignano.bold.events.EventsHandler;
import it.liceoarzignano.bold.marks.Mark2;
import it.liceoarzignano.bold.marks.MarksHandler;
import it.liceoarzignano.bold.news.News2;
import it.liceoarzignano.bold.news.NewsHandler;

class BackupFile {
    private static final String TAG = BackupFile.class.getSimpleName();
    static final String FILE_NAME = "Liceo.backup";
    private static final String MARK_HEADER = "_ID, subject, value, date, description, firstQuarter";
    private static final String EVENT_HEADER = "_ID, title, date, description, category";
    private static final String NEWS_HEADER = "_ID, title, date, description, url";
    private static final String COMMA_REPLACER = "\u2016";
    private static final String SEPARATOR = ", ";

    private final StringBuilder mBuilder;
    private final String mDataPath;

    private File mRestore;

    BackupFile(Activity activity) {
        mBuilder = new StringBuilder();
        mDataPath = activity.getCacheDir().getAbsolutePath();
    }

    void createBackup(Context context) {
        addMarks(MarksHandler.getInstance(context).getAll());
        addEvents(EventsHandler.getInstance(context).getAll());
        addNews(NewsHandler.getInstance(context).getAll());
    }

    private void addMarks(List<Mark2> list) {
        mBuilder.append(MARK_HEADER).append('\n');
        for (Mark2 item : list) {
            mBuilder.append(item.getId())
                    .append(SEPARATOR)
                    .append(item.getSubject().replace(",", COMMA_REPLACER))
                    .append(SEPARATOR)
                    // Some locales use comma instead of dot as separator
                    .append(String.valueOf(item.getValue()).replace(",", COMMA_REPLACER))
                    .append(SEPARATOR)
                    .append(item.getDate())
                    .append(SEPARATOR)
                    .append(item.getDescription().replace(",",  COMMA_REPLACER))
                    .append(SEPARATOR)
                    .append(String.valueOf(item.isFirstQuarter()))
                    .append('\n');
        }
    }

    private void addEvents(List<Event2> list) {
        mBuilder.append(EVENT_HEADER).append('\n');
        for (Event2 item : list) {
            mBuilder.append(item.getId())
                    .append(SEPARATOR)
                    .append(item.getTitle().replace(",", COMMA_REPLACER))
                    .append(SEPARATOR)
                    .append(item.getDate())
                    .append(SEPARATOR)
                    .append(item.getDescription().replace(",",  COMMA_REPLACER))
                    .append(SEPARATOR)
                    .append(String.valueOf(item.getCategory()))
                    .append('\n');
        }
    }

    private void addNews(List<News2> list) {
        mBuilder.append(NEWS_HEADER).append('\n');
        for (News2 item : list) {
            mBuilder.append(item.getId())
                    .append(SEPARATOR)
                    .append(item.getTitle().replace(",", COMMA_REPLACER))
                    .append(SEPARATOR)
                    .append(item.getDate())
                    .append(SEPARATOR)
                    .append(item.getDescription().replace(",",  COMMA_REPLACER))
                    .append(SEPARATOR)
                    .append(item.getUrl().replace(",", COMMA_REPLACER))
                    .append('\n');
        }
    }

    File getOutput() {
        File file = new File(getFilePath());
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(mBuilder.toString());
            writer.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        return file;
    }

    private String getFilePath() {
        return mDataPath + "/" + FILE_NAME;
    }

    void fetch(InputStream iStream) {
        mRestore = new File(mDataPath + "/" + FILE_NAME);

        try {
            // Create file using bytes from gdrive
            OutputStream oStream = new FileOutputStream(mRestore);
            byte[] buffer = new byte[4 * 1024];
            int read;
            while ((read = iStream.read(buffer)) != -1) {
                oStream.write(buffer, 0, read);
            }
            oStream.flush();
            oStream.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    List<Mark2> getMarks() {
        List<Mark2> list = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(mRestore));
            reader.readLine(); // Marks header
            String line = reader.readLine();
            do {
                String[] data = line.split(SEPARATOR);
                list.add(new Mark2(
                        Long.parseLong(data[0]),
                        data[1].replace(COMMA_REPLACER, ","),
                        Integer.parseInt(data[2].replace(COMMA_REPLACER, ",")),
                        Long.parseLong(data[3].replace(COMMA_REPLACER, ",")),
                        data[4].replace(COMMA_REPLACER, ","),
                        "1".equals(data[5]))
                );
                line = reader.readLine();
            } while (line != null && !EVENT_HEADER.equals(line));
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        return list;
    }

    List<Event2> getEvents() {
        List<Event2> list = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(mRestore));
            String line = reader.readLine();
            while (!EVENT_HEADER.equals(line)) {
                line = reader.readLine();
            } // Move cursor to events part

            line = reader.readLine();

            do {
                String[] data = line.split(SEPARATOR);
                list.add(new Event2(
                        Long.parseLong(data[0]),
                        data[1].replace(COMMA_REPLACER, ","),
                        Long.parseLong(data[2].replace(COMMA_REPLACER, ",")),
                        data[3].replace(COMMA_REPLACER, ","),
                        Integer.parseInt(data[4]))
                );
                line = reader.readLine();
            } while (line != null && !NEWS_HEADER.equals(line));
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        return list;
    }

    List<News2> getNews() {
        List<News2> list = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(mRestore));
            String line = reader.readLine();
            while (!NEWS_HEADER.equals(line)) {
                line = reader.readLine();
            } // Move cursor to news part
            line = reader.readLine();

            do {
                String[] data = line.split(SEPARATOR);
                list.add(new News2(
                        Long.parseLong(data[0]),
                        data[1].replace(COMMA_REPLACER, ","),
                        Long.parseLong(data[2].replace(COMMA_REPLACER, ",")),
                        data[3].replace(COMMA_REPLACER, ","),
                        data[4].replace(COMMA_REPLACER, ","))
                );
                line = reader.readLine();
            } while (line != null);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        return list;
    }
}
