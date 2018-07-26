package com.example.vietvan.lapitchat.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.vietvan.lapitchat.ItemClickListener;
import com.example.vietvan.lapitchat.R;

/**
 * Created by VietVan on 24/07/2018.
 */

public class GroupViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView message;

    ItemClickListener itemClickListener;

    public GroupViewHolder(View itemView) {
        super(itemView);

        message = itemView.findViewById(R.id.tv_last_message);
        itemView.setOnClickListener(this);
    }

    public void setMessage(String mes){
        message.setText(mes);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v, getAdapterPosition());
    }

}
