package com.example.universe;

import static com.example.universe.Util.DEFAULT_F_LISTENER;
import static com.example.universe.Util.DEFAULT_VOID_S_LISTENER;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.universe.Models.Chat;
import com.example.universe.Models.Message;
import com.example.universe.Models.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ChatRoom extends Fragment {
    private static Util util;
    private String TAG = Util.TAG;
    private EditText editTextMessage;
    private Button buttonSendMessage;
    private String otherUserName;
    private String enteredMessage;
    private RecyclerView messageRecyclerView;
    private MessageAdapter messageAdaptor;
    private ArrayList<Message> messageList;
    private ImageButton imageButtonCamera;
    private ImageButton imageButtonFile;
    private Calendar calendar;
    private SimpleDateFormat simpleDateFormat;
    private String otherUserId;
    private User otherUser;

    public ChatRoom() {
        // Required empty public constructor
    }

    public static ChatRoom newInstance() {
        ChatRoom fragment = new ChatRoom();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        util = Util.getInstance();
        if (getArguments() != null) {
            otherUserId = getArguments().getString("otherUserId");
            util.getUser(otherUserId, new OnSuccessListener<User>() {
                @Override
                public void onSuccess(User user) {
                    otherUser = user;
                    otherUserName = user.getUserName();
                    getActivity().setTitle(otherUserName);
                    Log.d(TAG, "onCreate: otherUserId " + otherUserId + "otherUserName " + otherUserName);
                }
            }, DEFAULT_F_LISTENER);
            loadData();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat_room, container, false);

        editTextMessage = view.findViewById(R.id.chatRoom_editText_message);
        buttonSendMessage = view.findViewById(R.id.chatRoom_button_send);
        imageButtonCamera = view.findViewById(R.id.chatRoom_imageButton_camera);
        imageButtonFile = view.findViewById(R.id.chatRoom_imageButton_file);

        simpleDateFormat = new SimpleDateFormat("hh:mm a");
        calendar = Calendar.getInstance();

        messageList = new ArrayList<>();
        messageRecyclerView = view.findViewById(R.id.chatRoom_recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        messageRecyclerView.setLayoutManager(linearLayoutManager);
        messageAdaptor = new MessageAdapter(getContext(), messageList);
        messageRecyclerView.setAdapter(messageAdaptor);

        buttonSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enteredMessage = editTextMessage.getText().toString();
                if (enteredMessage.isEmpty()) {
                    Toast.makeText(getContext(),"Enter message first",
                            Toast.LENGTH_LONG).show();
                } else {
                    Message message = new Message(util.getCurrentUser(), enteredMessage, null);
                    util.sendMessage(otherUserId, message, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                        }
                    }, DEFAULT_F_LISTENER);
                    editTextMessage.setText(null);
                }
            }
        });

        //Create a listener for Firebase data change...
        util.getDB().collection("users")
                .document(util.getCurrentUser().getUid())
                .collection("chats")
                .document(otherUserId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error!=null){
                            Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        }else{
                            ArrayList<Message> messages = new ArrayList<>();
                            Chat chat = value.toObject(Chat.class);
                            for (Message m: chat.getMessages()) {
                                messages.add(m);
                            }
                            messageAdaptor.setMessages(messages);
                            messageAdaptor.notifyDataSetChanged();
                        }
                    }
                });

        return view;
    }

    private void loadData() {
        ArrayList<Message> messages = new ArrayList<>();
        util.getChat(otherUserId, new OnSuccessListener<Chat>() {
            @Override
            public void onSuccess(Chat chat) {
                for (Message message: chat.getMessages()) {
                    messages.add(message);
                }
                updateRecyclerView(messages);
            }
        }, DEFAULT_F_LISTENER);
    }

    public void updateRecyclerView(ArrayList<Message> messages) {
        this.messageList = messages;
        messageAdaptor.setMessages(messages);
        Log.d(TAG, "updateRecyclerView: " + messages.toString());
        messageAdaptor.notifyDataSetChanged();
    }
}