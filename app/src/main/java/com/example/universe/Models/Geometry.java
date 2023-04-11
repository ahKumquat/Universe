package com.example.universe.Models;


import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

public class Geometry implements Serializable {
    private LatLng location;

    public LatLng getLocation() {
        return location;
    }
}
