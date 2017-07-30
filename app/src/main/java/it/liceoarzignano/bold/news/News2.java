package it.liceoarzignano.bold.news;

import it.liceoarzignano.bold.database.DBItem;

public class News2 extends DBItem {
    private String title;
    private long date;
    private String description;
    private String url;

    public News2(String title, long date, String description, String url) {
        this.title = title;
        this.date = date;
        this.description = description;
        this.url = url;
    }

    public News2(long id, String title, long date, String description, String url) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.description = description;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
