package com.example.jagon.surveybot;

import java.util.UUID;

public class Message {
    private String id;
    private String moduleName;
    private String message;
    private String userId;
    private String timeStamp;

    public Message() {
    }

    public Message(String moduleName, String message, String userId, String timeStamp) {
        this.id = UUID.randomUUID().toString();
        this.moduleName = moduleName;
        this.message = message;
        this.userId = userId;
        this.timeStamp = timeStamp;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
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

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id='" + id + '\'' +
                ", moduleName='" + moduleName + '\'' +
                ", message='" + message + '\'' +
                ", userId='" + userId + '\'' +
                ", timeStamp='" + timeStamp + '\'' +
                '}';
    }
}
