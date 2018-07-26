package com.example.vietvan.lapitchat.ui.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.example.vietvan.lapitchat.ItemClickListener;
import com.example.vietvan.lapitchat.R;
import com.example.vietvan.lapitchat.model.Chat;
import com.example.vietvan.lapitchat.ui.activity.Chats;
import com.example.vietvan.lapitchat.ui.activity.HaveGroups;
import com.example.vietvan.lapitchat.ui.activity.PickGroupChat;
import com.example.vietvan.lapitchat.ui.adapter.ChatViewHolder;
import com.example.vietvan.lapitchat.utils.Common;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private static final String TAG = "TAG";
    @BindView(R.id.rv_chats)
    RecyclerView rvChats;
    @BindView(R.id.rl_empty)
    RelativeLayout rlEmpty;

    FirebaseDatabase database;
    DatabaseReference mes, chats, users;
    DatabaseReference groups, root;

    FirebaseRecyclerAdapter<Chat, ChatViewHolder> adapter;

    String mCurrentUid, mChatUid;
    LinearLayoutManager linearLayoutManager;

    String uid;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chats, container, false);
        ButterKnife.bind(this, v);

        uid = FirebaseAuth.getInstance().getUid();

        database = FirebaseDatabase.getInstance();
        root = database.getReference();
        groups = database.getReference("groups");

//        rvChats.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), rvChats, new RecyclerItemClickListener.OnItemClickListener() {
//            @Override
//            public void onItemClick(View view, int position) {
//
//            }
//
//            @Override
//            public void onLongItemClick(View view, int position) {
//
//            }
//        }));

        mCurrentUid = Common.getUid();

        database = FirebaseDatabase.getInstance();
        mes = database.getReference("Messages").child(mCurrentUid);
        mes.keepSynced(true);
        chats = database.getReference("Chats").child(mCurrentUid);
        chats.keepSynced(true);
        users = database.getReference("Users");

        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        rvChats.setHasFixedSize(true);
        rvChats.setLayoutManager(new LinearLayoutManager(getContext()));

        loadListMessages();

        return v;
    }

    private void loadListMessages() {

        Query query = chats.orderByChild("timestamp");

        adapter = new FirebaseRecyclerAdapter<Chat, ChatViewHolder>(
                Chat.class,
                R.layout.item_chat,
                ChatViewHolder.class,
                query
        ) {
            @Override
            protected void populateViewHolder(final ChatViewHolder viewHolder, Chat model, final int position) {

                final String list_user_id = getRef(position).getKey();

                Query lastMessageQuery = mes.child(list_user_id).limitToLast(1);

                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        rlEmpty.setVisibility(View.GONE);

                        String data = dataSnapshot.child("message").getValue().toString();
                        String time = dataSnapshot.child("time").getValue().toString();
                        viewHolder.setMessage(data);

                        Date date = new Date(Long.parseLong(time));
                        Date crDate = new Date();

                        String format = "";
                        if (date.getDate() == crDate.getDate() && date.getMonth() == crDate.getMonth() && date.getYear() == crDate.getYear())
                            format = new SimpleDateFormat("HH:mm").format(date);
                        else
                            format = new SimpleDateFormat("MMM d").format(date);
                        viewHolder.time.setText(format);

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {


                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                users.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("name").getValue().toString();
                        final String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                        if (dataSnapshot.hasChild("online")) {

                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            viewHolder.setOnline(userOnline);

                        }

                        viewHolder.setName(userName);
                        viewHolder.setAva(userThumb);

                        viewHolder.setItemClickListener(new ItemClickListener() {
                            @Override
                            public void onClick(View v, int position) {

                                Intent chatIntent = new Intent(getContext(), Chats.class);
                                chatIntent.putExtra("uid", list_user_id);
                                chatIntent.putExtra("user_name", userName);
                                chatIntent.putExtra("image", userThumb);
                                startActivity(chatIntent);

                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };

        rvChats.setAdapter(adapter);

    }

    @OnClick(R.id.fab_gr)
    public void onViewClicked() {
        startActivity(new Intent(getContext(), HaveGroups.class));
    }
}
