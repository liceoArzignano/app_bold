package it.liceoarzignano.bold.tasks;

class Task {

    private int id;
    private String title;
    private int day;
    private int stage;

    public Task(int id, String title, int day, int stage) {
        setId(id);
        setTitle(title);
        setDay(day);
        setStage(stage);
    }

    public Task(String title, int day, int stage) {
        setTitle(title);
        setDay(day);
        setStage(stage);
    }

    public int getId() {
        return id;
    }

    private void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    private void setTitle(String title) {
        this.title = title;
    }

    public int getDay() {
        return day;
    }

    private void setDay(int day) {
        this.day = day;
    }

    public int getStage() {
        return stage;
    }

    private void setStage(int stage) {
        this.stage = stage;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Task && this.title.equalsIgnoreCase(((Task) o).getTitle());
    }
}
