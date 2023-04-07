package com.example.universe;

import android.util.Log;

import com.example.universe.Models.Chat;
import com.example.universe.Models.Event;
import com.example.universe.Models.Message;
import com.example.universe.Models.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.firestore.GeoPoint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Test extends Thread{
    private static String TAG = Util.TAG;
    private static String[] TEST_EVENT_UIDs = {"test_event_uid", "test_event_uid_2", "test_event_uid_3"};
    private static String[] TEST_USER_EMAILS = {"abcd1234@gmai.com", "bcde2345@gmai.com", "cdef3456@gmai.com", "defg4567@gmai.com"};
    private static String[] TEST_USER_PWS = {"abcd1234", "bcde2345", "cdef3456", "defg4567"};
    private static String[] TEST_USER_IDS = {
            "i32u2BHOHIZeEFz7TJ67XlpmZE12",
            "BPPVNqSwlOg5EjQDq9pvKmXT1rq1",
            "05PF5OxNMWYdHKw5Upj4kU5tnAK2",
            "p17DVqKKhSMrtSmap9lj2tD77x13"
    };

    private static GeoPoint boston = new GeoPoint(42.34139354184174, -71.09022116162349);

    private static Util util;

    public Test(){
        util = Util.getInstance();
    }

    public void createUserWithEmailAndPassword(int i){
        util.createUserWithEmailAndPassword(TEST_USER_EMAILS[i],  TEST_USER_PWS[i], "Tester " + (i+1), Util.DEFAULT_VOID_S_LISTENER, Util.DEFAULT_F_LISTENER);
    }

    public void loginUserWithEmailAndPassword(int i, OnSuccessListener<AuthResult> sListener){
        util.loginUserWithEmailAndPassword(TEST_USER_EMAILS[i],  TEST_USER_PWS[i], sListener, Util.DEFAULT_F_LISTENER);

    }

    public void getUser(String uid){
        util.getUser(uid, new OnSuccessListener<User>() {
            @Override
            public void onSuccess(User user) {
                Log.d(TAG, "on getUser Success: " + user);
            }
        }, Util.DEFAULT_F_LISTENER);
    }

    public void followUser(String otherUserUid){
        util.followUser(otherUserUid, Util.DEFAULT_VOID_S_LISTENER, Util.DEFAULT_F_LISTENER);
    }

    public void unfollowUser(){
        util.unfollowUser("BPPVNqSwlOg5EjQDq9pvKmXT1rq1", Util.DEFAULT_VOID_S_LISTENER, Util.DEFAULT_F_LISTENER);
    }

    public void saveDraftEvent(){
        Event event = new Event(util.createUid(), util.getCurrentUser(), "DRAFT EVENT",
                new Timestamp(new Date()), 2.0, Event.UNIT_MIN,
                "79 Brook St", new GeoPoint(25.0, 25.0),
                10, "desc", "URL");
        util.saveDraftEvent(event, Util.DEFAULT_VOID_S_LISTENER, Util.DEFAULT_F_LISTENER);
    }

    public void postEvent(String eventId){
        Event event = new Event(eventId, util.getCurrentUser(), "TEST EVENT",
                new Timestamp(new Date()), 2.0, Event.UNIT_MIN,
                "79 Brook St", new GeoPoint(25.0, 25.0),
                30, "desc", "URL");
        util.postEvent(event, Util.DEFAULT_VOID_S_LISTENER, Util.DEFAULT_F_LISTENER);
    }

    public void deleteEvent(String eventUid){
        util.deleteEvent(eventUid, Util.DEFAULT_VOID_S_LISTENER, Util.DEFAULT_F_LISTENER);
    }

    public void joinEvent(String eventId){
        util.joinEvent(eventId, Util.DEFAULT_VOID_S_LISTENER, Util.DEFAULT_F_LISTENER);
    }

    public void quitEvent(String eventId){
        util.quitEvent(eventId, Util.DEFAULT_VOID_S_LISTENER, Util.DEFAULT_F_LISTENER);
    }

    public void approveJoinEvent(String otherUserId, String eventId){
        util.approveJoinEvent(otherUserId, eventId, Util.DEFAULT_VOID_S_LISTENER, Util.DEFAULT_F_LISTENER);
    }

    public void rejectJoinEvent(String otherUserId, String eventId){
        util.rejectJoinEvent(otherUserId, eventId, Util.DEFAULT_VOID_S_LISTENER, Util.DEFAULT_F_LISTENER);
    }

    public void addFavouriteEvent(String eventUid){
        util.addFavouriteEvent(eventUid, Util.DEFAULT_VOID_S_LISTENER,Util.DEFAULT_F_LISTENER);
    }

    public void removeFavouriteEvent(String eventUid){
        util.removeFavouriteEvent(eventUid, Util.DEFAULT_VOID_S_LISTENER,Util.DEFAULT_F_LISTENER);
    }

    public void sendMessage(String otherUserId){
        Message message = new Message(util.getCurrentUser(), "Test Message!", null,null);
        util.sendMessage(otherUserId, message, Util.DEFAULT_VOID_S_LISTENER, Util.DEFAULT_F_LISTENER);
    }

    public void readChat(String otherUserId){
        util.readChat(otherUserId, Util.DEFAULT_VOID_S_LISTENER, Util.DEFAULT_F_LISTENER);
    }

    public void deleteChat(String otherUserId){
        util.deleteChat(otherUserId, Util.DEFAULT_VOID_S_LISTENER, Util.DEFAULT_F_LISTENER);
    }

    public void getChat(String otherUserUid){
        util.getChat(otherUserUid, new OnSuccessListener<Chat>() {
            @Override
            public void onSuccess(Chat chat) {
                Log.d(TAG, "on getChat Success: " + chat);
            }
        }, Util.DEFAULT_F_LISTENER);
    }

    public void getChats(){
        util.getChats(new OnSuccessListener<List<Chat>>() {
            @Override
            public void onSuccess(List<Chat> chats) {
                for (Chat chat: chats){
                    Log.d(TAG, "on get Chats Success: " + chat);
                }
            }
        }, Util.DEFAULT_F_LISTENER);
    }

    public void getMessages(String otherUserId){
        util.getMessages(otherUserId, new OnSuccessListener<List<Message>>() {
            @Override
            public void onSuccess(List<Message> messages) {
                for (Message message: messages){
                    Log.d(TAG, message.toString());
                }
            }
        }, Util.DEFAULT_F_LISTENER);
    }

    public void getFollowers(String userUid){
        util.getFollowers(userUid, new OnSuccessListener<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                Log.d(TAG, "on getFollowers Success: " + users.size());
                for (User user: users){
                    Log.d(TAG, user.toString());
                }
            }
        }, Util.DEFAULT_F_LISTENER);
    }

    public void getFollowing(String userUid){
        util.getFollowing(userUid, new OnSuccessListener<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                Log.d(TAG, "on getFollowing Success: " + users);
                for (User user: users){
                    Log.d(TAG, user.toString());
                }
            }
        }, Util.DEFAULT_F_LISTENER);
    }

    public void getFavouriteEvents(String userUid){
        util.getFavouriteEvents(userUid, new OnSuccessListener<List<Event>>() {
            @Override
            public void onSuccess(List<Event> events) {
                Log.d(TAG, "on getFavouriteEvents Success: " + events.size() + "    " + events);
            }
        }, Util.DEFAULT_F_LISTENER);
    }

    public void getJoinEvents(String userUid){
        util.getJoinEvents(userUid, new OnSuccessListener<List<Event>>() {
            @Override
            public void onSuccess(List<Event> events) {
                Log.d(TAG, "on getJoinEvents Success: " + events.size() + "    " + events);
            }
        }, Util.DEFAULT_F_LISTENER);
    }

    public void getParticipantsAndCandidates(Event event){
        util.getParticipantsAndCandidates(event, new OnSuccessListener<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                Log.d(TAG, "on ParticipantsAndCandidates Success: " + users.size() + "    " + users);
            }
        },Util.DEFAULT_F_LISTENER);
    }

    public void getEvent(String eventId){
        util.getEvent(eventId, new OnSuccessListener<Event>() {
            @Override
            public void onSuccess(Event event) {
                Log.d(TAG, "on getEvent Success: " + event);
            }
        }, Util.DEFAULT_F_LISTENER);
    }

    public void getPostEvents(String userUid){
        util.getPostEvents(userUid, new OnSuccessListener<List<Event>>() {
            @Override
            public void onSuccess(List<Event> events) {
                Log.d(TAG, "on getPostEvents Success: " + events.size() + "    " + events);
            }
        }, Util.DEFAULT_F_LISTENER);
    }

    public void getNearbyEvents(GeoPoint geoPoint, double radius){
        util.getNearByEvents(geoPoint, radius, new OnSuccessListener<List<Event>>() {
            @Override
            public void onSuccess(List<Event> events) {
                Log.d(TAG, "on getNearbyEvents Success: " + events);
                for (Event event: events){
                    Log.d(TAG, event.getGeoPoint().getLatitude() + "_" + event.getGeoPoint().getLongitude());
                }
            }
        }, Util.DEFAULT_F_LISTENER);
    }

    public void prepopulateEvents(GeoPoint startPoint, double stepDist,  int step) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("MM-dd,HH:mm");
        double lat = startPoint.getLatitude();
        double lon = startPoint.getLongitude();
        Event event;
        for (int i = 0; i <step; i++){
            lat = lat + stepDist;
            if (lat > 90){
                lat -= 180;
            }
            lon = startPoint.getLongitude();
            for (int j = 0; j < step; j++){
                lon = (lon + stepDist) % 180;
                GeoPoint loc = new GeoPoint(lat, lon);
                Timestamp time = new Timestamp(new Date(System.currentTimeMillis() + +3600000L * 6 *(i*step + j)));
                String uid = "lat="+ Math.round(lat*10)/10.0 + ",lon=" + Math.round(lon*10)/10.0 +",t="+ format.format(time.toDate());
                    event = new Event(uid, util.getCurrentUser(), "", time,
                            i+1, Event.UNIT_HOUR, "", new GeoPoint(lat, lon),
                            i + 1, "", null);
                util.postEvent(event, Util.DEFAULT_VOID_S_LISTENER, Util.DEFAULT_F_LISTENER);
            }
        }
    }

    public void getFollowingEvents(){
        util.getFriendEvents(new OnSuccessListener<List<Event>>() {
            @Override
            public void onSuccess(List<Event> events) {
                Log.d(TAG, "onSuccess: " + events);
                for (Event event: events){
                    Log.d(TAG, event.getUid() + ", " + Util.timeStampToEventTimeString(event.getTime()) );
                }
            }
        }, Util.DEFAULT_F_LISTENER);
    }

    @Override
    public void run() {
        //createUserWithEmailAndPassword(3);
        loginUserWithEmailAndPassword(2, authResult -> {
            Log.d(TAG, "on Login Success: " + util.getmAuth().getUid());
            util.updateProfile("/images/113", "About me!", Util.DEFAULT_VOID_S_LISTENER, Util.DEFAULT_F_LISTENER);
        });
            //getFollowingEvents();
            //getFollowing(util.getmAuth().getUid());
            //getNearbyEvents(new GeoPoint(31, 31), Util.DEFAULT_RADIUS);
            //prepopulateEvents(new GeoPoint(30, 30), 0.2, 10);
            //getJoinEvents(util.getCurrentUser().getUid());
            //getFavouriteEvents(util.getCurrentUser().getUid());
            //getPostEvents(util.getCurrentUser().getUid());
            //addFavouriteEvent(TEST_EVENT_UIDs[2]);
            //addFavouriteEvent();
            //getFollowers(util.getCurrentUser().getUid());
            //getChats();
            //deleteChat(TEST_USER_IDS[1]);
            //readChat(TEST_USER_IDS[0]);
            //sendMessage(TEST_USER_IDS[1]);
            //deleteEvent(TEST_EVENT_UIDs[0]);
            //approveJoinEvent(TEST_USER_IDS[1], TEST_EVENT_UIDs[0]);
            //joinEvent(TEST_EVENT_UIDs[0]);
            //sendMessage(TEST_USER_IDS[1]);
            //rejectJoinEvent(TEST_USER_IDS[1], TEST_EVENT_UIDs[0]);
            //publishEvent(TEST_EVENT_UIDs[0]);
            //removeFavouriteEvent();
            //getUser();
            //deleteEvent(TEST_EVENT_UIDs[2]);
    }
}
