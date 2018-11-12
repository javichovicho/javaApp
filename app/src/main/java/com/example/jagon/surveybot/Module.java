package com.example.jagon.surveybot;

public class Module {
    private String id;
    private String title;

    public Module(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "Module{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
