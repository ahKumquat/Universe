package com.example.universe;

import android.util.Log;

import com.example.universe.Models.Event;
import com.example.universe.Models.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Date;

public class Test extends Thread{
    private static String TAG = Util.TAG;
    private static Util util;

    public Test(){
        util = Util.getInstance();
    }

    public void createUserWithEmailAndPassword(){
        util.createUserWithEmailAndPassword("abcd1234@gmai.com", "abcd1234", "Tester 1");
        util.createUserWithEmailAndPassword("bcde2345@gmai.com", "bcde2345", "Tester 2");
        util.createUserWithEmailAndPassword("cdef3456@gmai.com", "cdef3456", "Tester 3");
    }

    public void loginUserWithEmailAndPassword(){
        util.loginUserWithEmailAndPassword("bcde2345@gmai.com",  "bcde2345");
    }

    public void saveDraftEvent(){
        Event event = new Event(util.createUid(), util.getCurrentUser(),
                new Timestamp(new Date()), 2.0, Event.UNIT_MIN,
                "79 Brook St", new GeoPoint(15.0, 18.0),
                10, "desc", "URL");
        util.saveDraftEvent(event);
    }

    public void publishEvent(){
        Event event = new Event(util.createUid(), util.getCurrentUser(),
                new Timestamp(new Date()), 2.0, Event.UNIT_MIN,
                "79 Brook St", new GeoPoint(15.0, 18.0),
                30, "desc", "URL");
        util.publishEvent(event);
    }

    public void deleteEvent(){
        util.getDb().collection(Util.USERS_COLLECTION_NAME)
                .document(util.getCurrentUser().getUid())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User user = documentSnapshot.toObject(User.class);
                        String uid = user.getPostsIdList().get(0);
                        util.deleteEvent(uid);
                    }
                });
    }

    @Override
    public void run() {
        //createUserWithEmailAndPassword();
        loginUserWithEmailAndPassword();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //publishEvent();
        deleteEvent();
    }
}
