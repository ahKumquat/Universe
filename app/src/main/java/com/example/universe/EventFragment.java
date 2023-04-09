package com.example.universe;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Icon;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.SimpleDateFormat;

//TODO Have to implement the four buttons

public class EventFragment extends Fragment {

    private static final String ARG_EVENT = "event";
    private static final String ARG_USER = "user";

    private IEventFragmentAction mListener;
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

    private User me;

    private RecyclerView.LayoutManager recyclerViewLayoutManager;

    public EventFragment() {
        // Required empty public constructor
    }

    public static EventFragment newInstance(Event event, User user) {
        EventFragment fragment = new EventFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_EVENT, event);
        args.putSerializable(ARG_USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable(ARG_EVENT);
            me = (User) getArguments().getSerializable(ARG_USER);
        }
        //TODO need to finish backpressed()
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

        if (event.getImagePath() != null) {
            util.getDownloadUrlFromPath(event.getImagePath(), uri -> Glide.with(requireContext())
                    .load(uri)
                    .error(R.drawable.image_not_found)
                    .into(eventPic), Util.DEFAULT_F_LISTENER);
        }

        util.getUser(event.getHostId(), user -> {
            if (user.getAvatarPath() != null) {
                util.getDownloadUrlFromPath(user.getAvatarPath(), uri -> Glide.with(requireContext())
                        .load(uri)
                        .error(R.drawable.circle_user_avatar)
                        .into(hostAvatar), Util.DEFAULT_F_LISTENER);
            }
        }, Util.DEFAULT_F_LISTENER);

        hostName.setText(event.getHostName());

        date.setText(new SimpleDateFormat("MMM dd").format(event.getTime().toDate()));

        time.setText(new SimpleDateFormat("H:mm").format(event.getTime().toDate()));

        duration.setText(event.getDuration() + " " + event.getDurationUnit());

        location.setText(event.getAddress());

        description.setText(event.getDescription());

        showMap(event.getGeoPoint().getLatitude(), event.getGeoPoint().getLongitude());

        recyclerViewLayoutManager = new LinearLayoutManager(getContext());

        if (me.getJoinedEventsIdList().contains(event.getUid())) {
            signUpButton.setImageIcon(Icon.createWithResource(requireContext(), R.drawable.check));
        }

        signUpButton.setOnClickListener(v -> {
            if (!me.getJoinedEventsIdList().contains(event.getUid())) {
                util.joinEvent(event.getUid(), unused -> {
                        signUpButton.setImageIcon(Icon.createWithResource(requireContext(), R.drawable.check));
                        me.getJoinedEventsIdList().add(event.getUid());
                }, Util.DEFAULT_F_LISTENER);
            } else {
                util.quitEvent(event.getUid(), unused -> {
                    signUpButton.setImageIcon(Icon.createWithResource(requireContext(), R.drawable.baseline_check_24));
                    me.getJoinedEventsIdList().remove(event.getUid());
                }, Util.DEFAULT_F_LISTENER);
            }
        });

        if (me.getFavouritesIdList().contains(event.getUid())) {
            favouriteButton.setImageTintList(ColorStateList.valueOf(Color.parseColor("#FF934D")));
        }
        favouriteButton.setOnClickListener(v -> {
            if (!me.getFavouritesIdList().contains(event.getUid())) {
                util.addFavouriteEvent(event.getUid(), unused -> {
                    favouriteButton.setImageTintList(ColorStateList.valueOf(Color.parseColor("#FF934D")));
                    me.getFavouritesIdList().add(event.getUid());
                    }, Util.DEFAULT_F_LISTENER);
            } else {
                util.removeFavouriteEvent(event.getUid(), unused -> {
                    favouriteButton.setImageTintList(null);
                    me.getFavouritesIdList().remove(event.getUid());
                }, Util.DEFAULT_F_LISTENER);
            }
        });

        if (!event.getHostId().equals(util.getCurrentUser().getUid())) {
            postButton.setOnClickListener(v -> mListener.startChatPageFromEvent(event.getHostId()));
        } else {
            postButton.setVisibility(View.INVISIBLE);
            signUpButton.setVisibility(View.INVISIBLE);
        }


        return view;
    }

    private void showMap(double lat, double lon) {
        mapView.onCreate(null);
        mapView.onResume();
        mapView.getMapAsync(googleMap -> {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 13f));
            googleMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)));
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            googleMap.getUiSettings().setAllGesturesEnabled(false);
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IEventFragmentAction){
            this.mListener = (IEventFragmentAction) context;
        }else{
            throw new RuntimeException(context + "must implement IEventFragmentAction");
        }
    }

    public interface IEventFragmentAction {
        void backToPrevious();
        void populateHomeFragment();
        void startChatPageFromEvent(String otherUserId);
    }
}