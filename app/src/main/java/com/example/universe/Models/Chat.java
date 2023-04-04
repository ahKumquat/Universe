package com.example.universe.Models;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Chat {
    private String targetUserId;
    private List<Message> messages;
    private List<Message> unreadMessages;
    public Chat() {
    }

    public Chat(String targetUserId) {
        this.targetUserId = targetUserId;
        this.messages = new ArrayList<>();
        this.unreadMessages = new ArrayList<>();
    }
}
