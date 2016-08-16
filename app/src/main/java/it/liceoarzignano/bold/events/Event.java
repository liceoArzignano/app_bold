package it.liceoarzignano.bold.events;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Event extends RealmObject {

    @PrimaryKey
    private long id = 0;

    private String title;
    private String date;
    private int icon;

    /**
     * icon values
     * 0 = test
     * 1 = school
     * 2 = bday
     * 3 = hangout
     * 4 = other
     */

    public Event() {

    }

    public Event(long id, String title, String date, int icon) {
        setId(id);
        setTitle(title);
        setDate(date);
        setIcon(icon);
    }

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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Event && title.equalsIgnoreCase(((Event) o).getTitle());
    }
}
