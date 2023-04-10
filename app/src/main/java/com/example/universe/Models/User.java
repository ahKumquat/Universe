package com.example.universe.Models;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * This class represents a user.
 */
public class User implements Serializable {
    public static final String KEY_UID = "uid";
    public static final String KEY_USERNAME = "userName";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_ABOUT = "about";
    public static final String KEY_AVATAR_Path = "avatarPath";
    public static final String KEY_DRAFT_EVENT = "draftEvent";
    public static final String KEY_FOLLOWERS_ID_LIST = "followersIdList";
    public static final String KEY_FOLLOWING_ID_LIST = "followingIdList";
    public static final String KEY_FAVOURITES_ID_LIST = "favouritesIdList";
    public static final String KEY_POSTS_ID_LIST = "postsIdList";
    public static final String KEY_JOINED_EVENTS_ID_LIST = "joinedEventsIdList";
    public static final String KEY_CHATS_MAP = "chatsMap";
    public static final String KEY_UNREAD_COUNT = "unreadCount";
    private String uid;
    private String userName;
    private String email;
    private String avatarPath;
    private String about;
    private Event draftEvent;
    private List<String> followersIdList;
    private List<String> followingIdList;
    private List<String> favouritesIdList;
    private List<String> postsIdList;
    private List<String> joinedEventsIdList;
    private HashMap<String, String> chatsMap;
    private int unreadCount;

    public User() {
    }

    public User(String uid, String userName, String email) {
        this.uid = uid;
        this.userName = userName;
        this.email = email;
        avatarPath = null;
        about = "";
        this.draftEvent = null;
        followersIdList = new ArrayList<>();
        followingIdList = new ArrayList<>();
        favouritesIdList = new ArrayList<>();
        postsIdList = new ArrayList<>();
        joinedEventsIdList = new ArrayList<>();
        chatsMap = new HashMap<>();
        unreadCount = 0;
    }

    public String getUid() {
        return uid;
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

    public String getAvatarPath() {return avatarPath;}

    public void setAvatarPath(String avatarPath) {this.avatarPath = avatarPath;}

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public Event getDraftEvent() {
        return draftEvent;
    }

    public List<String> getFollowersIdList() {
        return followersIdList;
    }

    public List<String> getFollowingIdList() {
        return followingIdList;
    }

    public List<String> getFavouritesIdList() {
        return favouritesIdList;
    }

    public List<String> getPostsIdList() {
        return postsIdList;
    }

    public List<String> getJoinedEventsIdList() {
        return joinedEventsIdList;
    }

    public HashMap<String, String> getChatsMap() {
        return chatsMap;
    }

    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", userName='" + userName + '\'' +
                ", email='" + email + '\'' +
                ", about='" + about + '\'' +
                '}';
    }

    /**
     * These setters are not recommended to use.
     */

    /**
     * @param uid
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setDraftEvent(Event draftEvent) {
        this.draftEvent = draftEvent;
    }

    public void setFollowersIdList(List<String> followersIdList) {
        this.followersIdList = followersIdList;
    }

    public void setFollowingIdList(List<String> followingIdList) {
        this.followingIdList = followingIdList;
    }

    public void setFavouritesIdList(List<String> favouritesIdList) {
        this.favouritesIdList = favouritesIdList;
    }

    public void setPostsIdList(List<String> postsIdList) {
        this.postsIdList = postsIdList;
    }

    public void setJoinedEventsIdList(List<String> joinedEventsIdList) {
        this.joinedEventsIdList = joinedEventsIdList;
    }

    public void setChatsMap(HashMap<String, String> chatsMap) {
        this.chatsMap = chatsMap;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return getUnreadCount() == user.getUnreadCount() && Objects.equals(getUid(), user.getUid()) && Objects.equals(getUserName(), user.getUserName()) && Objects.equals(getEmail(), user.getEmail()) && Objects.equals(getAvatarPath(), user.getAvatarPath()) && Objects.equals(getAbout(), user.getAbout()) && Objects.equals(getDraftEvent(), user.getDraftEvent()) && Objects.equals(getFollowersIdList(), user.getFollowersIdList()) && Objects.equals(getFollowingIdList(), user.getFollowingIdList()) && Objects.equals(getFavouritesIdList(), user.getFavouritesIdList()) && Objects.equals(getPostsIdList(), user.getPostsIdList()) && Objects.equals(getJoinedEventsIdList(), user.getJoinedEventsIdList()) && Objects.equals(getChatsMap(), user.getChatsMap());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUid(), getUserName(), getEmail(), getAvatarPath(), getAbout(), getDraftEvent(), getFollowersIdList(), getFollowingIdList(), getFavouritesIdList(), getPostsIdList(), getJoinedEventsIdList(), getChatsMap(), getUnreadCount());
    }
}
