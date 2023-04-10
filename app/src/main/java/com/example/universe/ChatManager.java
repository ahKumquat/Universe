package com.example.universe;

import static com.example.universe.Util.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.universe.Models.Chat;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChatManager extends Fragment {
    private static Util util;
    private static final String CHATMANAGERTITLE = "Chat Manager";
    private ArrayList<Chat> chatList;
    private ChatAdapter chatAdapter;
    private RecyclerView recyclerView;
    private TextView title;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;
    private OnBackPressedCallback callback;
    private IchatManagerFragmentAction mListener;


    public ChatManager() {
        // Required empty public constructor
    }

    public static ChatManager newInstance() {
        ChatManager fragment = new ChatManager();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        util = Util.getInstance();
        chatList = new ArrayList<>();
        callback = new OnBackPressedCallback(true) {
            public void handleOnBackPressed() {
                mListener.backToPrevious();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chatmanager, container, false);
        recyclerView = view.findViewById(R.id.chatManager_recyclerView);
        title = view.findViewById(R.id.chatManager_title_TextView);

        recyclerViewLayoutManager = new LinearLayoutManager(getContext());


        // Set toolbar title
        title.setText(CHATMANAGERTITLE);

        chatAdapter = new ChatAdapter(getContext(), chatList);
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        recyclerView.setAdapter(chatAdapter);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        ArrayList<Chat> chatlist = new ArrayList<>();
        util.getChats(chats -> {
            chatlist.addAll(chats);
            updateRecyclerView(chatlist);
        }, Util.DEFAULT_F_LISTENER);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateRecyclerView(ArrayList<Chat> chats) {
        this.chatList = chats;
        chatAdapter.setChats(chats);
        chatAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof IchatManagerFragmentAction){
            mListener = (IchatManagerFragmentAction) context;
        }else{
            throw new RuntimeException(context + "must implement IchatManagerFragmentAction");
        }
    }

    public interface IchatManagerFragmentAction {
        void startChatPage(String otherUserId);
        //void populateHomeFragment();
        void backToPrevious();
    }
}