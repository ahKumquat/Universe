package com.example.universe;

import android.util.Log;

import com.example.universe.Models.Event;
import com.example.universe.Models.Message;
import com.example.universe.Models.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Date;

public class Test extends Thread{
    private static String TAG = Util.TAG;
    private static String[] TEST_EVENT_UIDs = {"test_event_uid", "test_event_uid_2", "test_event_uid_3"};
    private static String[] TEST_USER_EMAILS = {"abcd1234@gmai.com", "bcde2345@gmai.com", "cdef3456@gmai.com"};
    private static String[] TEST_USER_PWS = {"abcd1234", "bcde2345", "cdef3456"};
    private static String[] TEST_USER_IDS = {
            "i32u2BHOHIZeEFz7TJ67XlpmZE12",
            "BPPVNqSwlOg5EjQDq9pvKmXT1rq1",
            "05PF5OxNMWYdHKw5Upj4kU5tnAK2"
    };
    private static Util util;

    public Test(){
        util = Util.getInstance();
    }

    public void createUserWithEmailAndPassword(int i){
        util.createUserWithEmailAndPassword(TEST_USER_EMAILS[i],  TEST_USER_PWS[i], "Tester " + (i+1), Util.DEFAULT_VOID_S_LISTENER, Util.DEFAULT_F_LISTENER);
    }

    public void loginUserWithEmailAndPassword(int i){
        util.loginUserWithEmailAndPassword(TEST_USER_EMAILS[i],  TEST_USER_PWS[i], Util.DEFAULT_AUTH_S_LISTENER, Util.DEFAULT_F_LISTENER);

    }

    public void getUser(){
        util.getUser("i32u2BHOHIZeEFz7TJ67XlpmZE12", new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Log.d(TAG, "on getUser Success: " + documentSnapshot.toObject(User.class).toString());
            }
        }, Util.DEFAULT_F_LISTENER);
    }

    public void followUser(){
        util.followUser("BPPVNqSwlOg5EjQDq9pvKmXT1rq1", Util.DEFAULT_VOID_S_LISTENER, Util.DEFAULT_F_LISTENER);
    }

    public void unfollowUser(){
        util.unfollowUser("BPPVNqSwlOg5EjQDq9pvKmXT1rq1", Util.DEFAULT_VOID_S_LISTENER, Util.DEFAULT_F_LISTENER);
    }

    public void saveDraftEvent(){
        Event event = new Event(util.createUid(), util.getCurrentUser(), "DRAFT EVENT",
                new Timestamp(new Date()), 2.0, Event.UNIT_MIN,
                "79 Brook St", new GeoPoint(15.0, 18.0),
                10, "desc", "URL");
        util.saveDraftEvent(event, Util.DEFAULT_VOID_S_LISTENER, Util.DEFAULT_F_LISTENER);
    }

    public void publishEvent(String eventId){
        Event event = new Event(eventId, util.getCurrentUser(), "TEST EVENT",
                new Timestamp(new Date()), 2.0, Event.UNIT_MIN,
                "79 Brook St", new GeoPoint(15.0, 18.0),
                30, "desc", "URL");
        util.publishEvent(event, Util.DEFAULT_VOID_S_LISTENER, Util.DEFAULT_F_LISTENER);
    }

    public void deleteEvent(String eventUid){
        util.deleteEvent(eventUid, Util.DEFAULT_VOID_S_LISTENER, Util.DEFAULT_F_LISTENER);
    }

    public void joinEvent(String eventId){
        util.joinEvent(eventId, Util.DEFAULT_VOID_S_LISTENER, Util.DEFAULT_F_LISTENER);
    }

    public void approveJoinEvent(String otherUserId, String eventId){
        util.approveJoinEvent(otherUserId, eventId, Util.DEFAULT_VOID_S_LISTENER, Util.DEFAULT_F_LISTENER);
    }

    public void rejectJoinEvent(String otherUserId, String eventId){
        util.rejectJoinEvent(otherUserId, eventId, Util.DEFAULT_VOID_S_LISTENER, Util.DEFAULT_F_LISTENER);
    }

    public void addFavouriteEvent(){
        util.addFavouriteEvent("211a39e7-6fc0-472a-b4d7-4e35a8bcaf5f", Util.DEFAULT_VOID_S_LISTENER,Util.DEFAULT_F_LISTENER);
    }

    public void removeFavouriteEvent(){
        util.removeFavouriteEvent("211a39e7-6fc0-472a-b4d7-4e35a8bcaf5f", Util.DEFAULT_VOID_S_LISTENER,Util.DEFAULT_F_LISTENER);
    }

    public void sendMessage(String otherUserId){
        Message message = new Message(util.getCurrentUser(), "Test Message!", null);
        util.sendMessage(otherUserId, message, Util.DEFAULT_VOID_S_LISTENER, Util.DEFAULT_F_LISTENER);
    }
    @Override
    public void run() {
        //createUserWithEmailAndPassword();
        loginUserWithEmailAndPassword(0);
        try {
            Thread.sleep(3000);
            //deleteEvent(TEST_EVENT_UIDs[0]);
            //approveJoinEvent(TEST_USER_IDS[1], TEST_EVENT_UIDs[0]);
            //joinEvent(TEST_EVENT_UIDs[0]);
            //sendMessage(TEST_USER_IDS[1]);
            //rejectJoinEvent(TEST_USER_IDS[1], TEST_EVENT_UIDs[0]);
            //publishEvent(TEST_EVENT_UIDs[0]);
            //removeFavouriteEvent();
            //getUser();
            deleteEvent(TEST_EVENT_UIDs[0]);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
