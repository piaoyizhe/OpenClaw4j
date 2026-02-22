package com.openclaw.model.entity;

public class Message {
    private String role;
    private String content;
    private long timestamp;

    public Message(String role, String content) {
        this.role = role;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }

    public String getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
