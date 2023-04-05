package com.example.universe.Models;

import com.example.universe.Util;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The Chat class represents a chat, recording all the messages in a List.
 */
public class Chat {
    public static String KEY_OTHER_USER_ID = "otherUserId";
    public static String KEY_OTHER_USER_NAME = "otherUserName";
    public static String KEY_MESSAGES = "messages";
    public static String KEY_UNREAD_Count = "unreadCount";
    public static String KEY_LATEST_MESSAGE_TIME = "lastMessageTime";
    private String otherUserId;
    private List<Message> messages;
    private int unreadCount;
    private Timestamp lastMessageTime;
    public Chat() {
    }

    public Chat(String otherUserId) {
        this.otherUserId = otherUserId;
        this.messages = new ArrayList<>();
        this.unreadCount = 0;
        this.lastMessageTime = new Timestamp(new Date());
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

    public Message getLastMessage() {
        return messages.get(messages.size() - 1);
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

    public Timestamp getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(Timestamp lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    @Override
    public String toString() {
        return "Chat{" +
                "otherUserId='" + otherUserId + '\'' +
                ", lastMessageTime=" + Util.timeStampToEventTimeString(lastMessageTime) +
                ", messages=" + messages +
                '}';
    }
}
