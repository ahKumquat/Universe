package com.example.universe;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.universe.Models.Event;
import com.example.universe.Models.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;

import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Profile#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Profile extends Fragment {
    private static final String ARG_USER = "user";
    private IProfileFragmentAction mListener;
    private ImageButton imageButtonBack;
    private ImageButton imageButtonSetting;

    private RecyclerView.LayoutManager recyclerViewLayoutManager;

    private RecyclerView recyclerView;

    private Button followButton;
    private Button chatButton;
    private ImageView icon;
    private TextView name;
    private TextView about;
    private TextView followerNum;
    private TextView followingNum;
    private TabLayout tabLayout;
    private ProgressBar progressBar;

    private CardView cardViewFollower;

    private CardView cardViewFollowing;

    private List<Event> postEvents;

    private List<Event> joinedEvents;

    private List<Event> favEvents;

    private Util util;

    private ProfileEventAdapter postEventAdapter;

    private ProfileEventAdapter joinedEventAdapter;

    private ProfileEventAdapter favEventAdapter;


    private User user;

    private boolean isClick;

    private int tabNum;

    public Profile() {
        // Required empty public constructor
    }


    public static Profile newInstance(User user) {
        Profile fragment = new Profile();
        Bundle args = new Bundle();
        args.putSerializable(ARG_USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        util = Util.getInstance();
        if (getArguments() != null) {
            user = (User) getArguments().getSerializable(ARG_USER);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        followButton = view.findViewById(R.id.profile_button_follow);
        chatButton = view.findViewById(R.id.profile_button_send);
        icon = view.findViewById(R.id.profile_imageview_icon);
        name = view.findViewById(R.id.profile_textview_namespace);
        about = view.findViewById(R.id.profile_textview_aboutspace);
        followerNum = view.findViewById(R.id.profile_textview_followerNum);
        followingNum = view.findViewById(R.id.profile_textview_followingNum);
        tabLayout = view.findViewById(R.id.profile_tablayout);
        recyclerView = view.findViewById(R.id.profile_recyclerview);
        progressBar = view.findViewById(R.id.profile_progressBar);
        cardViewFollower = view.findViewById(R.id.profile_cardview_follower);
        cardViewFollowing = view.findViewById(R.id.profile_cardview_following);


        recyclerViewLayoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(recyclerViewLayoutManager);

        imageButtonBack = view.findViewById(R.id.profile_imagebutton_backbutton);
        imageButtonSetting = view.findViewById(R.id.profile_imagebutton_setting);

        if (Objects.equals(user.getUid(), util.getmAuth().getUid())) {
            followButton.setVisibility(View.INVISIBLE);
            chatButton.setVisibility(View.INVISIBLE);
            Objects.requireNonNull(tabLayout.getTabAt(2)).setIcon(null);
            imageButtonSetting.setOnClickListener(v -> mListener.populateSettingFragment(user));
        } else {
            imageButtonSetting.setVisibility(View.INVISIBLE);
            tabLayout.getTouchables().get(2).setEnabled(false);
            if (user.getFollowersIdList().contains(util.getCurrentUser().getUid())) {
                followButton.setText("Following");
                followButton.setOnClickListener(v -> {
                    if (!isClick) {
                        util.unfollowUser(user.getUid(), unused -> {
                            followButton.setText("Follow");
                            user.getFollowersIdList().remove(util.getCurrentUser().getUid());
                            followerNum.setText(String.valueOf(user.getFollowersIdList().size()));
                        }, Util.DEFAULT_F_LISTENER);
                    } else {
                       util.followUser(user.getUid(), unused -> {
                           followButton.setText("Following");
                           user.getFollowersIdList().add(util.getCurrentUser().getUid());
                           followerNum.setText(String.valueOf(user.getFollowersIdList().size()));
                       }, Util.DEFAULT_F_LISTENER);
                    }
                    isClick = !isClick;
                });
            } else {
                followButton.setText("Follow");
                followButton.setOnClickListener(v -> {
                    if (!isClick) {
                        util.followUser(user.getUid(), unused -> {
                            followButton.setText("Following");
                            user.getFollowersIdList().add(util.getCurrentUser().getUid());
                            followerNum.setText(String.valueOf(user.getFollowersIdList().size()));
                        }, Util.DEFAULT_F_LISTENER);
                    } else {
                        util.unfollowUser(user.getUid(), unused -> {
                            followButton.setText("Follow");
                            user.getFollowersIdList().remove(util.getCurrentUser().getUid());
                            followerNum.setText(String.valueOf(user.getFollowersIdList().size()));
                        }, Util.DEFAULT_F_LISTENER);
                    }
                    isClick = !isClick;
                });
            }
        }

        if (user.getAvatarPath() != null) {
            util.getDownloadUrlFromPath(user.getAvatarPath(),
                    uri -> Glide.with(requireContext()).load(uri)
                    .error(R.drawable.circle_user_profile).into(icon),Util.DEFAULT_F_LISTENER);
        }

        name.setText(user.getUserName());
        about.setText(user.getAbout());
        followerNum.setText(String.valueOf(user.getFollowersIdList().size()));
        followingNum.setText(String.valueOf(user.getFollowingIdList().size()));

        cardViewFollowing.setOnClickListener(v -> mListener.populateFollowerFragment(user));
        cardViewFollower.setOnClickListener(v -> mListener.populateFollowerFragment(user));


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        loadPostEvent();
                        tabNum = 0;
                    break;

                    case 1:
                        loadJoinedEvent();
                        tabNum = 1;
                    break;

                    case 2:
                        loadFavEvent();
                        tabNum = 2;
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

        imageButtonBack.setOnClickListener(v -> mListener.populateHomeFragment());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (postEventAdapter == null) {
            loadPostEvent();
        } else {
            updateMyEventList();
        }
    }

    private void updateMyEventList() {
        recyclerView.setAdapter(null);
        progressBar.setVisibility(View.VISIBLE);
        switch (tabNum) {
            case 0:
                loadPostEvent();
            break;

            case 1:
                loadJoinedEvent();
            break;

            case 2:
                loadFavEvent();
            break;
        }
    }

    private void loadPostEvent(){
        progressBar.setVisibility(View.VISIBLE);
        util.getPostEvents(user.getUid(), events -> {
            postEvents = events;
            postEventAdapter = new ProfileEventAdapter(requireContext(), postEvents);
            recyclerView.setAdapter(postEventAdapter);
            progressBar.setVisibility(View.INVISIBLE);
        }, Util.DEFAULT_F_LISTENER);
    }

    private void loadJoinedEvent(){
        progressBar.setVisibility(View.VISIBLE);
        util.getJoinEvents(user.getUid(), events -> {
            joinedEvents = events;
            joinedEventAdapter = new ProfileEventAdapter(requireContext(), joinedEvents);
            recyclerView.setAdapter(joinedEventAdapter);
            progressBar.setVisibility(View.INVISIBLE);
        }, Util.DEFAULT_F_LISTENER);
    }

    private void loadFavEvent(){
        progressBar.setVisibility(View.VISIBLE);
        util.getFavouriteEvents(user.getUid(), events -> {
            favEvents = events;
            favEventAdapter = new ProfileEventAdapter(requireContext(), favEvents);
            recyclerView.setAdapter(favEventAdapter);
            progressBar.setVisibility(View.INVISIBLE);
        }, Util.DEFAULT_F_LISTENER);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IProfileFragmentAction){
            this.mListener = (IProfileFragmentAction) context;
        }else{
            throw new RuntimeException(context + "must implement profile Fragment Action");
        }
    }


    public interface IProfileFragmentAction {
        void backToPrevious();
        void populateHomeFragment();
        void populateSettingFragment(User user);
        void populateFollowerFragment(User user);
    }
}