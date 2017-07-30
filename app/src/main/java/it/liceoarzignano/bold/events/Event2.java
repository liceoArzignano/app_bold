package it.liceoarzignano.bold.events;

import it.liceoarzignano.bold.database.DBItem;

public class Event2 extends DBItem {
    private String title;
    private long date;
    private String description;
    private int category;

    /*
     * Category values
     * 0 = test
     * 1 = school
     * 2 = bday
     * 3 = homework
     * 4 = reminder
     * 5 = hangout
     * 6 = other
     */

    public Event2() {
    }

    public Event2(String title, long date, String description, int category) {
        this.title = title;
        this.date = date;
        this.description = description;
        this.category = category;
    }

    public Event2(long id, String title, long date, String description, int category) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.description = description;
        this.category = category;
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

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }
}
