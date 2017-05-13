package it.liceoarzignano.bold.marks;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Mark extends RealmObject {
    @PrimaryKey
    private long id = 0;

    private String title;
    private String note;
    private Date date;
    private int value;
    private boolean isFirstQuarter;

    public Mark() {
    }

    public Mark(long id, String title, int value, String note) {
        this.id = id;
        this.title = title;
        this.note = note;
        this.value = value;
    }

    public Mark(String title, int value, String note) {
        this.title = title;
        this.note = note;
        this.value = value;
    }

    public Mark(String title, int value) {
        this.title = title;
        this.value = value;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date, boolean isFirstQuarter) {
        this.date = date;
        this.isFirstQuarter = isFirstQuarter;
    }

    boolean getIsFirstQuarter() {
        return isFirstQuarter;
    }
}
