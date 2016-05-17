package it.liceoarzignano.bold.marks;

public class Mark {

    private int id;
    private String title;
    private String content;
    private int value;

    public Mark(int id, String title, int value, String content) {
        setId(id);
        setTitle(title);
        setValue(value);
        setContent(content);
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

    public String getContent() {
        return content;
    }

    private void setContent(String content) {
        this.content = content;
    }

    public int getValue() {
        return value;
    }

    private void setValue(int value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Mark && this.title.equalsIgnoreCase(((Mark) o).getTitle());
    }
}
