package com.example.jagon.surveybot;

import java.util.UUID;

public class Message {
    private String id;
    private String moduleName;
    private String message;

    public Message(String moduleName, String message) {
        this.id = UUID.randomUUID().toString();
        this.moduleName = moduleName;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getMessage() {
        return message;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id='" + id + '\'' +
                "moduleName='" + moduleName + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
