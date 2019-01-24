package com.example.dnt.chatapp;

public class Message {
    private String from, message, type;


    public Message() {
    }

    public Message(String from, String message, String type) {
        this.from = from;
        this.message = message;
        this.type = type;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }
}
