package com.example.vietvan.lapitchat.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.vietvan.lapitchat.ItemClickListener;
import com.example.vietvan.lapitchat.R;
import com.squareup.picasso.Picasso;

/**
 * Created by VietVan on 14/07/2018.
 */

public class FriendsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public ImageView image, online;
    public ImageView call, message, info, video;
    public TextView name, status;

    ItemClickListener itemClickListener;

    public FriendsViewHolder(View itemView) {
        super(itemView);

        image = itemView.findViewById(R.id.iv_user);
        name = itemView.findViewById(R.id.tv_name);
        status = itemView.findViewById(R.id.tv_status);
        online = itemView.findViewById(R.id.iv_online);
        call = itemView.findViewById(R.id.iv_call);
        video = itemView.findViewById(R.id.iv_video);
        message = itemView.findViewById(R.id.iv_message);
        info = itemView.findViewById(R.id.iv_info);

        call.setOnClickListener(this);
        itemView.setOnClickListener(this);
    }

    public void setName(String name){
        this.name.setText(name);
    }

    public void setOnline(String is){
        if(is.equals("true"))
            online.setBackgroundResource(R.drawable.dot_online);
        else
            online.setBackgroundResource(R.drawable.dot_offline);
    }

    public void setImage(String src){
        if(!src.equals("thumb_image"))
            Picasso.get().load(src)
                    .placeholder(R.drawable.user)
                    .into(image);
    }

    public void setStatus(String status){
        this.status.setText(status);
    }


    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v, getAdapterPosition());
    }
}
