package com.example.prif233.model;


import java.time.LocalDate;

public class Chat {
    private int id;
    private String name;
    private String chatText;
    private LocalDate dateCreated;

    public Chat() {
    }

    public Chat(int id, String name, String chatText, LocalDate dateCreated) {
        this.id = id;
        this.name = name;
        this.chatText = chatText;
        this.dateCreated = dateCreated;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getChatText() {
        return chatText;
    }

    public void setChatText(String chatText) {
        this.chatText = chatText;
    }

    public LocalDate getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDate dateCreated) {
        this.dateCreated = dateCreated;
    }

    @Override
    public String toString() {
        // Present user and message text nicely; date kept minimal
        if (dateCreated != null) {
            return name + ": " + chatText + " (" + dateCreated + ")";
        }
        return name + ": " + chatText;
    }
}
