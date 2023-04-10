package com.example.universe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.universe.Models.Event;
import com.example.universe.Models.User;

import java.util.List;

public class ProfileEventAdapter extends RecyclerView.Adapter<ProfileEventAdapter.ViewHolder> {

    private List<Event> eventList;

    private HomeEventAdapter.IEventListRecyclerAction mListener;
    private static Util util;
    private Context context;

    private User me;

    public ProfileEventAdapter(Context context, List<Event> eventList, User user){
        this.eventList = eventList;
        this.context = context;
        me = user;
        util = Util.getInstance();
        if(context instanceof HomeEventAdapter.IEventListRecyclerAction){
            mListener = (HomeEventAdapter.IEventListRecyclerAction) context;
        }else{
            throw new RuntimeException(context.toString()+ "must implement IEventListRecyclerAction");
        }
    }

    public List<Event> getEventList() {
        return eventList;
    }
    @NonNull
    @Override
    public ProfileEventAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.events_row, parent, false);
        return new ProfileEventAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileEventAdapter.ViewHolder holder, int position) {
        Event event = this.getEventList().get(position);
        holder.getTextViewTitle().setText(event.getTitle());
        if (!"".equals(event.getImagePath())) {
            util.getDownloadUrlFromPath(event.getImagePath(),
                    uri -> {Glide.with(context).load(uri)
                            .error(R.drawable.image_not_found)
                            .into(holder.getImageViewEventPic());
                    },
                    Util.DEFAULT_F_LISTENER);

        }
        holder.getImageViewEventPic().setOnClickListener(v -> mListener.eventClickedFromRecyclerView(event, me));
    }

    @Override
    public int getItemCount() {
        return this.getEventList().size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewTitle;
        private ImageView imageViewEventPic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.event_textview_title);
            imageViewEventPic = itemView.findViewById(R.id.event_imageview_eventPicture);
        }

        public TextView getTextViewTitle() {
            return textViewTitle;
        }

        public ImageView getImageViewEventPic() {
            return imageViewEventPic;
        }
    }
}
