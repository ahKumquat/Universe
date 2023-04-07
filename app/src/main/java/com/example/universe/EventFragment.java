package com.example.universe;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.universe.Models.Event;
import com.example.universe.Models.User;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.SimpleDateFormat;

//TODO Have to implement the four buttons

public class EventFragment extends Fragment {

    private static final String ARG_EVENT = "event";

    private Event event;

    private TextView title;
    private ImageView eventPic;
    private ImageView hostAvatar;
    private TextView hostName;
    private TextView date;
    private TextView time;
    private TextView duration;
    private TextView location;
    private TextView description;
    private MapView mapView;
    private RecyclerView recyclerViewParticipants;
    private ImageButton shareButton;
    private ImageButton signUpButton;
    private ImageButton favouriteButton;
    private ImageButton postButton;
    private static Util util;

    private RecyclerView.LayoutManager recyclerViewLayoutManager;

    public EventFragment() {
        // Required empty public constructor
    }

    public static EventFragment newInstance(Event event) {
        EventFragment fragment = new EventFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_EVENT, event);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable(ARG_EVENT);
        }
        util = Util.getInstance();
    }

    @SuppressLint({"SimpleDateFormat", "SetTextI18n"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_event, container, false);
        title = view.findViewById(R.id.event_textView_toolbar_title);
        eventPic = view.findViewById(R.id.event_imageView_event);
        hostAvatar = view.findViewById(R.id.event_imageView_hostAvatar);
        hostName = view.findViewById(R.id.event_textView_hostName);
        date = view.findViewById(R.id.event_textView_date);
        time = view.findViewById(R.id.event_textView_time);
        duration = view.findViewById(R.id.event_textView_duration);
        location = view.findViewById(R.id.event_textView_location);
        description = view.findViewById(R.id.event_textView_description);
        mapView = view.findViewById(R.id.event_mapView_map);
        recyclerViewParticipants = view.findViewById(R.id.event_recyclerView_participants);
        shareButton = view.findViewById(R.id.event_imageButton_share);
        signUpButton = view.findViewById(R.id.event_imageButton_signUp);
        favouriteButton = view.findViewById(R.id.event_imageButton_favourite);
        postButton = view.findViewById(R.id.event_imageButton_post);

        title.setText(event.getTitle());

        if (!event.getImageURL().equals("")) {
            Glide.with(requireContext()).load(event.getImageURL()).error(R.drawable.image_not_found).into(eventPic);
        }

        util.getUser(event.getHostId(), user -> {
            if (user.getAvatarUrl() != null) {
                Glide.with(requireContext()).load(Uri.parse(user.getAvatarUrl()))
                        .into(hostAvatar);
            }
        }, Util.DEFAULT_F_LISTENER);

        date.setText(new SimpleDateFormat("MMM dd").format(event.getTime().toDate()));

        time.setText(new SimpleDateFormat("H:mm").format(event.getTime().toDate()));

        duration.setText(event.getDuration() + " " + event.getDurationUnit());

        location.setText(event.getAddress());

        description.setText(event.getDescription());

        showMap(event.getGeoPoint().getLatitude(), event.getGeoPoint().getLongitude());

        recyclerViewLayoutManager = new LinearLayoutManager(getContext());






        return view;
    }

    private void showMap(double lat, double lon) {
        mapView.onCreate(null);
        mapView.onResume();
        mapView.getMapAsync(googleMap -> {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lon), 13f));
            googleMap.addMarker(new MarkerOptions().position(new LatLng(lat,lon)));
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            googleMap.getUiSettings().setAllGesturesEnabled(false);
        });
    }
}