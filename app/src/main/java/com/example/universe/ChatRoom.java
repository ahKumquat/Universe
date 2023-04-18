package com.example.universe;

import static com.example.universe.Util.DEFAULT_F_LISTENER;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.universe.Models.Message;
import com.example.universe.Models.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ChatRoom extends Fragment {
    private static Util util;
    private RichEditText editTextMessage;
    private Button buttonSendMessage;
    private String otherUserName;
    private String enteredMessage;
    private RecyclerView messageRecyclerView;
    private MessageAdapter messageAdaptor;
    private ArrayList<Message> messageList;
    private ImageButton imageButtonCamera;
    private ImageButton imageButtonFile;
    private String otherUserId;
    private User otherUser;
    private IchatFragmentButtonAction mListener;
    private TextView textViewTitle;
    private OnBackPressedCallback callback;

    private User me;
    private Parcelable parcelable;

    public ChatRoom() {
    }

    public static ChatRoom newInstance() {
        ChatRoom fragment = new ChatRoom();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        util = Util.getInstance();
        if (getArguments() != null) {
            otherUserId = getArguments().getString("otherUserId");
            List<String> users = new ArrayList<>();
            users.add(otherUserId);
            users.add(util.getCurrentUser().getUid());
            util.getUsersByIdList(users, users1 -> {
                otherUser = users1.stream().filter(user -> !user.getUid()
                        .equals(util.getCurrentUser().getUid())).collect(Collectors.toList()).get(0);
                otherUserName = otherUser.getUserName();
                textViewTitle.setText(otherUserName);
                me = users1.stream().filter(user -> user.getUid()
                        .equals(util.getCurrentUser().getUid())).collect(Collectors.toList()).get(0);
                loadData();
            }, DEFAULT_F_LISTENER);
        }
        callback = new OnBackPressedCallback(true) {
            public void handleOnBackPressed() {
                mListener.backToPrevious();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof IchatFragmentButtonAction){
            mListener = (IchatFragmentButtonAction) context;
        }else{
            throw new RuntimeException(context + "must implement IchatFragmentAction");
        }
    }

    @SuppressLint({"SimpleDateFormat", "NotifyDataSetChanged"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat_room, container, false);

        textViewTitle = view.findViewById(R.id.chatRoom_title);
        textViewTitle.setText(otherUserName);
        editTextMessage = view.findViewById(R.id.chatRoom_editText_message);
        buttonSendMessage = view.findViewById(R.id.chatRoom_button_send);
        imageButtonCamera = view.findViewById(R.id.chatRoom_imageButton_camera);
        imageButtonFile = view.findViewById(R.id.chatRoom_imageButton_file);

        messageList = new ArrayList<>();
        messageRecyclerView = view.findViewById(R.id.chatRoom_recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        messageRecyclerView.setLayoutManager(linearLayoutManager);

        editTextMessage.setKeyBoardInputCallbackListener((inputContentInfo, flags, opts) ->
                sendImage(Objects.requireNonNull(inputContentInfo.getLinkUri()).toString()));

        buttonSendMessage.setOnClickListener(view1 -> {
            enteredMessage = Objects.requireNonNull(editTextMessage.getText()).toString();
            if (enteredMessage.isEmpty()) {
                Toast.makeText(getContext(),"Enter message first",
                        Toast.LENGTH_LONG).show();
            } else {
                Message message = new Message(util.getCurrentUser(), enteredMessage, null, null);
                util.sendMessage(otherUserId, message, unused -> {}, DEFAULT_F_LISTENER);
                editTextMessage.setText(null);
            }
        });

        imageButtonCamera.setOnClickListener(v -> mListener.sendImage());
        imageButtonFile.setOnClickListener(v -> mListener.sendFile());

        util.getDB().collection("users")
                .document(util.getCurrentUser().getUid())
                .collection("chats")
                .document(otherUserId)
                .addSnapshotListener(MetadataChanges.INCLUDE, (value, error) -> {
                    parcelable = Objects.requireNonNull(messageRecyclerView.getLayoutManager()).onSaveInstanceState();
                    loadData();
                });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (me != null) {
            loadData();
        }
    }

    private void loadData() {
        util.getMessages(otherUserId, newMessages -> updateRecyclerView(new ArrayList<>(newMessages)), DEFAULT_F_LISTENER);
    }


    @SuppressLint("NotifyDataSetChanged")
    public void updateRecyclerView(ArrayList<Message> messages) {
        this.messageList = messages;
        if (messageAdaptor == null){
            messageAdaptor = new MessageAdapter(getContext(), messageList, me);
        }
        messageAdaptor.setMessages(messages);
        messageRecyclerView.setAdapter(messageAdaptor);
        if (parcelable != null) {
            Objects.requireNonNull(messageRecyclerView.getLayoutManager()).onRestoreInstanceState(parcelable);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void sendImage(String path) {
        Message message = new Message(util.getCurrentUser(), null, path, null);
        util.sendMessage(otherUserId, message, unused -> {}, DEFAULT_F_LISTENER);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void sendFile(Uri fileUri) {
        Message message = new Message(util.getCurrentUser(), null, null, fileUri.toString());
        util.sendMessage(otherUserId, message, unused -> {}, DEFAULT_F_LISTENER);
    }

    public interface IchatFragmentButtonAction {
        void sendImage();
        void sendFile();
        void backToPrevious();
    }
}