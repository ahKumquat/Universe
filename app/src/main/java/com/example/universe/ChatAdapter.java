package com.example.universe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.universe.Models.Chat;
import com.example.universe.Models.User;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder>{
    private ArrayList<Chat> chatList;
    private IchatListRecyclerAction mListener;
    private static Util util;

    public ChatAdapter(Context context, ArrayList<Chat> chatList) {
        this.chatList = chatList;
        util = Util.getInstance();
        if(context instanceof IchatListRecyclerAction){
            mListener = (IchatListRecyclerAction) context;
        }else{
            throw new RuntimeException(context.toString()+ "must implement IchatListRecyclerAction");
        }
    }

    public ChatAdapter() {
        util = Util.getInstance();
    }

    public ArrayList<Chat> getChats() {
        return chatList;
    }

    public void setChats(ArrayList<Chat> chats) {
        this.chatList = chats;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.chatmanager_recycler_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chat chat = this.getChats().get(position);
        String otherUserId = chat.getOtherUserId();

        if (chat.getUnreadCount() > 0) {
            holder.getImageViewDotForUnreadMessage().setVisibility(View.VISIBLE);
        } else {
            holder.getImageViewDotForUnreadMessage().setVisibility(View.INVISIBLE);
        }

        util.getUser(otherUserId, new OnSuccessListener<User>() {
            @Override
            public void onSuccess(User user) {
                String name = user.getUserName();
                holder.getTextViewUserName().setText(name);
            }
        }, Util.DEFAULT_F_LISTENER);
        if (chat.getLastMessage()!= null) {
            holder.getTextViewLastMessage().setText(chat.getLastMessage().getText());
        } else {
            holder.getTextViewLastMessage().setText("");
        }

        holder.getTextViewUserName().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.chatClickedFromRecyclerView(chat);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.getChats().size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewUserAvatar;
        private TextView textViewUserName;
        private TextView textViewLastMessage;
        private ImageView imageViewDotForUnreadMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewUserAvatar = itemView.findViewById(R.id.recyclerChatList_imageView_userAvatar);
            textViewUserName = itemView.findViewById(R.id.recyclerChatList_textView_userName);
            textViewLastMessage = itemView.findViewById(R.id.recyclerChatList_textView_lastMessage);
            imageViewDotForUnreadMessage = itemView.findViewById(R.id.chatManager_imageView_dotForNewMessage);
        }

        public ImageView getImageViewUserAvatar() {
            return imageViewUserAvatar;
        }

        public TextView getTextViewUserName() {
            return textViewUserName;
        }

        public TextView getTextViewLastMessage() {
            return textViewLastMessage;
        }

        public ImageView getImageViewDotForUnreadMessage() {
            return imageViewDotForUnreadMessage;
        }
    }

    public interface IchatListRecyclerAction {
        void chatClickedFromRecyclerView(Chat chat);
    }
}
