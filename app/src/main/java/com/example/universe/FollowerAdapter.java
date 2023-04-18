package com.example.universe;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.example.universe.Models.User;

import java.util.List;

public class FollowerAdapter extends RecyclerView.Adapter<FollowerAdapter.ViewHolder> {

    private final List<User> userList;
    private final IFollowerListRecyclerAction mListener;

    private final IFollowerListRecyclerActionToFragment mListenerFrg;
    private static Util util;

    private final Context context;

    private final User user;

    private final User me;

    private boolean isClicked;


    public FollowerAdapter(Context context, List<User> userList, User user, User me, Fragment fragment){
        this.userList = userList;
        this.context = context;
        this.user = user;
        if (user.getUid().equals(me.getUid())){
            this.me = user;
        } else {
            this.me = me;
        }
        util = Util.getInstance();
        if(context instanceof IFollowerListRecyclerAction){
            mListener = (IFollowerListRecyclerAction) context;
        }else{
            throw new RuntimeException(context.toString()+ "must implement IFollowerListRecyclerAction");
        }

        if(fragment instanceof IFollowerListRecyclerActionToFragment){
            mListenerFrg = (IFollowerListRecyclerActionToFragment) fragment;
        }else{
            throw new RuntimeException(context + "must implement IFollowerListRecyclerActionToFragment");
        }
    }

    public List<User> getUserList() {
        return userList;
    }

    @NonNull
    @Override
    public FollowerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.followers_recycler_row, parent, false);
        return new FollowerAdapter.ViewHolder(view);
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    @Override
    public void onBindViewHolder(@NonNull FollowerAdapter.ViewHolder holder, int position) {
        User follower = this.getUserList().get(position);

        if (follower.getAvatarPath() != null) {
            util.getDownloadUrlFromPath(follower.getAvatarPath(),
                    uri -> Glide.with(context).load(uri)
                    .override(140,140).into(holder.getAvatar()), Util.DEFAULT_F_LISTENER);
        }

        holder.getName().setText(follower.getUserName());

        if (follower.getUid().equals(util.getCurrentUser().getUid())) {
            holder.getButton().setVisibility(View.INVISIBLE);
        } else {
            if (!me.getFollowingIdList().contains(follower.getUid())) {
                holder.getButton().setText("Follow");
            } else {
                holder.getButton().setText("Following");
            }
            holder.getButton().setOnClickListener(v -> {
                if (!me.getFollowingIdList().contains(follower.getUid())) {
                    util.followUser(follower.getUid(), unused -> {
                        holder.getButton().setText("Following");
                        me.getFollowingIdList().add(follower.getUid());
                        mListenerFrg.updateFollowingList(user.getFollowingIdList());
                    }, Util.DEFAULT_F_LISTENER);
                } else {
                    util.unfollowUser(follower.getUid(), unused -> {
                        holder.getButton().setText("Follow");
                        me.getFollowingIdList().remove(follower.getUid());
                        mListenerFrg.updateFollowingList(user.getFollowingIdList());
                    }, Util.DEFAULT_F_LISTENER);
                }
                isClicked = !isClicked;
            });
        }

        holder.getCardView().setOnClickListener(v -> mListener.followerClickedFromRecyclerView(follower));
    }

    @Override
    public int getItemCount() {
        return this.getUserList().size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
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
    }

    public interface IFollowerListRecyclerAction {
        void followerClickedFromRecyclerView(User user);
    }

    public interface IFollowerListRecyclerActionToFragment{
        void updateFollowingList(List<String> newFollowingList);
    }
}
