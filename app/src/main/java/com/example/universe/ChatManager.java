package com.example.universe;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.universe.Models.Chat;

import java.util.ArrayList;

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


    public ChatManager() {}

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
        View view = inflater.inflate(R.layout.fragment_chatmanager, container, false);
        recyclerView = view.findViewById(R.id.chatManager_recyclerView);
        title = view.findViewById(R.id.chatManager_title_TextView);

        recyclerViewLayoutManager = new LinearLayoutManager(getContext());

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