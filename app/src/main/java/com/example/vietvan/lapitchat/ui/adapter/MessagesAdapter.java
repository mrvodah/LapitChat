package com.example.vietvan.lapitchat.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.vietvan.lapitchat.ItemClickListener;
import com.example.vietvan.lapitchat.R;
import com.example.vietvan.lapitchat.model.Message;
import com.example.vietvan.lapitchat.ui.activity.FullImage;
import com.google.firebase.auth.FirebaseAuth;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by VietVan on 14/07/2018.
 */

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessagesViewHolder> {

    private static final String TAG = "TAG";
    List<Message> mMessageList;
    String thumb_image;
    FirebaseAuth auth;
    Context context;
    public final int SENT_MESSAGE = 0, RECEIVED_MESSAGE = 1;
    public final int SENT_IMAGE_MESSAGE = 2, RECEIVED_IMAGE_MESSAGE = 3;

    OnClickItem itemClickListener;

    public MessagesAdapter(List<Message> mMessageList, String thumb_image, Context context, OnClickItem item) {
        this.mMessageList = mMessageList;
        this.thumb_image = thumb_image;
        this.context = context;
        auth = FirebaseAuth.getInstance();
        itemClickListener = item;
    }

    @NonNull
    @Override
    public MessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = null;
        switch (viewType) {
            case SENT_MESSAGE:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, null);
                break;
            case RECEIVED_MESSAGE:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, null);
                break;
            case SENT_IMAGE_MESSAGE:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_sent, null);
                break;
            case RECEIVED_IMAGE_MESSAGE:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_received, null);
                break;
        }

        return new MessagesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MessagesViewHolder holder, final int position) {

        if (mMessageList.get(position).getType().equals("image")) {
            holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent profile = new Intent(context, FullImage.class);
                    profile.putExtra("image", mMessageList.get(position).getMessage());
                    context.startActivity(profile);

                }
            });
            holder.setImage(mMessageList.get(position).getMessage());
        } else
            holder.setData(mMessageList.get(position));

        if (holder.ava != null)
            holder.setAva(thumb_image);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListener.onClick();
            }
        });

    }

    @Override
    public int getItemViewType(int position) {
        if (mMessageList.get(position).getFrom().equals(auth.getUid())) {
            if (mMessageList.get(position).getType().equals("image"))
                return SENT_IMAGE_MESSAGE;
            else
                return SENT_MESSAGE;
        } else {
            if (mMessageList.get(position).getType().equals("image"))
                return RECEIVED_IMAGE_MESSAGE;
            else
                return RECEIVED_MESSAGE;
        }
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public class MessagesViewHolder extends RecyclerView.ViewHolder {

        public TextView message;
        public CircleImageView ava;
        public RoundedImageView image;
        public AVLoadingIndicatorView load;

        public MessagesViewHolder(View itemView) {
            super(itemView);

            message = itemView.findViewById(R.id.tv_message);
            ava = itemView.findViewById(R.id.iv_user);
            image = itemView.findViewById(R.id.iv_image);
            load = itemView.findViewById(R.id.av_load);
        }

        public void setData(Message ms) {
            message.setText(ms.getMessage());
        }

        public void setAva(String src) {
            if (!thumb_image.equals("thumb_link"))
                Picasso.get().load(thumb_image)
                        .placeholder(R.drawable.user)
                        .into(ava);
        }

        public void setImage(String src) {
            if (load != null)
                load.setVisibility(View.VISIBLE);
            Picasso.get().load(src)
                    .into(image, new Callback() {
                        @Override
                        public void onSuccess() {
                            if (load != null)
                                load.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });
        }

    }

    public interface OnClickItem {
        void onClick();
    }
}
