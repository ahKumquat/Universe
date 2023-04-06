package com.example.universe;

import static com.example.universe.Util.TAG;

import android.os.Bundle;

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


    public ChatManager() {
        // Required empty public constructor
    }

    public static ChatManager newInstance() {
        ChatManager fragment = new ChatManager();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        util = Util.getInstance();
        //getActivity().setTitle();
        chatList = new ArrayList<Chat>();
        loadData();
        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
        }

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
        Log.d(TAG, "onCreateView: set up recycler view");
        return view;
    }

    private void loadData() {
        ArrayList<Chat> chatlist = new ArrayList<>();
        util.getChats(new OnSuccessListener<List<Chat>>() {
            @Override
            public void onSuccess(List<Chat> chats) {
                for (Chat chat: chats) {
                    chatlist.add(chat);
                }
                updateRecyclerView(chatlist);
            }
        }, Util.DEFAULT_F_LISTENER);
    }

    private void updateRecyclerView(ArrayList<Chat> chats) {
        this.chatList = chats;
        chatAdapter.setChats(chats);
        chatAdapter.notifyDataSetChanged();
    }

    public interface IchatManagerFragmentAction {
        void startChatPage(String otherUserId);
    }
}