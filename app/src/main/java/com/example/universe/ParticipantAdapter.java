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
import com.example.universe.Models.Event;
import com.example.universe.Models.User;

import java.util.ArrayList;
import java.util.List;


public class ParticipantAdapter extends RecyclerView.Adapter<ParticipantAdapter.ViewHolder>{

    private Context context;
    private List<User> userList;
    private Event event;
    private static Util util;
    private IEventListRecyclerAction mListener;
    private IEventRecyclerActionToFragment mListenerFrg;

    public ParticipantAdapter (Context context, List<User> userList, Event event, Fragment fragment) {
        this.userList = userList;
        this.context = context;
        this.event = event;
        util = Util.getInstance();
        if(context instanceof IEventListRecyclerAction){
            mListener = (IEventListRecyclerAction) context;
        }else{
            throw new RuntimeException(context.toString()+ "must implement IEventListRecyclerAction");
        }

        if(fragment instanceof IEventRecyclerActionToFragment){
            mListenerFrg = (IEventRecyclerActionToFragment) fragment;
        }else{
            throw new RuntimeException(context + "must implement IEventRecyclerActionToFragment");
        }
    }

    public List<User> getUserList() {
        return userList;
    }

    @NonNull
    @Override
    public ParticipantAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.participant_recycler_row, parent, false);

        return new ParticipantAdapter.ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ParticipantAdapter.ViewHolder holder, int position) {
        User user = this.getUserList().get(position);

        if (user.getAvatarPath() != null) {
            util.getDownloadUrlFromPath(user.getAvatarPath(),
                    uri -> Glide.with(context).load(uri)
                            .override(70,70).into(holder.getAvatar()), Util.DEFAULT_F_LISTENER);
        }

        if (!user.getUid().equals(util.getCurrentUser().getUid())) {
            holder.getCardView().setOnClickListener(v -> mListener.eventClickedFromRecyclerView(user));
        }

        holder.getName().setText(user.getUserName());

        if (event.getHostId().equals(user.getUid())) {
            holder.getApproveButton().setVisibility(View.INVISIBLE);
        } else {
            if (event.getHostId().equals(util.getCurrentUser().getUid())) {
                if (!event.getParticipants().contains(user.getUid())) {
                    holder.getApproveButton().setText("Approve");
                } else {
                    holder.getApproveButton().setText("Remove");
                }
                holder.getApproveButton().setOnClickListener(v -> {
                    mListenerFrg.Loading();
                    if (event.getParticipants().contains(user.getUid())) {
                        util.rejectJoinEvent(user.getUid(), event.getUid(), unused -> {
                            holder.getApproveButton().setText("Approve");
                            event.getParticipants().remove(user.getUid());
                            user.getJoinedEventsIdList().remove(event.getUid());
                            mListenerFrg.updateParticipantList(event.getParticipants());
                            mListenerFrg.finished();
                        }, Util.DEFAULT_F_LISTENER);
                    } else {
                        util.approveJoinEvent(user.getUid(), event.getUid(), unused -> {
                            holder.getApproveButton().setText("Remove");
                            event.getParticipants().add(user.getUid());
                            user.getJoinedEventsIdList().add(event.getUid());
                            mListenerFrg.updateParticipantList(event.getParticipants());
                            mListenerFrg.finished();
                        }, Util.DEFAULT_F_LISTENER);
                    }
                });
            } else {
                if (!event.getParticipants().contains(user.getUid())) {
                    holder.getApproveButton().setText("WaitList");
                } else {
                    holder.getApproveButton().setText("Going");
                }
            }
        }


    }

    @Override
    public int getItemCount() {
        return this.getUserList().size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView name;
        private ImageView avatar;
        private Button approveButton;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.participants_cardView);
            name = itemView.findViewById(R.id.participants_textView_userName);
            avatar = itemView.findViewById(R.id.participants_imageView_userAvatar);
            approveButton = itemView.findViewById(R.id.participantRecycler_button_manage);
        }

        public TextView getName() {
            return name;
        }

        public ImageView getAvatar() {
            return avatar;
        }

        public CardView getCardView() {
            return cardView;
        }

        public Button getApproveButton() {
            return approveButton;
        }
    }
    public interface IEventListRecyclerAction{
        void eventClickedFromRecyclerView(User user);
    }

    public interface IEventRecyclerActionToFragment{
        void updateParticipantList(List<String> newParticipantList);
        void Loading();
        void finished();
    }
}
