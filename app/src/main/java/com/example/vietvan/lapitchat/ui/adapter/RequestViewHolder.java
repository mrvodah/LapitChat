package com.example.vietvan.lapitchat.ui.adapter;

import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.vietvan.lapitchat.ItemClickListener;
import com.example.vietvan.lapitchat.R;
import com.squareup.picasso.Picasso;

/**
 * Created by VietVan on 14/07/2018.
 */

public class RequestViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public ImageView ava;
    public Button tick, cancel;
    public TextView name, status;

    ItemClickListener itemClickListener;

    public RequestViewHolder(View itemView) {
        super(itemView);

        ava = itemView.findViewById(R.id.iv_user);
        tick = itemView.findViewById(R.id.iv_tick);
        cancel = itemView.findViewById(R.id.iv_cancel);
        name = itemView.findViewById(R.id.tv_name);
        status = itemView.findViewById(R.id.tv_status);

        itemView.setOnClickListener(this);
    }

    public void setName(String name){
        this.name.setText(name);
    }

    public void setImage(String src){
        if(!src.equals("thumb_image"))
            Picasso.get().load(src)
                    .placeholder(R.drawable.user)
                    .into(ava);
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
