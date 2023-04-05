package com.example.universe;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.universe.Models.Chat;
import com.example.universe.Models.Event;
import com.example.universe.Models.Message;
import com.example.universe.Models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
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

import java.text.SimpleDateFormat;
import java.util.UUID;

/**
 * This class gives you a singleton Util object using Util.getInstance.
 * The singleton Util object provides method to interact with the database and handle callbacks.
 * This class also stores other useful constants/methods in static so that you can access them anywhere.
 */
public class Util {
    public static final String TAG = "test";
    public static final String USERS_COLLECTION_NAME = "users";
    public static final String EVENTS_COLLECTION_NAME = "events";
    public static final String CHATS_COLLECTION_NAME = "chats";
    private static final SimpleDateFormat EVENT_TIME_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    private static String currentTask = ""; //used for test printout
    private static Util util;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    //public Object lock = new Object();
    /**
     * This is the default onSuccessListener that logs the succeeded task, which takes null as the callback argument.
     */
    public static final OnSuccessListener<Void> DEFAULT_VOID_S_LISTENER = new OnSuccessListener<Void>() {
        @Override
        public void onSuccess(Void unused) {
            Log.d(TAG, "on " +currentTask +" Success: ");
        }
    };

    /**
     * This is the default onSuccessListener for authorization, which takes authresult as the callback argument.
     */
    public static final OnSuccessListener<AuthResult> DEFAULT_AUTH_S_LISTENER = new OnSuccessListener<AuthResult>() {
        @Override
        public void onSuccess(AuthResult authResult) {
            Log.d(TAG, "on " + currentTask +" Success: ");
        }
    };

    /**
     * This is the default onFailureListener for authorization, which prints out the error encountered in the current task.
     */
    public static final OnFailureListener DEFAULT_F_LISTENER = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            Log.d(TAG, "on" + currentTask + "Failure: " + e.getMessage());
        }
    };

    /**
     * This function turns a time stamp into a string that follows the event time format.
     * @param timestamp time stamp
     * @return a string.
     */
    public static String timeStampToEventTimeString(Timestamp timestamp){
        return EVENT_TIME_FORMAT.format(timestamp.toDate());
    }

    /**
     * Get the singleton util object.
     */
    private Util() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
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

    public String createUid(){
        return UUID.randomUUID().toString();
    }

