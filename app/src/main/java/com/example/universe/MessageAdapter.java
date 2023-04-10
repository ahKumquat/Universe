package com.example.universe;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.universe.Models.Message;
import com.example.universe.Models.User;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter {
    private Context context;
    private String TAG = Util.TAG;
    private ArrayList<Message> messageList;
    private static Util util;

    int ITEM_SEND = 1;
    int ITEM_RECEIVE = 2;

    public MessageAdapter(Context context, ArrayList<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
        util = Util.getInstance();
    }

    public MessageAdapter() {
        util = Util.getInstance();
    }

    public ArrayList<Message> getMessages() {
        return messageList;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messageList = messages;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_SEND) {
            View view = LayoutInflater.from(context).inflate(R.layout.senderchat, parent, false);
            return new SenderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.receiverchat, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = this.getMessages().get(position);

        if (holder.getClass() == SenderViewHolder.class) {
            SenderViewHolder viewHolder = (SenderViewHolder) holder;
            //load user avatar
            util.getUser(message.getUserId(), new OnSuccessListener<User>() {
                @Override
                public void onSuccess(User user) {
                    if (user.getAvatarPath() != null) {
                        util.getDownloadUrlFromPath(user.getAvatarPath(), new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Glide.with(context)
                                        .load(uri)
                                        .centerCrop()
                                        .into(viewHolder.getImageViewUserAvatar());
                            }
                        }, Util.DEFAULT_F_LISTENER);
                    }
                }
            }, Util.DEFAULT_F_LISTENER);

            if (message.getImagePath()!=null) {
                viewHolder.getImageViewPhoto().setVisibility(View.VISIBLE);
                viewHolder.getTextViewTimeOfMessage().setVisibility(View.GONE);
                viewHolder.getTextViewMessage().setVisibility(View.GONE);

                if (!message.getImagePath().startsWith("https")) {
                    util.getDownloadUrlFromPath(message.getImagePath(), new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Glide.with(context)
                                    .load(uri)
                                    .override(500, 500)
                                    .into(viewHolder.getImageViewPhoto());
                        }
                    }, Util.DEFAULT_F_LISTENER);
                } else {
                    Glide.with(context)
                            .load(message.getImagePath())
                            .override(500, 500)
                            .into(viewHolder.getImageViewPhoto());
                }

            }else if (message.getFileURL()!=null) {
                viewHolder.getTextViewTimeOfMessage().setVisibility(View.GONE);
                viewHolder.getTextViewMessage().setVisibility(View.GONE);
            } else {
                viewHolder.getImageViewPhoto().setVisibility(View.GONE);
                viewHolder.getTextViewTimeOfMessage().setVisibility(View.VISIBLE);
                viewHolder.getTextViewMessage().setVisibility(View.VISIBLE);
                viewHolder.getTextViewMessage().setText(message.getText());
                viewHolder.getTextViewTimeOfMessage().setText(message.getSimpleTime());
            }
        } else {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            //load user avatar
            util.getUser(message.getUserId(), new OnSuccessListener<User>() {
                @Override
                public void onSuccess(User user) {
                    if (user.getAvatarPath() != null) {
                        util.getDownloadUrlFromPath(user.getAvatarPath(), new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Glide.with(context)
                                        .load(uri)
                                        .centerCrop()
                                        .into(viewHolder.getImageViewUserAvatar());
                            }
                        }, Util.DEFAULT_F_LISTENER);
                    }
                }
            }, Util.DEFAULT_F_LISTENER);

            if (message.getImagePath()!=null) {
                viewHolder.getImageViewPhoto().setVisibility(View.VISIBLE);
                viewHolder.getTextViewTimeOfMessage().setVisibility(View.GONE);
                viewHolder.getTextViewMessage().setVisibility(View.GONE);
                util.getDownloadUrlFromPath(message.getImagePath(), new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(context)
                                .load(uri)
                                .centerCrop()
                                .override(500,500)
                                .into(viewHolder.getImageViewPhoto());
                    }
                }, Util.DEFAULT_F_LISTENER);
            } else if (message.getFileURL()!=null) {
                //TODO
                viewHolder.getTextViewTimeOfMessage().setVisibility(View.GONE);
                viewHolder.getTextViewMessage().setVisibility(View.GONE);
            } else {
                viewHolder.getImageViewPhoto().setVisibility(View.GONE);
                viewHolder.getTextViewTimeOfMessage().setVisibility(View.VISIBLE);
                viewHolder.getTextViewMessage().setVisibility(View.VISIBLE);
                viewHolder.getTextViewMessage().setText(message.getText());
                viewHolder.getTextViewTimeOfMessage().setText(message.getSimpleTime());
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = this.getMessages().get(position);
        if (util.getCurrentUser().getUid().equals(message.getUserId())) {
            return ITEM_SEND;
        } else return ITEM_RECEIVE;
    }

    @Override
    public int getItemCount() {
        return this.getMessages().size();
    }

    class SenderViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewMessage;
        private TextView timeOfMessage;
        private ImageView imageViewPhoto;
        private ImageView imageViewUserAvatar;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.chatRoom_textView_messageContent);
            timeOfMessage = itemView.findViewById(R.id.chatRoom_textView_timeOfMessage);
            imageViewUserAvatar = itemView.findViewById(R.id.chatRoom_imageView_senderAvatar);
            imageViewPhoto = itemView.findViewById(R.id.chatRoom_imageView_message);
            imageViewPhoto.setVisibility(View.INVISIBLE);
        }

        public TextView getTextViewMessage() {
            return textViewMessage;
        }
        public TextView getTextViewTimeOfMessage() {
            return timeOfMessage;
        }
        public ImageView getImageViewPhoto() {
            return imageViewPhoto;
        }
        public ImageView getImageViewUserAvatar() {
            return imageViewUserAvatar;
        }
    }


    class ReceiverViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewMessage;
        private TextView timeOfMessage;
        private ImageView imageViewPhoto;
        private ImageView imageViewUserAvatar;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.chatRoom_textView_messageContent);
            timeOfMessage = itemView.findViewById(R.id.chatRoom_textView_timeOfMessage);
            imageViewUserAvatar = itemView.findViewById(R.id.chatRoom_imageView_senderAvatar);
            imageViewPhoto = itemView.findViewById(R.id.chatRoom_imageView_message);
            imageViewPhoto.setVisibility(View.INVISIBLE);
        }

        public TextView getTextViewMessage() {
            return textViewMessage;
        }

        public TextView getTextViewTimeOfMessage() {
            return timeOfMessage;
        }

        public ImageView getImageViewPhoto() {
            return imageViewPhoto;
        }
        public ImageView getImageViewUserAvatar() {
            return imageViewUserAvatar;
        }

    }
}
