package com.example.universe.Models;

import java.io.Serializable;

public class GeocodingResult implements Serializable {
    private String formatted_address;
    private Geometry geometry;

    public Geometry getGeometry() {
        return geometry;
    }

    public String getFormattedAddress() {
        return formatted_address;
    }
}
