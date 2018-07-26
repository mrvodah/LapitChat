package com.example.vietvan.lapitchat.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.vietvan.lapitchat.ItemClickListener;
import com.example.vietvan.lapitchat.R;
import com.squareup.picasso.Picasso;

/**
 * Created by VietVan on 12/07/2018.
 */

public class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public ImageView image;
    public TextView name, status;
    public Button add, hide;

    ItemClickListener itemClickListener;

    public UserViewHolder(View itemView) {
        super(itemView);

        image = itemView.findViewById(R.id.iv_user);
        name = itemView.findViewById(R.id.tv_name);
        status = itemView.findViewById(R.id.tv_status);
        add = itemView.findViewById(R.id.iv_add);
        hide = itemView.findViewById(R.id.iv_hide);

        itemView.setOnClickListener(this);
    }

    public void setName(String name){
        this.name.setText(name);
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
