package com.example.vietvan.lapitchat.ui.adapter;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.vietvan.lapitchat.ItemClickListener;
import com.example.vietvan.lapitchat.R;
import com.squareup.picasso.Picasso;

/**
 * Created by VietVan on 16/07/2018.
 */

public class ChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public ImageView ava, online;
    public TextView message, time, name;

    ItemClickListener itemClickListener;

    public ChatViewHolder(View itemView) {
        super(itemView);

        ava = itemView.findViewById(R.id.iv_user);
        name = itemView.findViewById(R.id.tv_name);
        message = itemView.findViewById(R.id.tv_last_message);
        time = itemView.findViewById(R.id.tv_time);
        online = itemView.findViewById(R.id.iv_online);

        itemView.setOnClickListener(this);
    }

    public void setName(String n){
        name.setText(n);
    }

    public void setMessage(String mes){
        message.setText(mes);
    }

    public void setTime(String time){

    }

    public void setNotSeen(){

        name.setTextSize(20);
        name.setTypeface(Typeface.DEFAULT_BOLD);

        message.setTextSize(15);
        message.setTextColor(Color.parseColor("#000000"));
        message.setTypeface(Typeface.DEFAULT_BOLD);

        time.setTextSize(11);
        time.setTypeface(Typeface.DEFAULT_BOLD);
    }

    public void setSeen(){
        name.setTextSize(19);
        name.setTypeface(Typeface.DEFAULT);

        message.setTextSize(14);
        message.setTextColor(Color.parseColor("#737373"));
        message.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));

        time.setTextSize(10);
        time.setTypeface(Typeface.DEFAULT);
    }

    public void setOnline(String is){
        if(is.equals("true"))
            online.setBackgroundResource(R.drawable.dot_online);
        else
            online.setBackgroundResource(R.drawable.dot_offline);
    }

    public void setAva(String src){
        if(!src.equals("thumb_image"))
            Picasso.get().load(src)
                    .placeholder(R.drawable.user)
                    .into(ava);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v, getAdapterPosition());
    }
}
