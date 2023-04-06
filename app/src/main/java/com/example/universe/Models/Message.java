package com.example.universe.Models;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.Exclude;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The Message class represents a message.
 */
public class Message {
    private String userId;
    //this is the sender's userId
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
    @Exclude
    public String getSimpleTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");
        return simpleDateFormat.format(timestamp.toDate());
    }

    public String getText() {
        return text;
    }

    public String getImageURL() {
        return imageURL;
    }

    @Override
    public String toString() {
        return "Message{" +
                "userId='" + userId + '\'' +
                ", text='" + text + '\'' +
                ", imageURL='" + imageURL + '\'' +
                "}";
    }
}
