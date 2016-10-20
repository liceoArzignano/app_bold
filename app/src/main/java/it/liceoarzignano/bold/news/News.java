package it.liceoarzignano.bold.news;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class News extends RealmObject {

    @PrimaryKey
    private long id = 0;

    private String title;
    private String message;
    private String url;
    private String date;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return String.format("%1$s: %2$s (%3$s)\n%4$s", title, message, date, url);
    }

}
