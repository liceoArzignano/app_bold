package it.liceoarzignano.bold.marks;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import it.liceoarzignano.bold.Utils;

public class Mark extends RealmObject {
    @PrimaryKey
    private long id = 0;

    private String title;
    private String content;
    private String date;
    private int value;
    private boolean isFirstQuarter;

    public Mark() {

    }

    public Mark(long id, String title, int value, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.value = value;
    }

    public Mark(String title, int value, String content) {
        this.title = title;
        this.content = content;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
        isFirstQuarter = Utils.isFirstQuarter(date);
    }

    public boolean getIsFirstQuarter() {
        return isFirstQuarter;
    }
}
