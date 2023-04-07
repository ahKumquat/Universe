package com.example.universe;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.universe.Models.Chat;
import com.example.universe.Models.Event;
import com.example.universe.Models.Message;
import com.example.universe.Models.User;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
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
    public static final String MESSAGES_COLLECTION_NAME = "messages";
    private static final long CUT_OFF_TIME_MILLISECONDS = 1000 * 60 * 60 * 24 * 3;
    public static final double DEFAULT_RADIUS = 50 * 1000;
    public static final SimpleDateFormat EVENT_TIME_FORMAT = new SimpleDateFormat("yyyy/MM/dd,HH:mm");
    private static String currentTask = ""; //used for test printout
    private static Util util;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    /**
     * This is the default onSuccessListener that logs the succeeded task, which takes null as the callback argument.
     */
    public static final OnSuccessListener<Void> DEFAULT_VOID_S_LISTENER = new OnSuccessListener<Void>() {
        @Override
        public void onSuccess(Void unused) {
            Log.d(TAG, "on " + currentTask + " Success: ");
        }
    };

    /**
     * This is the default onSuccessListener for authorization, which takes AuthResult as the callback argument.
     */
    public static final OnSuccessListener<AuthResult> DEFAULT_AUTH_S_LISTENER = new OnSuccessListener<AuthResult>() {
        @Override
        public void onSuccess(AuthResult authResult) {
            Log.d(TAG, "on " + currentTask + " Success: ");
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
     *
     * @param timestamp time stamp
     * @return a string.
     */
    public static String timeStampToEventTimeString(Timestamp timestamp) {
        return EVENT_TIME_FORMAT.format(timestamp.toDate());
    }

    /**
     * Create a random uid string
     *
     * @return a random uid
     */
    public static String createUid() {
        return UUID.randomUUID().toString();
    }

    /**
     * private constructor for singleton.
     */
    private Util() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    /**
     * Get the singleton util object.
     */
    public static Util getInstance() {
        if (util == null) {
            util = new Util();
        }
        return util;
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    /**
     * Use email and PW to create a user account.
     *
     * @param email     email
     * @param password  password
     * @param userName  userName
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
                                if (task.isSuccessful()) {
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
     *
     * @param email     email
     * @param password  password
     * @param sListener OnSuccessListener<AuthResult>
     * @param fListener onFailure Listener
     */
    public void loginUserWithEmailAndPassword(String email, String password, OnSuccessListener<AuthResult> sListener, OnFailureListener fListener) {
        currentTask = "loginUserWithEmailAndPassword";
        mAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(sListener).addOnFailureListener(fListener);
    }

    /**
     * Get the user with the user's uid. For current user, use util.getmAuth.getUid() to get its uid.
     *
     * @param uid       the uid of a user
     * @param sListener OnSuccessListener<User>
     * @param fListener onFailure Listener
     */
    public void getUser(String uid, OnSuccessListener<User> sListener, OnFailureListener fListener) {
        currentTask = "getUser";
        db.collection(USERS_COLLECTION_NAME)
                .document(uid)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User user = documentSnapshot.toObject(User.class);
                        sListener.onSuccess(user);
                    }
                })
                .addOnFailureListener(fListener);
    }

    /**
     * Update the db to follow a user. This is a transaction.
     *
     * @param otherUserUid the uid of user to follow.
     * @param sListener    OnSuccessListener<Void>
     * @param fListener    OnFailureListener
     */
    public void followUser(String otherUserUid, OnSuccessListener<Void> sListener, OnFailureListener fListener) {
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
     *
     * @param otherUserUid the uid of user to follow.
     * @param sListener    OnSuccessListener<Void>
     * @param fListener    OnFailureListener
     */
    public void unfollowUser(String otherUserUid, OnSuccessListener<Void> sListener, OnFailureListener fListener) {
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
     *
     * @param event     an Event object.
     * @param sListener OnSuccessListener<Void>
     * @param fListener OnFailureListener
     */
    public void saveDraftEvent(Event event, OnSuccessListener<Void> sListener, OnFailureListener fListener) {
        db.collection(USERS_COLLECTION_NAME)
                .document(mAuth.getUid())
                .update(User.KEY_DRAFT_EVENT, event)
                .addOnSuccessListener(sListener)
                .addOnFailureListener(fListener);
    }

    /**
     * Update the db to publish an event. This is a transaction.
     *
     * @param event     an Event object.
     * @param sListener OnSuccessListener<Void>
     * @param fListener OnFailureListener
     */
    public void postEvent(Event event, OnSuccessListener<Void> sListener, OnFailureListener fListener) {
        currentTask = "postEvent";
        DocumentReference eventRef = db.collection(EVENTS_COLLECTION_NAME).document(event.getUid());
        DocumentReference userRef = db.collection(USERS_COLLECTION_NAME).document(mAuth.getUid());
        db.runTransaction(new Transaction.Function<Void>() {
                    @Nullable
                    @Override
                    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                        transaction.update(userRef, User.KEY_DRAFT_EVENT, null);
                        transaction.update(userRef, User.KEY_POSTS_ID_LIST, FieldValue.arrayUnion(event.getUid()));
                        transaction.set(eventRef, event);
                        Message message = event.createUpdateMessage(getCurrentUser());

                        for (String participantId : event.getParticipants()) {
                            DocumentReference participantRef = db.collection(USERS_COLLECTION_NAME).document(participantId);
                            //send notification to users that are affected by deletion.
                            sendMessage(participantId, message, DEFAULT_VOID_S_LISTENER, DEFAULT_F_LISTENER);
                        }

                        for (String candidateId : event.getCandidates()) {
                            DocumentReference participantRef = db.collection(USERS_COLLECTION_NAME).document(candidateId);
                            //send notification to users that are affected by deletion.
                            sendMessage(candidateId, message, DEFAULT_VOID_S_LISTENER, DEFAULT_F_LISTENER);
                        }

                        return null;
                    }
                })
                .addOnSuccessListener(sListener)
                .addOnFailureListener(fListener);
    }

    /**
     * Update the db to delete an event. It will also update users' join list and send a message to users joined the event. This is a transaction.
     *
     * @param eventUid  uid of the event to delete.
     * @param sListener OnSuccessListener<Void>
     * @param fListener OnFailureListener
     */
    public void deleteEvent(String eventUid, OnSuccessListener<Void> sListener, OnFailureListener fListener) {
        currentTask = "deleteEvent";
        DocumentReference eventRef = db.collection(EVENTS_COLLECTION_NAME).document(eventUid);
        DocumentReference userRef = db.collection(USERS_COLLECTION_NAME).document(mAuth.getUid());
        db.runTransaction(new Transaction.Function<Void>() {
                    @Nullable
                    @Override
                    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                        Event event = transaction.get(eventRef).toObject(Event.class);
                        Message message = event.createDeletionMessage(getCurrentUser());
                        for (String participantId : event.getParticipants()) {
                            DocumentReference participantRef = db.collection(USERS_COLLECTION_NAME).document(participantId);
                            //send notification to users that are affected by deletion.
                            sendMessage(participantId, message, DEFAULT_VOID_S_LISTENER, DEFAULT_F_LISTENER);
                            transaction.update(participantRef, User.KEY_JOINED_EVENTS_ID_LIST, FieldValue.arrayRemove(eventUid));
                        }

                        for (String candidateId : event.getCandidates()) {
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
     *
     * @param eventUid  uid of the event to add to favourite
     * @param sListener OnSuccessListener<Void>
     * @param fListener OnFailureListener
     */
    public void addFavouriteEvent(String eventUid, OnSuccessListener<Void> sListener, OnFailureListener fListener) {
        currentTask = "addFavouriteEvent";
        db.collection(USERS_COLLECTION_NAME)
                .document(mAuth.getUid())
                .update(User.KEY_FAVOURITES_ID_LIST, FieldValue.arrayUnion(eventUid))
                .addOnSuccessListener(sListener)
                .addOnFailureListener(fListener);
    }

    /**
     * Update the db to remove an event from favourite.
     *
     * @param eventUid  uid of the event to remove from favourite
     * @param sListener OnSuccessListener<Void>
     * @param fListener OnFailureListener
     */
    public void removeFavouriteEvent(String eventUid, OnSuccessListener<Void> sListener, OnFailureListener fListener) {
        currentTask = "removeFavouriteEvent";
        db.collection(USERS_COLLECTION_NAME)
                .document(mAuth.getUid())
                .update(User.KEY_FAVOURITES_ID_LIST, FieldValue.arrayRemove(eventUid))
                .addOnSuccessListener(sListener)
                .addOnFailureListener(fListener);
    }

    /**
     * Update the db to join an event. It will also send a message to the host. This is a transaction.
     *
     * @param eventUid  uid of the event to join.
     * @param sListener OnSuccessListener<Void>
     * @param fListener OnFailureListener
     */
    public void joinEvent(String eventUid, OnSuccessListener<Void> sListener, OnFailureListener fListener) {
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
     * Update the db to quit from an event. It will also send a message to the host. This is a transaction.
     *
     * @param eventUid  Uid of event to quit from
     * @param sListener OnSuccessListener<Void>
     * @param fListener OnFailureListener
     */
    public void quitEvent(String eventUid, OnSuccessListener<Void> sListener, OnFailureListener fListener) {
        currentTask = "quitEvent";
        DocumentReference eventRef = db.collection(EVENTS_COLLECTION_NAME).document(eventUid);
        DocumentReference selfUserRef = db.collection(USERS_COLLECTION_NAME).document(mAuth.getUid());
        db.runTransaction(new Transaction.Function<Void>() {
                    @Nullable
                    @Override
                    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                        Event event = transaction.get(eventRef).toObject(Event.class);
                        //send notification to users that are affected by approval.
                        Message message = event.createQuitMessage(getCurrentUser());
                        if (event.getParticipants().contains(mAuth.getUid())) {
                            sendMessageWithTransaction(transaction, event.getHostId(), message);
                        }
                        transaction.update(selfUserRef, User.KEY_JOINED_EVENTS_ID_LIST, FieldValue.arrayRemove(eventUid));
                        transaction.update(eventRef, Event.KEY_CANDIDATES, FieldValue.arrayRemove(mAuth.getUid()));
                        transaction.update(eventRef, Event.KEY_PARTICIPANTS, FieldValue.arrayRemove(mAuth.getUid()));
                        return null;
                    }
                })
                .addOnSuccessListener(sListener)
                .addOnFailureListener(fListener);
    }

    /**
     * Update the db to approve a user to join a event. It will also send a message to the candidate approved. This is a transaction.
     *
     * @param otherUserUid the uid of user to approve
     * @param eventUid     the uid of event.
     * @param sListener    OnSuccessListener<Void>
     * @param fListener    OnFailureListener
     */
    public void approveJoinEvent(String otherUserUid, String eventUid, OnSuccessListener<Void> sListener, OnFailureListener fListener) {
        currentTask = "approveJoinEvent";
        DocumentReference eventRef = db.collection(EVENTS_COLLECTION_NAME).document(eventUid);
        DocumentReference otherUserRef = db.collection(USERS_COLLECTION_NAME).document(otherUserUid);
        db.runTransaction(new Transaction.Function<Void>() {
                    @Nullable
                    @Override
                    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                        Event event = transaction.get(eventRef).toObject(Event.class);
                        if (event.getParticipants().size() >= event.getCapacity()) {
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

    /**
     * Update the db to reject a user to join an event. It will also send a message to the candidate rejected. This is a transaction.
     *
     * @param otherUserUid the uid of user to reject
     * @param eventUid     the uid of event.
     * @param sListener    OnSuccessListener<Void>
     * @param fListener    OnFailureListener
     */
    public void rejectJoinEvent(String otherUserUid, String eventUid, OnSuccessListener<Void> sListener, OnFailureListener fListener) {
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

    /**
     * Update the db to send a user a message. it will also update the user's and chats' unreadCount. This is a transaction.
     *
     * @param otherUserUid the uid of user to reject
     * @param message      the message to send.
     * @param sListener    OnSuccessListener<Void>
     * @param fListener    OnFailureListener
     */
    public void sendMessage(String otherUserUid, Message message, OnSuccessListener<Void> sListener, OnFailureListener fListener) {
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

    /**
     * Read the chat and update the read count in user and chat. This is a transaction.
     *
     * @param otherUserUid the uid of user to chat with.
     * @param sListener    OnSuccessListener<Void>
     * @param fListener    OnFailureListener
     */
    public void readChat(String otherUserUid, OnSuccessListener<Void> sListener, OnFailureListener fListener) {
        currentTask = "read Chat";
        DocumentReference userRef = db.collection(USERS_COLLECTION_NAME).document(mAuth.getUid());
        DocumentReference chatRef = userRef.collection(Util.CHATS_COLLECTION_NAME).document(otherUserUid);
        db.runTransaction(new Transaction.Function<Void>() {
                    @Nullable
                    @Override
                    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                        int unreadCount = transaction.get(chatRef).get(Chat.KEY_UNREAD_Count, int.class);
                        transaction.update(chatRef, Chat.KEY_UNREAD_Count, 0);
                        transaction.update(userRef, User.KEY_UNREAD_COUNT, FieldValue.increment(-unreadCount));
                        return null;
                    }
                })
                .addOnSuccessListener(sListener)
                .addOnFailureListener(fListener);
    }

    /**
     * Delete the chat and update the read count in user and chat. This will not delete chat on other user's side. This is a transaction.
     *
     * @param otherUserUid the uid of user to chat with.
     * @param sListener    OnSuccessListener<Void>
     * @param fListener    OnFailureListener
     */
    public void deleteChat(String otherUserUid, OnSuccessListener<Void> sListener, OnFailureListener fListener) {
        currentTask = "deleteChat";
        DocumentReference userRef = db.collection(USERS_COLLECTION_NAME).document(mAuth.getUid());
        DocumentReference chatRef = userRef.collection(Util.CHATS_COLLECTION_NAME).document(otherUserUid);
        chatRef.collection(MESSAGES_COLLECTION_NAME).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                db.runTransaction(new Transaction.Function<Void>() {
                            @Nullable
                            @Override
                            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                                int unreadCount = transaction.get(chatRef).get(Chat.KEY_UNREAD_Count, int.class);
                                for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                                    DocumentReference messageRef = chatRef.collection(MESSAGES_COLLECTION_NAME).document(snapshot.get(Message.KEY_MESSAGE_UID, String.class));
                                    transaction.delete(messageRef);
                                }
                                transaction.update(userRef, User.KEY_UNREAD_COUNT, FieldValue.increment(-unreadCount));
                                transaction.delete(chatRef);
                                return null;
                            }
                        })
                        .addOnSuccessListener(sListener)
                        .addOnFailureListener(fListener);
            }
        }).addOnFailureListener(fListener);
    }

    /**
     * Get a List of Chats order by latest message time in descending order.
     *
     * @param sListener OnSuccessListener<List<Chat>>
     * @param fListener OnFailureListener
     */
    public void getChats(OnSuccessListener<List<Chat>> sListener, OnFailureListener fListener) {
        currentTask = "getChats";
        DocumentReference userRef = db.collection(USERS_COLLECTION_NAME).document(mAuth.getUid());
        userRef.collection(CHATS_COLLECTION_NAME)
                .orderBy(Chat.KEY_LATEST_MESSAGE_TIME, Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<Chat> chats = queryDocumentSnapshots.toObjects(Chat.class);
                        sListener.onSuccess(chats);
                    }
                }).addOnFailureListener(fListener);
    }

    /**
     * Get a Chat by otherUser's Uid
     *
     * @param sListener OnSuccessListener<Chat>
     * @param fListener OnFailureListener
     */
    public void getChat(String otherUserId, OnSuccessListener<Chat> sListener, OnFailureListener fListener) {
        currentTask = "getChat";
        DocumentReference userRef = db.collection(USERS_COLLECTION_NAME).document(mAuth.getUid());
        DocumentReference chatRef = userRef.collection(CHATS_COLLECTION_NAME).document(otherUserId);
        chatRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (!documentSnapshot.exists()) {
                            Chat chat = new Chat(otherUserId);
                            chatRef.set(chat).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    sListener.onSuccess(chat);
                                }
                            }).addOnFailureListener(fListener);
                        } else {
                            Chat chat = documentSnapshot.toObject(Chat.class);
                            sListener.onSuccess(chat);
                        }
                    }
                }).addOnFailureListener(fListener);
    }

    /**
     * Get a List of Message by otherUserId. Message will be ascending ordered by time.
     *
     * @param otherUserId the id of other user chat with
     * @param sListener   OnSuccessListener<List<Message>>
     * @param fListener   OnFailureListener
     */
    public void getMessages(String otherUserId, OnSuccessListener<List<Message>> sListener, OnFailureListener fListener) {
        DocumentReference userRef = db.collection(USERS_COLLECTION_NAME).document(mAuth.getUid());
        DocumentReference chatRef = userRef.collection(CHATS_COLLECTION_NAME).document(otherUserId);
        chatRef.collection(MESSAGES_COLLECTION_NAME)
                .orderBy(Message.KEY_TIME, Query.Direction.ASCENDING)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<Message> messages = queryDocumentSnapshots.toObjects(Message.class);
                        sListener.onSuccess(messages);
                    }
                }).addOnFailureListener(fListener);
    }

    public static enum FollowType {FOLLOWER, FOLLOWING}

    /**
     * Get a User List containing the following of a user.
     *
     * @param userUid   the user uid  to get the followings from.
     * @param sListener OnSuccessListener<List<User>>
     * @param fListener OnFailureListener
     */
    public void getFollowing(String userUid, OnSuccessListener<List<User>> sListener, OnFailureListener fListener) {
        currentTask = "getFollowing";
        getUsersByType(userUid, FollowType.FOLLOWING, sListener, fListener);
    }

    /**
     * Get a User List containing the followers of a user.
     *
     * @param userUid   the user uid to get the followers from.
     * @param sListener OnSuccessListener<List<User>>
     * @param fListener OnFailureListener
     */
    public void getFollowers(String userUid, OnSuccessListener<List<User>> sListener, OnFailureListener fListener) {
        currentTask = "getFollowers";
        getUsersByType(userUid, FollowType.FOLLOWER, sListener, fListener);
    }

    /**
     * Get a userList containing participants and candidates.
     *
     * @param event     The event to get participants and candidates from.
     * @param sListener OnSuccessListener<List<User>>
     * @param fListener OnFailureListener
     */
    public void getParticipantsAndCandidates(Event event, OnSuccessListener<List<User>> sListener, OnFailureListener fListener) {
        getUsersByIdList(event.getParticipantsAndCandidates(), sListener, fListener);
    }

    /**
     * Get a List of User by Follow Type, this is private method.
     *
     * @param userUid   the uid of user to get followers/following from.
     * @param type      Enum FollowType
     * @param sListener OnSuccessListener<List<User>>
     * @param fListener OnFailureListener
     */
    private void getUsersByType(String userUid, FollowType type, OnSuccessListener<List<User>> sListener, OnFailureListener fListener) {
        DocumentReference userRef = db.collection(USERS_COLLECTION_NAME).document(mAuth.getUid());
        getUser(userUid, new OnSuccessListener<User>() {
            @Override
            public void onSuccess(User user) {
                List<String> usersIdList;
                switch (type) {
                    case FOLLOWER:
                        usersIdList = user.getFollowersIdList();
                        break;
                    case FOLLOWING:
                        usersIdList = user.getFollowingIdList();
                        break;
                    default:
                        usersIdList = new ArrayList<>();
                }

                getUsersByIdList(usersIdList, sListener, fListener);
            }
        }, fListener);
    }

    /**
     * Provide a usersIdList, get a List of Users in the Id List.
     *
     * @param usersIdList List<String> userIdList
     * @param sListener   OnSuccessListener<List<User>>
     * @param fListener   OnFailureListener
     */
    public void getUsersByIdList(List<String> usersIdList, OnSuccessListener<List<User>> sListener, OnFailureListener fListener) {
        List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        List<List<String>> splitUserIds = splitIDList(usersIdList);
        for (List<String> ids : splitUserIds) {
            Task<QuerySnapshot> task = db.collection(USERS_COLLECTION_NAME).whereIn(FieldPath.documentId(), ids).get();
            tasks.add(task);
        }
        Tasks.whenAllComplete(tasks).addOnSuccessListener(new OnSuccessListener<List<Task<?>>>() {
                    @Override
                    public void onSuccess(List<Task<?>> t) {
                        List<User> users = new ArrayList<>();
                        for (Task<QuerySnapshot> task : tasks) {
                            List<User> result = task.getResult().toObjects(User.class);
                            users.addAll(result);
                        }
                        sListener.onSuccess(users);
                    }
                })
                .addOnFailureListener(fListener);
    }

    public static enum EventType {FAVOURITE, JOIN, POST}

    /**
     * Get a List of Event a user joined.
     *
     * @param userUid   the uid of user to get events from
     * @param sListener OnSuccessListener<List<Event>>
     * @param fListener OnFailureListener
     */
    public void getJoinEvents(String userUid, OnSuccessListener<List<Event>> sListener, OnFailureListener fListener) {
        getEventsByType(userUid, EventType.JOIN, sListener, fListener);
    }

    /**
     * Get a List of Event a user posted.
     *
     * @param userUid   the uid of user to get events from
     * @param sListener OnSuccessListener<List<Event>>
     * @param fListener OnFailureListener
     */
    public void getPostEvents(String userUid, OnSuccessListener<List<Event>> sListener, OnFailureListener fListener) {
        getEventsByType(userUid, EventType.POST, sListener, fListener);
    }

    /**
     * Get a List of Event a user favourites.
     *
     * @param userUid   the uid of user to get events from
     * @param sListener OnSuccessListener<List<Event>>
     * @param fListener OnFailureListener
     */
    public void getFavouriteEvents(String userUid, OnSuccessListener<List<Event>> sListener, OnFailureListener fListener) {
        getEventsByType(userUid, EventType.FAVOURITE, sListener, fListener);
    }

    /**
     * Private method. Get a List of Event from a user By type.
     *
     * @param type      Enum EventType
     * @param sListener OnSuccessListener<List<Event>>
     * @param fListener OnFailureListener
     */
    private void getEventsByType(String userUid, EventType type, OnSuccessListener<List<Event>> sListener, OnFailureListener fListener) {
        DocumentReference userRef = db.collection(USERS_COLLECTION_NAME).document(mAuth.getUid());
        getUser(userUid, new OnSuccessListener<User>() {
            @Override
            public void onSuccess(User user) {
                List<String> eventIdList;
                switch (type) {
                    case JOIN:
                        eventIdList = user.getJoinedEventsIdList();
                        break;
                    case POST:
                        eventIdList = user.getPostsIdList();
                        break;
                    case FAVOURITE:
                        eventIdList = user.getFavouritesIdList();
                        break;
                    default:
                        eventIdList = new ArrayList<>();
                }
                getEventsByIdList(eventIdList, sListener, fListener);
            }
        }, fListener);
    }

    /**
     * Provide a List of event Uid, return a List of Event in the Uid list.
     *
     * @param eventIdList List<String> a List of event id.
     * @param sListener   OnSuccessListener<List<Event>>
     * @param fListener   OnFailureListener
     */
    public void getEventsByIdList(List<String> eventIdList, OnSuccessListener<List<Event>> sListener, OnFailureListener fListener) {
        List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        List<List<String>> splitEventIds = splitIDList(eventIdList);
        for (List<String> ids : splitEventIds) {
            Task<QuerySnapshot> task = db.collection(EVENTS_COLLECTION_NAME).whereIn(FieldPath.documentId(), ids).get();
            tasks.add(task);
        }
        Tasks.whenAllComplete(tasks).addOnSuccessListener(new OnSuccessListener<List<Task<?>>>() {
                    @Override
                    public void onSuccess(List<Task<?>> t) {
                        List<Event> events = new ArrayList<>();
                        for (Task<QuerySnapshot> task : tasks) {
                            events.addAll(task.getResult().toObjects(Event.class));
                        }
                        sListener.onSuccess(events);
                    }
                })
                .addOnFailureListener(fListener);
    }

    /**
     * Get an Event based on event uid.
     *
     * @param uid       the uid of event to get.
     * @param sListener OnSuccessListener<Event>
     * @param fListener OnFailureListener
     */
    public void getEvent(String uid, OnSuccessListener<Event> sListener, OnFailureListener fListener) {
        currentTask = "getEvent";
        db.collection(EVENTS_COLLECTION_NAME)
                .document(uid)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Event event = documentSnapshot.toObject(Event.class);
                        sListener.onSuccess(event);
                    }
                })
                .addOnFailureListener(fListener);
    }

    /**
     * Provide a geoPoint, and search radius, get a List of Event in that search radius. The resulting list is not sorted.
     *
     * @param geoPoint  the start location of search.
     * @param radius    the search radius, unit: Meter.
     * @param sListener OnSuccessListener<List<Event>>
     * @param fListener OnFailureListener
     */
    public void getNearByEvents(GeoPoint geoPoint, double radius, OnSuccessListener<List<Event>> sListener, OnFailureListener fListener) {
        GeoLocation location = new GeoLocation(geoPoint.getLatitude(), geoPoint.getLongitude());
        String geoHash = GeoFireUtils.getGeoHashForLocation(location);
        List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        List<GeoQueryBounds> bounds = GeoFireUtils.getGeoHashQueryBounds(location, radius);
        for (GeoQueryBounds b : bounds) {
            Query q = db.collection(EVENTS_COLLECTION_NAME)
                    .orderBy(Event.KEY_GEO_HASH)
                    .startAt(b.startHash)
                    .endAt(b.endHash);

            tasks.add(q.get());
        }
        Tasks.whenAllComplete(tasks).addOnSuccessListener(new OnSuccessListener<List<Task<?>>>() {
                    @Override
                    public void onSuccess(List<Task<?>> t) {
                        List<Event> events = new ArrayList<>();
                        for (Task<QuerySnapshot> task : tasks) {
                            events.addAll(task.getResult().toObjects(Event.class));
                        }
                        sListener.onSuccess(events);
                    }
                })
                .addOnFailureListener(fListener);
    }

    /**
     * Get a List of Events that are posted by users' friends. The resulting list is not sorted.
     *
     * @param sListener OnSuccessListener<List<Event>>
     * @param fListener OnFailureListener
     */
    public void getFriendEvents(OnSuccessListener<List<Event>> sListener, OnFailureListener fListener) {
        DocumentReference userRef = db.collection(USERS_COLLECTION_NAME).document(mAuth.getUid());
        getFollowing(mAuth.getUid(), new OnSuccessListener<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                ArrayList<String> eventIds = new ArrayList<>();
                for (User user : users) {
                    eventIds.addAll(user.getPostsIdList());
                }
                List<Task<QuerySnapshot>> tasks = new ArrayList<>();
                //Timestamp cutOffTime = new Timestamp(new Date(System.currentTimeMillis() - CUT_OFF_TIME_MILLISECONDS));
                List<List<String>> splitEventIds = splitIDList(eventIds);
                for (List<String> ids : splitEventIds) {
                    Task<QuerySnapshot> task = db.collection(EVENTS_COLLECTION_NAME).whereIn(FieldPath.documentId(), ids).get();
                    tasks.add(task);
                }
                Tasks.whenAllComplete(tasks).addOnSuccessListener(new OnSuccessListener<List<Task<?>>>() {
                            @Override
                            public void onSuccess(List<Task<?>> t) {
                                List<Event> events = new ArrayList<>();
                                for (Task<QuerySnapshot> task : tasks) {
                                    events.addAll(task.getResult().toObjects(Event.class));
                                }
                                sListener.onSuccess(events);
                            }
                        })
                        .addOnFailureListener(fListener);
            }
        }, fListener);
    }

    /**
     * send a message within a transaction. This method can only be used before the transaction update anything in the database.
     *
     * @param transaction  the transaction, which SHOULD NOT update anything in the database before passed into this method.
     * @param otherUserUid the uid of the user this message is sending to.
     * @param message      the message.
     * @throws FirebaseFirestoreException
     */
    private void sendMessageWithTransaction(Transaction transaction, String otherUserUid, Message message) throws FirebaseFirestoreException {
        DocumentReference selfChatRef = db.collection(USERS_COLLECTION_NAME).document(mAuth.getUid()).collection(CHATS_COLLECTION_NAME).document(otherUserUid);
        DocumentReference otherChatRef = db.collection(USERS_COLLECTION_NAME).document(otherUserUid).collection(CHATS_COLLECTION_NAME).document(mAuth.getUid());
        boolean selfChatExists = transaction.get(selfChatRef).exists();
        boolean otherChatExists = transaction.get(otherChatRef).exists();
        DocumentReference otherUserRef = db.collection(USERS_COLLECTION_NAME).document(otherUserUid);
        if (!selfChatExists) {
            transaction.set(selfChatRef, new Chat(otherUserUid));
        }
        if (!otherChatExists) {
            transaction.set(otherChatRef, new Chat(mAuth.getUid()));
        }

        DocumentReference selfMessageRef = selfChatRef.collection(MESSAGES_COLLECTION_NAME).document(message.getMessageUid());
        DocumentReference otherMessageRef = otherChatRef.collection(MESSAGES_COLLECTION_NAME).document(message.getMessageUid());

        transaction.set(selfMessageRef, message);
        transaction.update(selfMessageRef, Message.KEY_TIME, FieldValue.serverTimestamp());
        transaction.update(selfChatRef, Chat.KEY_LATEST_MESSAGE, message);
        transaction.update(selfChatRef, Chat.KEY_LATEST_MESSAGE_TIME, FieldValue.serverTimestamp());

        transaction.set(otherMessageRef, message);
        transaction.update(otherMessageRef, Message.KEY_TIME, FieldValue.serverTimestamp());
        transaction.update(otherChatRef, Chat.KEY_LATEST_MESSAGE, message);
        transaction.update(otherChatRef, Chat.KEY_LATEST_MESSAGE_TIME, FieldValue.serverTimestamp());
        transaction.update(otherChatRef, Chat.KEY_UNREAD_Count, FieldValue.increment(1));
        transaction.update(otherUserRef, User.KEY_UNREAD_COUNT, FieldValue.increment(1));
    }

    public void updateProfile(String avatarPath, String about, OnSuccessListener<Void> sListener, OnFailureListener fListener) {
        DocumentReference userRef = db.collection(USERS_COLLECTION_NAME).document(mAuth.getUid());
        db.runTransaction(new Transaction.Function<Void>() {
                    @Nullable
                    @Override
                    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                        transaction.update(userRef, User.KEY_AVATAR_Path, avatarPath);
                        transaction.update(userRef, User.KEY_ABOUT, about);
                        return null;
                    }
                })
                .addOnSuccessListener(sListener)
                .addOnFailureListener(fListener);
    }

    public void getDownloadUrlFromPath(String path, OnSuccessListener<Uri> sListener, OnFailureListener fListener) {
        if (path == null || path == "") {
            return;
        }
        currentTask = "loadImageWithPath";
        storage.getReference()
                .child(path)
                .getDownloadUrl()
                .addOnSuccessListener(sListener)
                .addOnFailureListener(fListener);
    }

    /**
     * Group ids in the list by 10 and return a list of id list groups.
     * This is used for querying with WhereIn method, which has a limit of 10 ids per query.
     *
     * @param idList List<String> the id list to split
     * @return List<List < String>> a list of sub id list, each sub id list contains a maximum of 10 ids.
     */
    public List<List<String>> splitIDList(List<String> idList) {
        if (idList == null) {
            return new ArrayList<>();
        }
        int size = idList.size();
        List<List<String>> list = new ArrayList<>();
        for (int i = 0; i < (size + 9) / 10; i++) {
            list.add(new ArrayList<>());
        }
        for (int i = 0; i < size; i++) {
            list.get(i / 10).add(idList.get(i));
        }
        return list;
    }

    /**
     * get the FirebaseAuth instance.
     *
     * @return the FirebaseAuth instance.
     */
    public FirebaseAuth getmAuth() {
        return mAuth;
    }

    /**
     * get the FirebaseFirestore instance (aka the database).
     *
     * @return the FirebaseFirestore instance (aka the database)..
     */
    public FirebaseFirestore getDB() {
        return db;
    }

    /**
     * get the FirebaseStorage instance.
     *
     * @return the FirebaseStorage instance.
     */
    public FirebaseStorage getStorage() {
        return storage;
    }
}