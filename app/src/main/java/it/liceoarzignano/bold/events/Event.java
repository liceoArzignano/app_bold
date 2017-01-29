package it.liceoarzignano.bold.events;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Event extends RealmObject {

    @PrimaryKey
    private long id = 0;

    private String title;
    private Date date;
    private String note;
    private int icon;

    /*
     * Icon values
     * 0 = test
     * 1 = school
     * 2 = bday
     * 3 = homework
     * 4 = reminder
     * 5 = hangout
     * 6 = other
     */

    public Event() {
    }

    public Event(long id, String title, Date date, String note, int icon) {
        setId(id);
        setTitle(title);
        setDate(date);
        setIcon(icon);
        setNote(note);
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Event &&
                title.equalsIgnoreCase(((Event) o).getTitle()) &&
                date.equals(((Event) o).getDate());
    }
}
