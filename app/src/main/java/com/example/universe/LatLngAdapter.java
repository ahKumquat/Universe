package com.example.universe;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class LatLngAdapter extends TypeAdapter<LatLng> {

    @Override
    public LatLng read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }

        double lat = 0;
        double lng = 0;
        boolean hasLat = false;
        boolean hasLng = false;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if ("lat".equals(name) || "latitude".equals(name)) {
                lat = reader.nextDouble();
                hasLat = true;
            } else if ("lng".equals(name) || "longitude".equals(name)) {
                lng = reader.nextDouble();
                hasLng = true;
            }
        }
        reader.endObject();

        if (hasLat && hasLng) {
            return new LatLng(lat, lng);
        } else {
            return null;
        }
    }

    @Override
    public void write(JsonWriter out, LatLng value) {
        throw new UnsupportedOperationException("Unimplemented method.");
    }
}
