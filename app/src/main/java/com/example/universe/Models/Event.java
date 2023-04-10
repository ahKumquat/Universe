package com.example.universe.Models;


import com.example.universe.Util;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The Event class represents an event.
 */
public class Event implements Serializable {
    public static final String KEY_UID = "uid";
    public static final String KEY_HOST_ID = "hostId";
    public static final String KEY_TITLE = "title";
    public static final String KEY_HOST_Name = "hostName";
    public static final String KEY_TIME_STAMP = "time";
    public static final String KEY_DURATION = "duration";
    public static final String KEY_DURATION_UNIT = "durationUnit";
    public static final String KEY_GEO_POINT = "geoPoint";
    public static final String KEY_GEO_HASH = "geoHash";
    public static final String KEY_CAPACITY = "capacity";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_IMAGE_PATH = "imagePath";
    public static final String KEY_PARTICIPANTS = "participants";
    public static final String KEY_CANDIDATES = "candidates";
    public static final String[] UNITS = {"min", "hour", "day", "month"};
    public static final String UNIT_MIN = UNITS[0];
    public static final String UNIT_HOUR = UNITS[1];
    public static final String UNIT_DAY = UNITS[2];
    public static final String UNIT_MONTH = UNITS[3];
    private String uid;
    private String title;
    private String hostId;
    private String hostName;
    private transient Timestamp time; // For implementing Serializable
    private double duration;
    private String durationUnit;
    private String address;
    private transient GeoPoint geoPoint; // For implementing Serializable
    private String geoHash;
    private int capacity;
    private String description;
    private String imagePath;
    private List<String> participants;
    private List<String> candidates;

    public Event() {
    }

    public Event(String uid, FirebaseUser user, String title,Timestamp time, double duration, String durationUnit, String address, GeoPoint geoPoint, int capacity, String description, String imagePath) {
        this.uid = uid;
        this.hostId = user.getUid();
        this.hostName = user.getDisplayName();
        this.title = title;
        this.time = time;
        this.duration = duration;
        this.durationUnit = durationUnit;
        this.address = address;
        this.geoPoint = geoPoint;
        this.geoHash = GeoFireUtils.getGeoHashForLocation(new GeoLocation(geoPoint.getLatitude(), geoPoint.getLongitude()));
        this.capacity = capacity;
        this.description = description;
        this.imagePath = imagePath;
        this.participants = new ArrayList<>();
        this.candidates = new ArrayList<>();
    }

    /**
     * Create an apply message based on event info. Use when user applies for an event.
     * @param user should pass in the current authorized user.
     * @return a message to be sent.
     */
    public Message createApplyMessage(FirebaseUser user){
        String text = "Hi, "+ hostName +"! I'd like to join your event: \"" + title + "\". \n";
        return new Message(user, text, null, null);
    }

    /**
     * Create a quit message based on event info. Use when user applies for an event.
     * @param user should pass in the current authorized user.
     * @return a message to be sent.
     */
    public Message createQuitMessage(FirebaseUser user){
        String text = "Hi, "+ hostName +"! I'm sorry that I have to quit your event: \"" + title + "\". \n";
        return new Message(user, text, null,null);
    }

    /**
     * Create an approve message based on event info.
     * @param user should pass in the current authorized user.
     * @return a message to be sent.
     */
    public Message createApproveMessage(FirebaseUser user){
        String text = "Congratulations! You were approved to participate in the event: \"" + title + "\".\n"
                + "Time: " + Util.timeStampToEventTimeString(time);
        return new Message(user, text, null,null);
    }

    /**
     * Create a reject message based on event info. Use when user rejects someone.
     * @param user should pass in the current authorized user.
     * @return a message to be sent.
     */
    public Message createRejectMessage(FirebaseUser user){
        String text = "Your application to join in the event: \"" + title + "\" is rejected.\n"
                + "Time: " + Util.timeStampToEventTimeString(time);
        return new Message(user, text, null,null);
    }

    /**
     * Create a delete message based on event info. Use when user deletes an event.
     * @param user should pass in the current authorized user.
     * @return a message to be sent.
     */
    public Message createDeletionMessage(FirebaseUser user){
        String text = "The event: \""+ title +"\" has been deleted.\n"
                + "Time: " + Util.timeStampToEventTimeString(time);
        return new Message(user, text, null, null);
    }

    /**
     * Create an update message based on event info. Use when user updates an event.
     * @param user should pass in the current authorized user.
     * @return a message to be sent.
     */
    public Message createUpdateMessage(FirebaseUser user){
        String text = "The event: \""+ title +"\" has changed state.\n"
                + "Time: " + Util.timeStampToEventTimeString(time);
        return new Message(Util.getInstance().getCurrentUser(), text, null, null);
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public String getDurationUnit() {
        return durationUnit;
    }

    public void setDurationUnit(String durationUnit) {
        this.durationUnit = durationUnit;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
        this.geoHash = GeoFireUtils.getGeoHashForLocation(new GeoLocation(geoPoint.getLatitude(), geoPoint.getLongitude()));
    }

    public String getGeoHash() {
        return geoHash;
    }

    public void setGeoHash(String geoHash) {
        this.geoHash = geoHash;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public List<String> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<String> candidates) {
        this.candidates = candidates;
    }

    @Exclude
    public List<String> getParticipantsAndCandidates(){
        ArrayList<String> list = new ArrayList<>(participants);
        list.addAll(candidates);
        return list;
    }


    @Override
    public String toString() {
        return "Event{" +
                "uid='" + uid + '\'' +
                ", title='" + title + '\'' +
                ", hostId='" + hostId + '\'' +
                ", hostName='" + hostName + '\'' +
                ", time=" + time +
                ", duration=" + duration +
                ", durationUnit='" + durationUnit + '\'' +
                ", address='" + address + '\'' +
                ", geoPoint=" + geoPoint +
                ", capacity=" + capacity +
                ", imageURL='" + imagePath + '\'' +
                ", participants=" + participants +
                ", candidates=" + candidates +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Double.compare(event.getDuration(), getDuration()) == 0 && getCapacity() == event.getCapacity() && Objects.equals(getUid(), event.getUid()) && Objects.equals(getTitle(), event.getTitle()) && Objects.equals(getHostId(), event.getHostId()) && Objects.equals(getHostName(), event.getHostName()) && Objects.equals(getTime(), event.getTime()) && Objects.equals(getDurationUnit(), event.getDurationUnit()) && Objects.equals(getAddress(), event.getAddress()) && Objects.equals(getGeoPoint(), event.getGeoPoint()) && Objects.equals(getGeoHash(), event.getGeoHash()) && Objects.equals(getDescription(), event.getDescription()) && Objects.equals(getImagePath(), event.getImagePath()) && Objects.equals(getParticipants(), event.getParticipants()) && Objects.equals(getCandidates(), event.getCandidates());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUid(), getTitle(), getHostId(), getHostName(), getTime(), getDuration(), getDurationUnit(), getAddress(), getGeoPoint(), getGeoHash(), getCapacity(), getDescription(), getImagePath(), getParticipants(), getCandidates());
    }
}
