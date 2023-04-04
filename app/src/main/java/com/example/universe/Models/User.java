package com.example.universe.Models;

import java.util.LinkedList;
import java.util.List;

public class User {
    private String userId;
    private String userName;
    private String email;
    private String about;
    private Event draftEvent;
    private List<String> followersIdList;
    private List<String> followingIdList;
    private List<String> favouritesIdList;
    private List<String> postsIdList;
    private List<String> pastEventsIdList;
    private List<Chat> chats;

    public User() {
    }

    public User(String userId, String userName, String email) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        about = "";
        this.draftEvent = null;
        followersIdList = new LinkedList<>();
        followingIdList = new LinkedList<>();
        favouritesIdList = new LinkedList<>();
        postsIdList = new LinkedList<>();
        pastEventsIdList = new LinkedList<>();
        chats = new LinkedList<>();
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }
}