//    public void updateUser(){
//        db.collection(USERS_COLLECTION_NAME)
//                .document(getCurrentUser().getUid())
//                .get()
//                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                    @Override
//                    public void onSuccess(DocumentSnapshot documentSnapshot) {
//                        dbUser = documentSnapshot.toObject(User.class);
//                        //TODO: remove this
//                        getCurrentUser().updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(dbUser.getUserName()).build());
//                        Log.d(TAG, "onSuccess Update User: " + dbUser);
//                    }
//                }).addOnFailureListener(defaultOnFailureListener);
//    }

    /**
     * Use email and PW to create a user account.
     * @param email email
     * @param password password
     * @param userName userName
     * @param sListener OnSuccessListener<Void>
     * @param fListener onFailure Listener
     */
    public void createUserWithEmailAndPassword(String email, String password, String userName, OnSuccessListener<Void> sListener, OnFailureListener fListener) {
        currentTask = "createUserWithEmailAndPassword";
        mAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d(TAG, "onCreateUserSuccess: " + authResult.getUser());
                        User tempUser = new User(mAuth.getUid(), userName, email);
                        db.collection(USERS_COLLECTION_NAME).document(getCurrentUser().getUid()).set(tempUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            getCurrentUser().updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(userName).build());
                                        }
                                    }
                                })
                                .addOnSuccessListener(sListener)
                                .addOnFailureListener(fListener);
                    }
                }).addOnFailureListener(DEFAULT_F_LISTENER);
    }

    /**
     * Use email and PW to login a user account.
     * @param email email
     * @param password password
     * @param sListener OnSuccessListener<AuthResult>
     * @param fListener onFailure Listener
     */
    public void loginUserWithEmailAndPassword(String email, String password, OnSuccessListener<AuthResult> sListener, OnFailureListener fListener){
        currentTask = "loginUserWithEmailAndPassword";
        mAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(sListener).addOnFailureListener(fListener);
    }

    /**
     * Get the user with the user's uid. For current user, use util.getmAuth.getUid() to get its uid.
     * @param uid the uid of a user
     * @param sListener OnSuccessListener<DocumentSnapshot>, the DocumentSnapshot can be converted to a User by toObject(User.class) method.
     * @param fListener onFailure Listener
     */
    public void getUser(String uid, OnSuccessListener<DocumentSnapshot> sListener, OnFailureListener fListener){
        currentTask = "getUser";
        db.collection(USERS_COLLECTION_NAME)
                .document(uid)
                .get()
                .addOnSuccessListener(sListener)
                .addOnFailureListener(fListener);
    }

    /**
     * Update the db to follow a user. This is a transaction.
     * @param otherUserUid the uid of user to follow.
     * @param sListener OnSuccessListener<Void>
     * @param fListener OnFailureListener
     */
    public void followUser(String otherUserUid, OnSuccessListener<Void> sListener, OnFailureListener fListener){
        currentTask = "followUser";
        DocumentReference userOtherRef = db.collection(USERS_COLLECTION_NAME).document(otherUserUid);
        DocumentReference userSelfRef = db.collection(USERS_COLLECTION_NAME).document(mAuth.getUid());
        db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                transaction.update(userSelfRef, User.KEY_FOLLOWING_ID_LIST, FieldValue.arrayUnion(otherUserUid));
                transaction.update(userOtherRef, User.KEY_FOLLOWERS_ID_LIST, FieldValue.arrayUnion(mAuth.getUid()));
                return null;
            }
        })
                .addOnSuccessListener(sListener)
                .addOnFailureListener(fListener);
    }

    /**
     * Update the db to unfollow a user. This is a transaction.
     * @param otherUserUid the uid of user to follow.
     * @param sListener OnSuccessListener<Void>
     * @param fListener OnFailureListener
     */
    public void unfollowUser(String otherUserUid, OnSuccessListener<Void> sListener, OnFailureListener fListener){
        currentTask = "unfollowUser";
        DocumentReference userOtherRef = db.collection(USERS_COLLECTION_NAME).document(otherUserUid);
        DocumentReference userSelfRef = db.collection(USERS_COLLECTION_NAME).document(mAuth.getUid());
        db.runTransaction(new Transaction.Function<Void>() {
                    @Nullable
                    @Override
                    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                        transaction.update(userSelfRef, User.KEY_FOLLOWING_ID_LIST, FieldValue.arrayRemove(otherUserUid));
                        transaction.update(userOtherRef, User.KEY_FOLLOWERS_ID_LIST, FieldValue.arrayRemove(mAuth.getUid()));
                        return null;
                    }
                })
                .addOnSuccessListener(sListener)
                .addOnFailureListener(fListener);
    }

    /**
     * Update the db to save event that is yet posted as a draft.
     * @param event an Event object.
     * @param sListener OnSuccessListener<Void>
     * @param fListener OnFailureListener
     */
    public void saveDraftEvent(Event event, OnSuccessListener<Void> sListener, OnFailureListener fListener){
        db.collection(USERS_COLLECTION_NAME)
                .document(mAuth.getUid())
                .update(User.KEY_DRAFT_EVENT, event)
                .addOnSuccessListener(sListener)
                .addOnFailureListener(fListener);
    }

    /**
     * Update the db to publish an event. This is a transaction.
     * @param event an Event object.
     * @param sListener OnSuccessListener<Void>
     * @param fListener OnFailureListener
     */
    public void publishEvent(Event event, OnSuccessListener<Void> sListener, OnFailureListener fListener){
        currentTask = "publishEvent";
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
        })
                .addOnSuccessListener(sListener)
                .addOnFailureListener(fListener);
    }

    /**
     * Update the db to delete an event. It will also update users' join list and send a message to users joined the event. This is a transaction.
     * @param eventUid uid of the event to delete.
     * @param sListener OnSuccessListener<Void>
     * @param fListener OnFailureListener
     */
    public void deleteEvent(String eventUid, OnSuccessListener<Void> sListener, OnFailureListener fListener){
        currentTask = "deleteEvent";
        DocumentReference eventRef = db.collection(EVENTS_COLLECTION_NAME).document(eventUid);
        DocumentReference userRef = db.collection(USERS_COLLECTION_NAME).document(mAuth.getUid());
        db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                Event event = transaction.get(eventRef).toObject(Event.class);
                Message message = event.createDeletionMessage(getCurrentUser());
                for (String participantId: event.getParticipants()){
                    DocumentReference participantRef = db.collection(USERS_COLLECTION_NAME).document(participantId);
                    //send notification to users that are affected by deletion.
                    sendMessage(participantId, message, DEFAULT_VOID_S_LISTENER, DEFAULT_F_LISTENER);
                    transaction.update(participantRef, User.KEY_JOINED_EVENTS_ID_LIST, FieldValue.arrayRemove(eventUid));
                }

                for (String candidateId: event.getCandidates()){
                    DocumentReference participantRef = db.collection(USERS_COLLECTION_NAME).document(candidateId);
                    //send notification to users that are affected by deletion.
                    sendMessage(candidateId, message, DEFAULT_VOID_S_LISTENER, DEFAULT_F_LISTENER);
                    transaction.update(participantRef, User.KEY_JOINED_EVENTS_ID_LIST, FieldValue.arrayRemove(eventUid));
                }

                transaction.update(userRef, User.KEY_POSTS_ID_LIST, FieldValue.arrayRemove(eventUid));
                transaction.delete(eventRef);
                return null;
            }
        })
                .addOnSuccessListener(sListener)
                .addOnFailureListener(fListener);
    }

    /**
     * Update the db to add event an event to favourite.
     * @param eventUid uid of the event to add to favourite
     * @param sListener OnSuccessListener<Void>
     * @param fListener OnFailureListener
     */
    public void addFavouriteEvent(String eventUid, OnSuccessListener<Void> sListener, OnFailureListener fListener){
        currentTask = "addFavouriteEvent";
        db.collection(USERS_COLLECTION_NAME)
                .document(mAuth.getUid())
                .update(User.KEY_FAVOURITES_ID_LIST, FieldValue.arrayUnion(eventUid))
                .addOnSuccessListener(sListener)
                .addOnFailureListener(fListener);
    }

    /**
     * Update the db to remove event an event from favourite.
     * @param eventUid uid of the event to remove from favourite
     * @param sListener OnSuccessListener<Void>
     * @param fListener OnFailureListener
     */
    public void removeFavouriteEvent(String eventUid, OnSuccessListener<Void> sListener, OnFailureListener fListener){
        currentTask = "removeFavouriteEvent";
        db.collection(USERS_COLLECTION_NAME)
                .document(mAuth.getUid())
                .update(User.KEY_FAVOURITES_ID_LIST, FieldValue.arrayRemove(eventUid))
                .addOnSuccessListener(sListener)
                .addOnFailureListener(fListener);
    }

    /**
     * Update the db to join an event. It will also send a message to the host. This is a transaction.
     * @param eventUid uid of the event to join.
     * @param sListener OnSuccessListener<Void>
     * @param fListener OnFailureListener
     */
    public void joinEvent(String eventUid, OnSuccessListener<Void> sListener, OnFailureListener fListener){
        currentTask = "joinEvent";
        DocumentReference eventRef = db.collection(EVENTS_COLLECTION_NAME).document(eventUid);
        DocumentReference selfUserRef = db.collection(USERS_COLLECTION_NAME).document(mAuth.getUid());
        db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                Event event = transaction.get(eventRef).toObject(Event.class);
                //send notification to users that are affected by approval.
                Message message = event.createApplyMessage(getCurrentUser());
                sendMessageWithTransaction(transaction, event.getHostId(), message);

                transaction.update(selfUserRef, User.KEY_JOINED_EVENTS_ID_LIST, FieldValue.arrayUnion(eventUid));
                transaction.update(eventRef, Event.KEY_CANDIDATES, FieldValue.arrayUnion(mAuth.getUid()));
                return null;
            }
        })
                .addOnSuccessListener(sListener)
                .addOnFailureListener(fListener);
    }

    /**
     * Update the db to approve an event. It will also send a message to the candidate approved. This is a transaction.
     * @param otherUserUid the uid of user to approve
     * @param eventUid the uid of event.
     * @param sListener OnSuccessListener<Void>
     * @param fListener OnFailureListener
     */
    public void approveJoinEvent(String otherUserUid, String eventUid, OnSuccessListener<Void> sListener, OnFailureListener fListener){
        currentTask = "approveJoinEvent";
        DocumentReference eventRef = db.collection(EVENTS_COLLECTION_NAME).document(eventUid);
        DocumentReference otherUserRef = db.collection(USERS_COLLECTION_NAME).document(otherUserUid);
        db.runTransaction(new Transaction.Function<Void>() {
                    @Nullable
                    @Override
                    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                        Event event = transaction.get(eventRef).toObject(Event.class);
                        if (event.getParticipants().size() >= event.getCapacity()){
                            throw new RuntimeException("Approve participant failed! Reach maximum event Capacity!");
                        }
                        //send notification to users that are affected by approval.
                        Message message = event.createApproveMessage(getCurrentUser());
                        sendMessageWithTransaction(transaction, otherUserUid, message);

                        transaction.update(eventRef, Event.KEY_CANDIDATES, FieldValue.arrayRemove(otherUserUid));
                        transaction.update(eventRef, Event.KEY_PARTICIPANTS, FieldValue.arrayUnion(otherUserUid));
                        return null;
                    }
                })
                .addOnSuccessListener(sListener)
                .addOnFailureListener(fListener);
    }

    public void rejectJoinEvent(String otherUserUid, String eventUid, OnSuccessListener<Void> sListener, OnFailureListener fListener){
        currentTask = "rejectJoinEvent";
        DocumentReference eventRef = db.collection(EVENTS_COLLECTION_NAME).document(eventUid);
        DocumentReference otherUserRef = db.collection(USERS_COLLECTION_NAME).document(otherUserUid);
        db.runTransaction(new Transaction.Function<Void>() {
                    @Nullable
                    @Override
                    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                        //end notification to users that are affected by approval.
                        Event event = transaction.get(eventRef).toObject(Event.class);
                        Message message = event.createRejectMessage(getCurrentUser());
                        sendMessageWithTransaction(transaction, otherUserUid, message);

                        transaction.update(eventRef, Event.KEY_CANDIDATES, FieldValue.arrayRemove(otherUserUid));
                        transaction.update(eventRef, Event.KEY_PARTICIPANTS, FieldValue.arrayRemove(otherUserUid));
                        transaction.update(otherUserRef, User.KEY_JOINED_EVENTS_ID_LIST, FieldValue.arrayRemove(eventUid));
                        return null;
                    }
                })
                .addOnSuccessListener(sListener)
                .addOnFailureListener(fListener);
    }

    public void sendMessage(String otherUserUid, Message message, OnSuccessListener<Void> sListener, OnFailureListener fListener){
        currentTask = "sendMessage";
        db.runTransaction(new Transaction.Function<Void>() {
                    @Nullable
                    @Override
                    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                        sendMessageWithTransaction(transaction, otherUserUid, message);
                        return null;
                    }
                })
                .addOnSuccessListener(sListener)
                .addOnFailureListener(fListener);
    }

    public void readChat(String otherUserUid, OnSuccessListener<Void> sListener, OnFailureListener fListener){
        currentTask = "read Chat";

    }

    public void sendMessageWithTransaction(Transaction transaction, String otherUserUid, Message message) throws FirebaseFirestoreException{
        DocumentReference selfChatRef = db.collection(USERS_COLLECTION_NAME).document(mAuth.getUid()).collection(CHATS_COLLECTION_NAME).document(otherUserUid);
        DocumentReference otherChatRef = db.collection(USERS_COLLECTION_NAME).document(otherUserUid).collection(CHATS_COLLECTION_NAME).document(mAuth.getUid());
        Chat selfChat = transaction.get(selfChatRef).toObject(Chat.class);
        Chat otherChat = transaction.get(otherChatRef).toObject(Chat.class);
        DocumentReference otherUserRef = db.collection(USERS_COLLECTION_NAME).document(otherUserUid);
        if (selfChat == null){
            selfChat = new Chat(otherUserUid);
            transaction.set(selfChatRef, selfChat);
        }
        if (otherChat == null){
            otherChat = new Chat(mAuth.getUid());
            transaction.set(otherChatRef, otherChat);
        }
        transaction.update(otherChatRef, Chat.KEY_MESSAGES, FieldValue.arrayUnion(message));
        transaction.update(otherChatRef, Chat.KEY_UNREAD_Count, FieldValue.increment(1));
        transaction.update(otherUserRef, User.KEY_UNREAD_COUNT, FieldValue.increment(1));
        transaction.update(selfChatRef, Chat.KEY_MESSAGES, FieldValue.arrayUnion(message));
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