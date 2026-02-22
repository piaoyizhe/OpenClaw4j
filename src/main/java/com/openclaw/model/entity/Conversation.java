package com.openclaw.model.entity;

import java.util.ArrayList;
import java.util.List;
import com.openclaw.model.entity.Message;

public class Conversation {
    private List<Message> messages;
    private String id;
    private long createdAt;

    public Conversation() {
        this.messages = new ArrayList<>();
        this.id = generateId();
        this.createdAt = System.currentTimeMillis();
    }

    private String generateId() {
        return "conv_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    public List<Message> getMessages() {
        return messages;
    }

    public String getId() {
        return id;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
