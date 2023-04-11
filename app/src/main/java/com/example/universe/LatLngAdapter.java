// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
    public void write(JsonWriter out, LatLng value) throws IOException {
        throw new UnsupportedOperationException("Unimplemented method.");
    }
}
