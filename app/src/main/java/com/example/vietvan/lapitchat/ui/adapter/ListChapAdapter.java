package com.example.vietvan.lapitchat.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.vietvan.lapitchat.R;
import com.example.vietvan.lapitchat.model.Message;
import com.example.vietvan.lapitchat.ui.activity.Chats;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by VietVan on 01/08/2018.
 */

public class ListChapAdapter extends RecyclerView.Adapter<ListChapAdapter.ListChapViewHolder> {

    List<Message> list;
    String uid;
    Context context;

    public ListChapAdapter(List<Message> list, String uid, Context context) {
        this.list = list;
        this.uid = uid;
        this.context = context;
    }

    @NonNull
    @Override
    public ListChapAdapter.ListChapViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, null);

        return new ListChapViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ListChapAdapter.ListChapViewHolder holder, int position) {
        final Message message = list.get(position);

        holder.setAva(message.getImage());
        holder.setName(message.getName());
        holder.setOnline(message.getOnline());
        holder.setMessage(message.getMessage());
        holder.setTime(String.valueOf(message.getTime()));

        if(message.getSeen().equals("false") && !message.getFrom().equals(uid))
            holder.setNotSeen();
        else
            holder.setSeen();

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ListChapViewHolder extends RecyclerView.ViewHolder {

        public ImageView ava, online;
        public TextView message, time, name;

        public ListChapViewHolder(View itemView) {
            super(itemView);

            ava = itemView.findViewById(R.id.iv_user);
            name = itemView.findViewById(R.id.tv_name);
            message = itemView.findViewById(R.id.tv_last_message);
            time = itemView.findViewById(R.id.tv_time);
            online = itemView.findViewById(R.id.iv_online);
        }

        public void setName(String n){
            name.setText(n);
        }

        public void setMessage(String mes){
            message.setText(mes);
        }

        public void setTime(String time){
            Date date = new Date(Long.parseLong(time));
            Date crDate = new Date();

            String format = "";
            if (date.getDate() == crDate.getDate() && date.getMonth() == crDate.getMonth() && date.getYear() == crDate.getYear())
                format = new SimpleDateFormat("HH:mm").format(date);
            else
                format = new SimpleDateFormat("MMM d").format(date);
            this.time.setText(format);
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


    }
}
