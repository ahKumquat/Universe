package com.example.universe;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toolbar;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.tabs.TabLayout;

public class HomeFragment extends Fragment {
    private ImageButton imageButtonChat;
    private IhomeFragmentAction mListener;

    private OnBackPressedCallback callback;

    private TabLayout tabLayout;

    private Toolbar toolbar;

    private ImageButton imageButtonPost;

    private ImageButton imageButtonProfile;

    private SearchView searchView;



    public HomeFragment() {
        // Required empty public constructor
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
        if (getArguments() != null) {

        }
        callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                mListener.logOut();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        imageButtonChat = view.findViewById(R.id.home_imageButton_chat);
        imageButtonPost = view.findViewById(R.id.home_imageButton_post);
        imageButtonProfile = view.findViewById(R.id.home_imageButton_user);
        searchView = view.findViewById(R.id.home_searchView);

        imageButtonProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.openProfile();
            }
        });

        imageButtonPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.openPost();
            }
        });

        imageButtonChat.setOnClickListener(view1 -> mListener.openChatManager());




        return view;
    }




    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IhomeFragmentAction){
            this.mListener = (IhomeFragmentAction) context;
        }else{
            throw new RuntimeException(context.toString()+ "must implement home Fragment Action");
        }
    }


    public interface IhomeFragmentAction {
        void openChatManager();
        void openProfile();
        void openPost();
        void logOut();
    }
}