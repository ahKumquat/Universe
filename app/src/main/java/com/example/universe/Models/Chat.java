package com.example.universe.Models;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Chat {
    public static String KEY_OTHER_USER_ID = "otherUserId";
    public static String KEY_OTHER_USER_NAME = "otherUserName";
    public static String KEY_MESSAGES = "messages";
    public static String KEY_UNREAD_Count = "unreadCount";
    private static String KEY_TIME_STAMP = "lastMessageTIme";
    private String otherUserId;
    private List<Message> messages;
    private int unreadCount;
    private Timestamp lastMessageTIme;
    public Chat() {
    }

    public Chat(String otherUserId) {
        this.otherUserId = otherUserId;
        this.messages = new ArrayList<>();
        this.unreadCount = 0;
        this.lastMessageTIme = new Timestamp(new Date());
    }

    public String getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(String otherUserId) {
        this.otherUserId = otherUserId;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public Timestamp getLastMessageTIme() {
        return lastMessageTIme;
    }

    public void setLastMessageTIme(Timestamp lastMessageTIme) {
        this.lastMessageTIme = lastMessageTIme;
    }
}
