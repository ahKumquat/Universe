package com.example.universe;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.universe.Models.Event;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class HomeEventAdapter extends RecyclerView.Adapter<HomeEventAdapter.ViewHolder> {

    private List<Event> eventList;
    private IEventListRecyclerAction mListener;
    private static Util util;
    private Context context;

    public HomeEventAdapter(Context context, List<Event> eventList){
        this.eventList = eventList;
        this.context = context;
        util = Util.getInstance();
        if(context instanceof IEventListRecyclerAction){
            mListener = (IEventListRecyclerAction) context;
        }else{
            throw new RuntimeException(context.toString()+ "must implement IEventListRecyclerAction");
        }
    }

    public List<Event> getEventList() {
        return eventList;
    }

    @NonNull
    @Override
    public HomeEventAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.home_events_recycler_row, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull HomeEventAdapter.ViewHolder holder, int position) {
        Event event = this.getEventList().get(position);
        holder.getTextViewTitle().setText(event.getTitle());
        if (!"".equals(event.getImagePath())) {
            util.getDownloadUrlFromPath(event.getImagePath(), new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(context)
                            .load(uri)
                            .error(R.drawable.image_not_found)
                            .into(holder.imageButtonEventPic);
                }
            }, Util.DEFAULT_F_LISTENER);
        }
        holder.getImageButtonEventPic().setOnClickListener(v -> mListener.eventClickedFromRecyclerView(event));
    }

    @Override
    public int getItemCount() {
        return this.getEventList().size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewTitle;
        private final ImageButton imageButtonEventPic;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.homeEvents_textView_eventTitle);
            imageButtonEventPic = itemView.findViewById(R.id.eventPage_imageButton_event);
        }

        public TextView getTextViewTitle() {
            return textViewTitle;
        }

        public ImageButton getImageButtonEventPic() {
            return imageButtonEventPic;
        }
    }

    public interface IEventListRecyclerAction {
        void eventClickedFromRecyclerView(Event event);
    }
}
