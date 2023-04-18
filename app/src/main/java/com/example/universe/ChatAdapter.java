package com.example.universe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.universe.Models.Chat;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder>{
    private ArrayList<Chat> chatList;
    private final Context context;
    private final IChatListRecyclerAction mListener;
    private static Util util;

    public ChatAdapter(Context context, ArrayList<Chat> chatList) {
        this.chatList = chatList;
        this.context = context;
        util = Util.getInstance();
        if(context instanceof IChatListRecyclerAction){
            mListener = (IChatListRecyclerAction) context;
        }else{
            throw new RuntimeException(context.toString()+ "must implement IChatListRecyclerAction");
        }
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
        holder.getImageViewUserAvatar().setImageResource(R.drawable.circle_user_avatar);

        if (chat.getUnreadCount() > 0) {
            holder.getImageViewDotForUnreadMessage().setVisibility(View.VISIBLE);
        } else {
            holder.getImageViewDotForUnreadMessage().setVisibility(View.INVISIBLE);
        }

        util.getUser(otherUserId, user -> {
            String name = user.getUserName();
            holder.getTextViewUserName().setText(name);
            String path = user.getAvatarPath();
            util.getDownloadUrlFromPath(path, uri -> Glide.with(context)
                    .load(uri)
                    .centerCrop()
                    .override(500,500)
                    .into(holder.getImageViewUserAvatar()), Util.DEFAULT_F_LISTENER);
        }, Util.DEFAULT_F_LISTENER);
        if (chat.getLastMessage()!= null) {
            holder.getTextViewLastMessage().setText(chat.getLastMessage().getText());
        } else {
            holder.getTextViewLastMessage().setText("");
        }

        holder.getCardView().setOnClickListener(view -> mListener.chatClickedFromRecyclerView(chat));
    }

    @Override
    public int getItemCount() {
        return this.getChats().size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageViewUserAvatar;
        private final TextView textViewUserName;
        private final TextView textViewLastMessage;
        private final ImageView imageViewDotForUnreadMessage;
        private final CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewUserAvatar = itemView.findViewById(R.id.recyclerChatList_imageView_userAvatar);
            textViewUserName = itemView.findViewById(R.id.recyclerChatList_textView_userName);
            textViewLastMessage = itemView.findViewById(R.id.recyclerChatList_textView_lastMessage);
            imageViewDotForUnreadMessage = itemView.findViewById(R.id.chatManager_imageView_dotForNewMessage);
            cardView = itemView.findViewById(R.id.chatroom_cardView);
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
        public CardView getCardView() {
            return cardView;
        }
    }

    public interface IChatListRecyclerAction {
        void chatClickedFromRecyclerView(Chat chat);
    }
}
