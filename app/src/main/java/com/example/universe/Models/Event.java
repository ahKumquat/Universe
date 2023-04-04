package com.example.universe.Models;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class Event {
    public static final String[] UNITS = {"min", "hour", "day", "month"};
    public static final String UNIT_MIN = UNITS[0];
    public static final String UNIT_HOUR = UNITS[1];
    public static final String UNIT_DAY = UNITS[2];
    public static final String UNIT_MONTH = UNITS[3];
    private String uid;
    private String hostId;
    private String hostName;
    private Timestamp time;
    private double duration;
    private String durationUnit;
    private String address;
    private GeoPoint geoPoint;
    private int capacity;
    private String description;
    private String imageURL;
    private List<String> participants;
    private List<String> candidates;

    public Event() {
    }

    public Event(String uid, FirebaseUser user, Timestamp time, double duration, String durationUnit, String address, GeoPoint geoPoint, int capacity, String description, String imageURL) {
        this.uid = uid;
        this.hostId = user.getUid();
        this.hostName = user.getDisplayName();
        this.time = time;
        this.duration = duration;
        this.durationUnit = durationUnit;
        this.address = address;
        this.geoPoint = geoPoint;
        this.capacity = capacity;
        this.description = description;
        this.imageURL = imageURL;
        this.participants = new ArrayList<>();
        this.candidates = new ArrayList<>();
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

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
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
}
