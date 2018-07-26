package com.example.vietvan.lapitchat.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.vietvan.lapitchat.ItemClickListener;
import com.example.vietvan.lapitchat.R;
import com.example.vietvan.lapitchat.model.Group;
import com.example.vietvan.lapitchat.model.InfoGroup;
import com.example.vietvan.lapitchat.model.Message;
import com.example.vietvan.lapitchat.ui.activity.ChatGroups;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by VietVan on 24/07/2018.
 */

public class HaveGroupAdapter extends RecyclerView.Adapter<HaveGroupAdapter.HaveGroupViewHolder> {

    private static final String TAG = "";
    List<InfoGroup> list;
    Context context;
    public final int MESSAGE_READ = 0, MESSAGE_UNREAD = 1;

    public HaveGroupAdapter(List<InfoGroup> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public HaveGroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = null;
        switch (viewType){
            case MESSAGE_READ:
                v = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
                break;
            case MESSAGE_UNREAD:
                v = LayoutInflater.from(context).inflate(R.layout.item_chat_unread, parent, false);
                break;
        }

        return new HaveGroupViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final HaveGroupViewHolder holder, final int position) {
        holder.setMessage(list.get(position).getContent());
        holder.setName(list.get(position).getName());

        Date date = new Date(list.get(position).getTime());
        Date crDate = new Date();

        String format = "";
        if (date.getDate() == crDate.getDate() && date.getMonth() == crDate.getMonth() && date.getYear() == crDate.getYear())
            format = new SimpleDateFormat("HH:mm").format(date);
        else
            format = new SimpleDateFormat("MMM d").format(date);
        holder.setTime(format);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(list.get(position).isRead())
            return MESSAGE_READ;
        else
            return MESSAGE_UNREAD;
    }

    public class HaveGroupViewHolder extends RecyclerView.ViewHolder {

        public TextView message, time, name;
        public CircleImageView ava;

        public HaveGroupViewHolder(View itemView) {
            super(itemView);

            ava = itemView.findViewById(R.id.iv_user);
            name = itemView.findViewById(R.id.tv_name);
            message = itemView.findViewById(R.id.tv_last_message);
            time = itemView.findViewById(R.id.tv_time);
        }

        public void setName(String n){
            name.setText(n);
        }

        public void setMessage(String mes){
            message.setText(mes);
        }

        public void setTime(String t){
            time.setText(t);
        }

        public void setAva(String src){
            if(!src.equals("thumb_image"))
                Picasso.get().load(src)
                        .placeholder(R.drawable.user)
                        .into(ava);
        }
    }

}
