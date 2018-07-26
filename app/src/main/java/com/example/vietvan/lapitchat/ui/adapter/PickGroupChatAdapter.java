package com.example.vietvan.lapitchat.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;

import com.example.vietvan.lapitchat.R;
import com.example.vietvan.lapitchat.model.Group;
import com.example.vietvan.lapitchat.ui.activity.PickGroupChat;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by VietVan on 24/07/2018.
 */

public class PickGroupChatAdapter extends ArrayAdapter<Group> {


    public PickGroupChatAdapter(Context context, int resource) {
        super(context, resource);
    }

    public PickGroupChatAdapter(Context context, int resource, List<Group> list) {
        super(context, resource, list);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.item_group_chat, null);
        }

        final Group group = getItem(position);

        CircleImageView image = v.findViewById(R.id.iv_user);
        final CheckedTextView name = v.findViewById(R.id.tv_name);

        if (group != null) {

            if(!group.getImage().equals("thumb_image"))
                Picasso.get().load(group.getImage())
                        .placeholder(R.drawable.user)
                        .into(image);
            name.setText(group.getName());

        }

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(name.isChecked()){
                    if(PickGroupChat.grList.contains(group))
                        PickGroupChat.grList.remove(group);
                    name.setChecked(false);
                }
                else{
                    if(!PickGroupChat.grList.contains(group))
                        PickGroupChat.grList.add(group);
                    name.setChecked(true);
                }

            }
        });

        return v;
    }
}
