package com.example.jagon.surveybot;

import java.util.UUID;

public class Module {
    private String id;
    private String title;

    public Module() {
    }

    public Module(String title) {
        this.id = UUID.randomUUID().toString();
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
