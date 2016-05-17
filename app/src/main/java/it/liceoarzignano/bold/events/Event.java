package it.liceoarzignano.bold.events;

public class Event {

    private int id;
    private String title;
    private String value;
    private int icon;

    /**
     * icon values
     * 0 = test
     * 1 = school
     * 2 = bday
     * 3 = hangout
     * 4 = other
     */

    public Event(int id, String title, String value, int icon) {
        setId(id);
        setTitle(title);
        setValue(value);
        setIcon(icon);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    private void setTitle(String title) {
        this.title = title;
    }

    public String getValue() {
        return value;
    }

    private void setValue(String value) {
        this.value = value;
    }

    public int getIcon() {
        return icon;
    }

    private void setIcon(int icon) {
        this.icon = icon;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Event && this.title.equalsIgnoreCase(((Event) o).getTitle());
    }
}
