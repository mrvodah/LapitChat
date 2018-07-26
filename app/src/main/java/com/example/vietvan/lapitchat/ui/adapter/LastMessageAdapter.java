package com.example.vietvan.lapitchat.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.vietvan.lapitchat.R;
import com.example.vietvan.lapitchat.model.LastMessage;
import com.example.vietvan.lapitchat.ui.activity.FullImage;
import com.google.firebase.auth.FirebaseAuth;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by VietVan on 24/07/2018.
 */

public class LastMessageAdapter extends RecyclerView.Adapter<LastMessageAdapter.LastMessageViewHolder> {

    List<LastMessage> list;
    FirebaseAuth auth;
    Context context;
    public final int SENT_MESSAGE = 0, RECEIVED_MESSAGE = 1;
    public final int SENT_IMAGE_MESSAGE = 2, RECEIVED_IMAGE_MESSAGE = 3;

    OnClickItem itemClickListener;

    public LastMessageAdapter(List<LastMessage> list, Context context, OnClickItem itemClickListener) {
        this.list = list;
        this.context = context;
        auth = FirebaseAuth.getInstance();
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public LastMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = null;
        switch (viewType) {
            case SENT_MESSAGE:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, null);
                break;
            case RECEIVED_MESSAGE:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received_gr, null);
                break;
            case SENT_IMAGE_MESSAGE:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_sent, null);
                break;
            case RECEIVED_IMAGE_MESSAGE:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_received_gr, null);
                break;
        }

        return new LastMessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LastMessageViewHolder holder, final int position) {
        if (list.get(position).getType().equals("image")) {
            holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent profile = new Intent(context, FullImage.class);
                    profile.putExtra("image", list.get(position).getContent());
                    context.startActivity(profile);

                }
            });
            holder.setImage(list.get(position).getContent());
        } else
        holder.setData(list.get(position));

        if(holder.name != null)
            holder.setname(list.get(position).getName());

        if(holder.ava != null)
            holder.setAva(list.get(position).getImage());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListener.onClick();
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        if (list.get(position).getFromID().equals(auth.getUid())) {
            if (list.get(position).getType().equals("image"))
                return SENT_IMAGE_MESSAGE;
            else
                return SENT_MESSAGE;
        } else {
            if (list.get(position).getType().equals("image"))
                return RECEIVED_IMAGE_MESSAGE;
            else
                return RECEIVED_MESSAGE;
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class LastMessageViewHolder extends RecyclerView.ViewHolder {

        public TextView message, name;
        public CircleImageView ava;
        public RoundedImageView image;
        public AVLoadingIndicatorView load;

        public LastMessageViewHolder(View itemView){
            super(itemView);

            ava = itemView.findViewById(R.id.iv_user);
            message = itemView.findViewById(R.id.tv_message);
            name = itemView.findViewById(R.id.tv_name);
            image = itemView.findViewById(R.id.iv_image);
            load = itemView.findViewById(R.id.av_load);
        }

        public void setData(LastMessage ms) {
            message.setText(ms.getContent());
        }

        public void setname(String name){
            this.name.setText(name);
        }

        public void setAva(String src) {
            if (!src.equals("thumb_link"))
                Picasso.get().load(src)
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
