package com.example.universe;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.universe.Models.Event;
import com.example.universe.Models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.UUID;

public class Util {
    public static final String TAG = "test";
    public static final String USERS_COLLECTION_NAME = "users";
    public static final String EVENTS_COLLECTION_NAME = "events";
    private static Util util;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    public Object lock = new Object();
    private User dbUser;

    public static OnFailureListener defaultOnFailureListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            Log.d(TAG, "onFailure: " + e.getMessage());
        }
    };

    private Util(FirebaseAuth mAuth, FirebaseFirestore db, FirebaseStorage storage) {
        this.mAuth = mAuth;
        this.db = db;
        this.storage = storage;
    }

    private Util() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public static Util getInstance(FirebaseAuth mAuth, FirebaseFirestore db, FirebaseStorage storage) {
        if (util == null) {
            util = new Util(mAuth, db, storage);
        }
        return util;
    }

    public static Util getInstance() {
        if (util == null) {
            util = new Util();
        }
        return util;
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public User getDbUser(){
        return dbUser;
    }

    public String createUid(){
        return UUID.randomUUID().toString();
    }

    public void updateUser(){
        db.collection(USERS_COLLECTION_NAME)
                .document(getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        dbUser = documentSnapshot.toObject(User.class);
                        //TODO: remove this
                        getCurrentUser().updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(dbUser.getUserName()).build());
                        Log.d(TAG, "onSuccess Update User: " + dbUser);
                    }
                }).addOnFailureListener(defaultOnFailureListener);
    }

    public void createUserWithEmailAndPassword(String email, String password, String userName) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d(TAG, "onCreateUserSuccess: " + authResult.getUser());
                        User tempUser = new User(mAuth.getUid(), userName, email);
                        db.collection(USERS_COLLECTION_NAME).document(getCurrentUser().getUid()).set(tempUser)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG, "successfully add user to db, uid:" + getCurrentUser().getUid());
                                        getCurrentUser().updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(userName).build());
                                        updateUser();
                                    }
                                }).addOnFailureListener(defaultOnFailureListener);
                    }
                }).addOnFailureListener(defaultOnFailureListener);
    }

    public void loginUserWithEmailAndPassword(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                Log.d(TAG, "onLoginSuccess: " + authResult.getUser());
                updateUser();
            }
        }).addOnFailureListener(defaultOnFailureListener);
    }

    public void saveDraftEvent(Event event){
        db.collection(USERS_COLLECTION_NAME)
                .document(mAuth.getUid())
                .update(User.KEY_DRAFT_EVENT, event)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "on saveDraftEvent Success: ");
                    }
                }).addOnFailureListener(defaultOnFailureListener);
    }

    public void publishEvent(Event event){
        DocumentReference eventRef = db.collection(EVENTS_COLLECTION_NAME).document(event.getUid());
        DocumentReference userRef = db.collection(USERS_COLLECTION_NAME).document(mAuth.getUid());
        db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                transaction.update(userRef, User.KEY_DRAFT_EVENT, null);
                transaction.update(userRef, User.KEY_POSTS_ID_LIST, FieldValue.arrayUnion(event.getUid()));
                transaction.set(eventRef, event);
                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "on PublishEvent Success: ");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                eventRef.delete();
            }
        });
    }

    public void deleteEvent(String eventUID){
        DocumentReference eventRef = db.collection(EVENTS_COLLECTION_NAME).document(eventUID);
        DocumentReference userRef = db.collection(USERS_COLLECTION_NAME).document(mAuth.getUid());
        db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                transaction.update(userRef, User.KEY_POSTS_ID_LIST, FieldValue.arrayRemove(eventUID));
                transaction.delete(eventRef);
                //TODO: send notification to users that are affected by deletion.
//                Event event = transaction.get(eventRef).toObject(Event.class);
//                for (String participantId: event.getParticipants()){
//                    DocumentReference participantRef = db.collection(USERS_COLLECTION_NAME).document(mAuth.getUid());
//                    transaction.update(participantRef, User.KEY_JOINED_EVENTS_ID_LIST, FieldValue.arrayRemove(eventUID));
//                }
                return null;
            }
        });
    }

    public FirebaseAuth getmAuth() {
        return mAuth;
    }

    public FirebaseFirestore getDb() {
        return db;
    }

    public FirebaseStorage getStorage() {
        return storage;
    }
}