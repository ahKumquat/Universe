package com.example.universe;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.universe.Models.Event;
import com.example.universe.Models.User;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;


public class ParticipantAdapter extends RecyclerView.Adapter<ParticipantAdapter.ViewHolder>{

    private Context context;
    private List<User> userList;
    private Event event;
    private static Util util;
    private boolean isClicked;
    private IEventListRecyclerAction mListener;

    public ParticipantAdapter (Context context, List<User> userList, Event event) {
        this.userList = userList;
        this.context = context;
        this.event = event;
        util = Util.getInstance();
        if(context instanceof IEventListRecyclerAction){
            mListener = (IEventListRecyclerAction) context;
        }else{
            throw new RuntimeException(context.toString()+ "must implement IEventListRecyclerAction");
        }
    }

    public List<User> getUserList() {
        return userList;
    }
    public void setParticipants(List<String> participants) {
        for (String s: participants) {
            util.getUser(s, new OnSuccessListener<User>() {
                @Override
                public void onSuccess(User user) {
                    userList.add(user);
                }
            }, Util.DEFAULT_F_LISTENER);
        }
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
            Glide.with(context).load(user.getAvatarPath())
                    .override(80,80).into(holder.getAvatar());
        }

        holder.getName().setText(user.getUserName());

        if (user.getUid().equals(util.getCurrentUser().getUid())) {
            holder.getApproveButton().setVisibility(View.INVISIBLE);
        } else {
            holder.getCardView().setOnClickListener(v -> mListener.eventClickedFromRecyclerView(user));
            if (!event.getParticipants().contains(user.getUid())) {
                holder.getApproveButton().setText("Approve");
            } else {
                holder.getApproveButton().setText("Remove");
            }
            holder.getApproveButton().setOnClickListener(v -> {
                if (event.getParticipants().contains(user.getUid())) {
                    util.rejectJoinEvent(user.getUid(), event.getUid(), new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            holder.getApproveButton().setText("Approve");
                            event.getParticipants().remove(user.getUid());
                            user.getJoinedEventsIdList().remove(event.getUid());
                            notifyDataSetChanged();
                        }
                    }, Util.DEFAULT_F_LISTENER);
                } else {
                    util.approveJoinEvent(user.getUid(), event.getUid(), new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            holder.getApproveButton().setText("Remove");
                            event.getParticipants().add(user.getUid());
                            user.getJoinedEventsIdList().add(event.getUid());
                            notifyDataSetChanged();
                        }
                    }, Util.DEFAULT_F_LISTENER);
                }
            });
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
}
