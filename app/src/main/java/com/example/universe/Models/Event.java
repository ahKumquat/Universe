package com.example.universe.Models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Event {
    private String eventId;
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

    public Event(String eventId, User user, Timestamp time, double duration, String durationUnit, String address, GeoPoint geoPoint, int capacity, String description, String imageURL) {
        this.eventId = eventId;
        this.hostId = user.getUserId();
        this.hostName = user.getUserName();
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
}
