package com.example.universe.Models;

import com.google.firebase.Timestamp;

import java.util.Date;

public class Message {
    private String userId;
    private String userName;
    private String email;
    private Timestamp timestamp;
    private String text;
    private String imageURL;

    public Message() {
    }

    public Message(User user, String text, String imageURL) {
        this.userId = user.getUid();
        this.userName = user.getUserName();
        this.email = user.getEmail();
        this.text = text;
        this.imageURL = imageURL;
        this.timestamp = new Timestamp(new Date());
    }

    public boolean isImage(){
        return imageURL != null;
    }

    public boolean isText(){
        return !isImage();
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return email;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getText() {
        return text;
    }

    public String getImageURL() {
        return imageURL;
    }
}
