package com.example.universe;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.universe.Models.User;
import com.google.android.material.tabs.TabLayout;

import java.util.List;
import java.util.Objects;


public class Followers extends Fragment implements FollowerAdapter.IFollowerListRecyclerActionToFragment {

    private static final String ARG_USER = "user";
    private static final String ARG_TAB = "tabNum";

    private IFollowerFragmentAction mListener;

    private TextView textViewTitle;

    private TabLayout tabLayout;

    private TextView textViewFollower;

    private TextView textViewFollowing;

    private RecyclerView recyclerView;

    private ProgressBar progressBar;

    private RecyclerView.LayoutManager recyclerViewLayoutManager;

    private User me;

    private Util util;

    private List<String> followerUIDs;

    private List<User> followerList;

    private List<String> followingUIDs;

    private List<User> followingList;

    private FollowerAdapter followerAdapter;
    private FollowerAdapter followingAdapter;

    private int tabNum;

    private OnBackPressedCallback callback;

    public Followers() {
        // Required empty public constructor
    }


    public static Followers newInstance(User user, int tabNum) {
        Followers fragment = new Followers();
        Bundle args = new Bundle();
        args.putSerializable(ARG_USER, user);
        args.putInt(ARG_TAB, tabNum);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        util = Util.getInstance();
        if (getArguments() != null) {
             me = (User) getArguments().getSerializable(ARG_USER);
             tabNum = getArguments().getInt(ARG_TAB);
             followerUIDs = me.getFollowersIdList();
             followingUIDs = me.getFollowingIdList();
        }
        callback = new OnBackPressedCallback(true) {
            public void handleOnBackPressed() {
                mListener.backToPrevious();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_followers, container, false);
        textViewTitle = view.findViewById(R.id.followers_textView_title);
        tabLayout = view.findViewById(R.id.followers_tablayout);
        recyclerView = view.findViewById(R.id.followers_recyclerView);
        progressBar = view.findViewById(R.id.followers_progressBar);
        recyclerViewLayoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(recyclerViewLayoutManager);

        textViewFollower = new TextView(requireContext());
        textViewFollowing = new TextView(requireContext());

        textViewFollowing.setText(me.getFollowingIdList().size()
                + "  "
                + "Following");

        textViewFollower.setText(me.getFollowersIdList().size()
                + "  "
                + "Follower");

        textViewFollowing.setGravity(Gravity.CENTER);
        textViewFollower.setGravity(Gravity.CENTER);

        textViewTitle.setText(me.getUserName());

        Objects.requireNonNull(tabLayout.getTabAt(0)).setCustomView(textViewFollower);

        Objects.requireNonNull(tabLayout.getTabAt(1)).setCustomView(textViewFollowing);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        getAllFollowers(followerUIDs);
                        tabNum = 0;
                    break;

                    case 1:
                        getAllFollowings(followingUIDs);
                        tabNum = 1;
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


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (followerAdapter == null) {
            Objects.requireNonNull(tabLayout.getTabAt(tabNum)).select();
            switch (tabNum) {
                case 0:
                    getAllFollowers(followerUIDs);
                break;

                case 1:

                    getAllFollowings(followingUIDs);
                break;
            }

        } else {
            updateMyList();
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateMyList(){
        recyclerView.setAdapter(null);
        progressBar.setVisibility(View.VISIBLE);
        util.getUser(util.getCurrentUser().getUid(), user -> {
            me = user;
            followerUIDs = me.getFollowersIdList();
            followingUIDs = me.getFollowingIdList();
            textViewFollowing.setText(me.getFollowingIdList().size()
                    + "  "
                    + "Following");

            textViewFollower.setText(me.getFollowersIdList().size()
                    + "  "
                    + "Follower");
            if (tabNum == 0) {
                getAllFollowers(followerUIDs);
            } else {
                getAllFollowings(followingUIDs);
            }
            Objects.requireNonNull(tabLayout.getTabAt(tabNum)).select();
        }, Util.DEFAULT_F_LISTENER);
    }

    private void getAllFollowers(List<String> followerUIDs) {
        progressBar.setVisibility(View.VISIBLE);
        util.getUsersByIdList(followerUIDs, users -> {
            followerList = users;
            progressBar.setVisibility(View.INVISIBLE);
            followerAdapter = new FollowerAdapter(requireContext(), followerList, me,this);
            recyclerView.setAdapter(followerAdapter);
        }, Util.DEFAULT_F_LISTENER);
    }

    private void getAllFollowings(List<String> followingUIDs) {
        progressBar.setVisibility(View.VISIBLE);
        util.getUsersByIdList(followingUIDs, users -> {
            followingList = users;
            progressBar.setVisibility(View.INVISIBLE);
            followingAdapter = new FollowerAdapter(requireContext(), followingList, me, this);
            recyclerView.setAdapter(followingAdapter);
        }, Util.DEFAULT_F_LISTENER);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void updateFollowingList(List<String> newFollowingList) {
        this.me.setFollowingIdList(newFollowingList);
        textViewFollowing.setText(me.getFollowingIdList().size()
                + "  "
                + "Following");
    }

    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IFollowerFragmentAction){
            this.mListener = (IFollowerFragmentAction) context;
        }else{
            throw new RuntimeException(context + "must implement IFollowerFragmentAction");
        }
    }
    public interface IFollowerFragmentAction {
        void backToPrevious();
    }
}