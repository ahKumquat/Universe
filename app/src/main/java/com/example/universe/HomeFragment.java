package com.example.universe;


import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.universe.Models.Event;
import com.example.universe.Models.User;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.GeoPoint;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class HomeFragment extends Fragment {
    private ImageButton imageButtonChat;
    private IhomeFragmentAction mListener;

    private OnBackPressedCallback callback;

    private TabLayout tabLayout;


    private ProgressBar progressBar;

    private ImageButton imageButtonHome;

    private ImageButton imageButtonPost;

    private ImageButton imageButtonProfile;

    private SearchView searchView;

    private SwipeRefreshLayout swipeRefreshLayout;

    private HomeEventAdapter followedEventAdapter;
    private HomeEventAdapter nearbyEventAdapter;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;
    private List<Event> allEvents;

    private List<Event> followedEvent;
    private List<Event> nearByEvent;
    private static Util util;

    private User me;

    private LocationManager lm;
    private int tabNum;


    public HomeFragment() {
    }

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().finish();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
        util = Util.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        imageButtonHome = view.findViewById(R.id.home_imageButton_home);
        imageButtonChat = view.findViewById(R.id.home_imageButton_chat);
        imageButtonPost = view.findViewById(R.id.home_imageButton_post);
        imageButtonProfile = view.findViewById(R.id.home_imageButton_user);
        searchView = view.findViewById(R.id.home_searchView);
        swipeRefreshLayout = view.findViewById(R.id.home_swipeRefreshLayout);
        recyclerView = view.findViewById(R.id.home_recyclerView);
        tabLayout = view.findViewById(R.id.home_tablayout);
        recyclerViewLayoutManager = new GridLayoutManager(requireContext(), 2);
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        progressBar = view.findViewById(R.id.home_progressBar);


        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(false);
            Refresh();
        });


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        tabNum = 0;
                        loadFollowedEvent();
                    break;

                    case 1:
                        tabNum = 1;
                        loadNearByEvent();
                    break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.equals("")){
                    mListener.showResult(query, me);
                    return true;
                }
               return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (me == null) {
            imageButtonProfile.setOnClickListener(v -> Toast.makeText(requireContext(),
                    "Loading data!", Toast.LENGTH_SHORT).show());

            imageButtonPost.setOnClickListener(v -> Toast.makeText(requireContext(),
                    "Loading data!", Toast.LENGTH_SHORT).show());

            imageButtonChat.setOnClickListener(view1 -> Toast.makeText(requireContext(),
                    "Loading data!", Toast.LENGTH_SHORT).show());
        } else {
            imageButtonProfile.setOnClickListener(v -> mListener.openProfile(me));
            imageButtonPost.setOnClickListener(v -> mListener.openPost(me.getDraftEvent()));
            imageButtonChat.setOnClickListener(view1 -> mListener.openChatManager());
            imageButtonHome.setOnClickListener(v -> Refresh());
        }
        loadFollowedEvent();
    }


    private void loadMyData() {
        util.getUser(util.getCurrentUser().getUid(), user -> {
            if (me == null) {
                imageButtonProfile.setOnClickListener(v -> mListener.openProfile(me));
                imageButtonPost.setOnClickListener(v -> mListener.openPost(me.getDraftEvent()));
                imageButtonChat.setOnClickListener(view1 -> mListener.openChatManager());
                imageButtonHome.setOnClickListener(v -> Refresh());
            }
            me = user;
            progressBar.setVisibility(View.INVISIBLE);
        }, Util.DEFAULT_F_LISTENER);
    }

    private void Refresh() {
        if (tabNum == 0) {
            allEvents = followedEventAdapter.getEventList();
            Collections.shuffle(allEvents, new Random(System.currentTimeMillis()));
            followedEventAdapter = new HomeEventAdapter(requireContext(), allEvents, me);
            recyclerView.setAdapter(followedEventAdapter);
        } else {
            if (nearbyEventAdapter!=null) {
                allEvents = nearbyEventAdapter.getEventList();
                Collections.shuffle(allEvents, new Random(System.currentTimeMillis()));
                nearbyEventAdapter = new HomeEventAdapter(requireContext(), allEvents, me);
                recyclerView.setAdapter(nearbyEventAdapter);
            }
        }
    }

    private void loadFollowedEvent() {
        progressBar.setVisibility(View.VISIBLE);
        util.getFriendEvents(events -> {
            followedEvent = events;
            followedEventAdapter = new HomeEventAdapter(requireContext(), followedEvent, me);
            recyclerView.setAdapter(followedEventAdapter);
            loadMyData();
            progressBar.setVisibility(View.INVISIBLE);
        }, Util.DEFAULT_F_LISTENER);
    }

    private void loadNearByEvent() {
        progressBar.setVisibility(View.VISIBLE);
        lm = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        util.getNearByEvents(new GeoPoint(location.getLatitude(), location.getLongitude()), 5 ,events -> {
            nearByEvent = events;
            nearbyEventAdapter = new HomeEventAdapter(requireContext(), nearByEvent, me);
            recyclerView.setAdapter(nearbyEventAdapter);
            progressBar.setVisibility(View.INVISIBLE);
        }, Util.DEFAULT_F_LISTENER);
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IhomeFragmentAction){
            this.mListener = (IhomeFragmentAction) context;
        }else{
            throw new RuntimeException(context + "must implement home Fragment Action");
        }
    }


    public interface IhomeFragmentAction {
        void openChatManager();
        void openProfile(User user);
        void openPost(Event event);
        void showResult(String query, User user);
    }
}