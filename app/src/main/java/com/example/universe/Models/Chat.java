package com.example.universe.Models;

import com.example.universe.Util;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The Chat class represents a chat, recording all the messages in a List.
 */
public class Chat {
    public static String KEY_OTHER_USER_ID = "otherUserId";
    public static String KEY_UNREAD_Count = "unreadCount";
    public static String KEY_LATEST_MESSAGE = "lastMessage";
    public static String KEY_LATEST_MESSAGE_TIME = "lastMessageTime";
    private String otherUserId;
    private Message lastMessage;
    private int unreadCount;
    private Timestamp lastMessageTime;
    public Chat() {
    }

    public Chat(String otherUserId) {
        this.otherUserId = otherUserId;
        this.unreadCount = 0;
        this.lastMessage = null;
        this.lastMessageTime = new Timestamp(new Date());
    }

    public String getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(String otherUserId) {
        this.otherUserId = otherUserId;
    }

    public void setLastMessage(Message lastMessage){
        this.lastMessage = lastMessage;
    }


    public Message getLastMessage() {
        return lastMessage;
//        if (messages.size() == 0){
//            return new Message(Util.getInstance().getCurrentUser(), "", "");
//        }
//        int count = messages.size();
//        if (count == 0) return null;
//        else return messages.get(count - 1);
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
                '}';
    }
}
