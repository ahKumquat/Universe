package com.example.universe;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.example.universe.Models.Event;
import com.example.universe.Models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class SearchFragment extends Fragment implements FollowerAdapter.IFollowerListRecyclerActionToFragment {

    private static final String ARG_QUERY = "query";
    private static final String ARG_USER = "user";
    private RecyclerView.LayoutManager recyclerViewLayoutManagerUsers;
    private RecyclerView.LayoutManager recyclerViewLayoutManagerEvents;

    private ISearchFragmentAction mListener;
    private SearchView searchView;
    private ProgressBar progressBar;
    private TabLayout tabLayout;

    private FollowerAdapter followerAdapter;

    private HomeEventAdapter homeEventAdapter;

    private RecyclerView recyclerView;

    private ImageButton backButton;
    private String query;
    private User me;

    private Util util;
    private int tabNum;

    public SearchFragment() {
        // Required empty public constructor
    }


    public static SearchFragment newInstance(String query, User user) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_QUERY, query);
        args.putSerializable(ARG_USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        util = Util.getInstance();
        if (getArguments() != null) {
            query = getArguments().getString(ARG_QUERY);
            me = (User) getArguments().getSerializable(ARG_USER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        recyclerView = view.findViewById(R.id.search_recyclerview);
        searchView = view.findViewById(R.id.search_searchview);
        tabLayout = view.findViewById(R.id.search_tablayout);
        progressBar = view.findViewById(R.id.search_progressBar);
        backButton = view.findViewById(R.id.search_imageButton);
        recyclerViewLayoutManagerUsers = new LinearLayoutManager(requireContext());
        recyclerViewLayoutManagerEvents = new GridLayoutManager(requireContext(), 2);

        searchView.setQueryHint(query);

        backButton.setOnClickListener(v -> mListener.backToPrevious());

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        tabNum = 0;
                        loadResultForEvents();
                        break;

                    case 1:
                        tabNum = 1;
                        loadResultForUsers();
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
            public boolean onQueryTextSubmit(String q) {
                if (!q.equals("")) {
                    query = q;
                    if (tabNum == 0) {
                        loadResultForEvents();
                    } else {
                        loadResultForUsers();
                    }
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
        loadResultForEvents();
    }

    public void loadResultForEvents() {
        Log.d(Util.TAG, "loadResultForEvents: ");
        progressBar.setVisibility(View.VISIBLE);
        util.getDB()
                .collection("events")
                .orderBy("title")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limit(101)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        homeEventAdapter = new HomeEventAdapter(requireContext(), task.getResult().toObjects(Event.class), me);
                        recyclerView.setAdapter(homeEventAdapter);
                        recyclerView.setLayoutManager(recyclerViewLayoutManagerEvents);
                        progressBar.setVisibility(View.INVISIBLE);
                    } else {
                        Log.d("test", "loadResultForEvents: " + task.getException());
                    }
                });
    }

    public void loadResultForUsers() {
        Log.d(Util.TAG, "loadResultForUsers: ");
        progressBar.setVisibility(View.VISIBLE);
        util.getDB()
                .collection(Util.USERS_COLLECTION_NAME)
                .orderBy(User.KEY_USERNAME)
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limit(102)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<User> userList = task.getResult().toObjects(User.class);
                        userList.remove(me);
                        followerAdapter = new FollowerAdapter(requireContext(), userList, me, this);
                        recyclerView.setAdapter(followerAdapter);
                        recyclerView.setLayoutManager(recyclerViewLayoutManagerUsers);
                        progressBar.setVisibility(View.INVISIBLE);
                    } else{
                        Log.d(Util.TAG, "loadResultForUsers: " + task.getException());
                    }
                });
    }

    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ISearchFragmentAction) {
            this.mListener = (ISearchFragmentAction) context;
        } else {
            throw new RuntimeException(context + "must implement ISearchFragmentAction");
        }
    }

    @Override
    public void updateFollowingList(List<String> newFollowingList) {
        this.me.setFollowingIdList(newFollowingList);
    }

    public interface ISearchFragmentAction {
        //void populateHomeFragment();
        void backToPrevious();
    }
}