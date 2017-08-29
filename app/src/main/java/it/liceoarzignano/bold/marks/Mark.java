package it.liceoarzignano.bold.marks;

import it.liceoarzignano.bold.database.DBItem;

public class Mark extends DBItem {
    private String subject;
    private int value;
    private long date;
    private String description;
    private boolean firstQuarter;

    public Mark() {
    }

    public Mark(String subject, int value) {
        this.subject = subject;
        this.value = value;
    }

    public Mark(String subject, int value, long date) {
        this.subject = subject;
        this.value = value;
        this.date = date;
    }

    public Mark(String subject, int value, long date, String description) {
        this.subject = subject;
        this.value = value;
        this.date = date;
        this.description = description;
    }

    public Mark(String subject, int value, long date,
                String description, boolean firstQuarter) {
        this.subject = subject;
        this.value = value;
        this.date = date;
        this.description = description;
        this.firstQuarter = firstQuarter;
    }


    public Mark(long id, String subject, int value, long date,
                String description, boolean firstQuarter) {
        this.id = id;
        this.subject = subject;
        this.value = value;
        this.date = date;
        this.description = description;
        this.firstQuarter = firstQuarter;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
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

    public boolean isFirstQuarter() {
        return firstQuarter;
    }

    public void setFirstQuarter(boolean firstQuarter) {
        this.firstQuarter = firstQuarter;
    }
}
