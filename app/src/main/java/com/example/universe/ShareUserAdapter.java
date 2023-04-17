package com.example.universe;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.universe.Models.Event;
import com.example.universe.Models.User;

import java.util.List;

public class ShareUserAdapter extends RecyclerView.Adapter<ShareUserAdapter.ViewHolder>{
    private List<User> userList;
    private static Util util;

    private User user;
    private View view;
    private boolean isClicked;
    private User selectedUser;
    private int selected_position;
    private IShareListRecyclerAction mListener;

    public List<User> getUserList() {
        return userList;
    }

    public ShareUserAdapter(View view, List<User> userList, User user, Fragment fragment) {
       this.userList = userList;
       this.user = user;
       this.view = view;
       util = Util.getInstance();
       if (fragment instanceof IShareListRecyclerAction) {
           mListener = (IShareListRecyclerAction) fragment;
       } else {
           throw new RuntimeException(view.toString()+ "must implement IShareListRecyclerAction");
       }
    }

    @NonNull
    @Override
    public ShareUserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.followers_recycler_row, parent, false);

        return new ShareUserAdapter.ViewHolder(view);
    }

    @SuppressLint({"ResourceAsColor", "ResourceType"})
    @Override
    public void onBindViewHolder(@NonNull ShareUserAdapter.ViewHolder holder, int position) {
        User friend = this.getUserList().get(position);
        if (friend.getAvatarPath() != null) {
            util.getDownloadUrlFromPath(friend.getAvatarPath(),
                    uri -> Glide.with(view.getContext()).load(uri)
                            .override(70,70).into(holder.getAvatar()), Util.DEFAULT_F_LISTENER);
        }
        holder.getName().setText(friend.getUserName());
        holder.getButton().setVisibility(View.INVISIBLE);
        holder.getCardView().setCardBackgroundColor((selected_position == position && isClicked) ?
                Color.parseColor(view.getContext().getResources().getString(R.color.universe_yellow)) : Color.WHITE);
    }

    public User getSelectedUser() {
        return selectedUser;
    }

    @Override
    public int getItemCount() {
        return this.getUserList().size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final CardView cardView;
        private final TextView name;
        private final ImageView avatar;
        private final Button button;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.followers_cardView);
            name = itemView.findViewById(R.id.recyclerChatList_textView_userName);
            avatar = itemView.findViewById(R.id.followers_imageView_userAvatar);
            button = itemView.findViewById(R.id.participantRecycler_button_manage);
            cardView.setOnClickListener(this);
        }

        public Button getButton() {
            return button;
        }

        public CardView getCardView() {
            return cardView;
        }

        public ImageView getAvatar() {
            return avatar;
        }

        public TextView getName() {
            return name;
        }

        @Override
        public void onClick(View v) {
            if (getAdapterPosition() == RecyclerView.NO_POSITION) return;
            if (!isClicked) {
                isClicked = true;
                mListener.enableButton();
            }
            notifyItemChanged(selected_position);
            selected_position = getAdapterPosition();
            notifyItemChanged(selected_position);
            selectedUser = getUserList().get(selected_position);
        }
    }

    public interface IShareListRecyclerAction{
        void enableButton();
    }
}
