package com.example.universe.Models;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;

import java.util.Date;

/**
 * The Message class represents a message.
 */
public class Message {
    private String userId;
    private String userName;
    private Timestamp timestamp;
    private String text;
    private String imageURL;

    public Message() {
    }

    public Message(FirebaseUser user, String text, String imageURL) {
        this.userId = user.getUid();
        this.userName = user.getDisplayName();
        this.text = text;
        this.imageURL = imageURL;
        this.timestamp = new Timestamp(new Date());
    }

    public boolean typeIsImage(){
        return imageURL != null;
    }

    public boolean typeIsText(){
        return imageURL == null;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
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
