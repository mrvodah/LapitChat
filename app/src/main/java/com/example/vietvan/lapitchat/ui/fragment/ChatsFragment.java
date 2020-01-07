package com.example.vietvan.lapitchat.ui.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.example.vietvan.lapitchat.ItemClickListener;
import com.example.vietvan.lapitchat.R;
import com.example.vietvan.lapitchat.model.Chat;
import com.example.vietvan.lapitchat.model.InfoGroup;
import com.example.vietvan.lapitchat.model.Message;
import com.example.vietvan.lapitchat.ui.activity.Chats;
import com.example.vietvan.lapitchat.ui.activity.HaveGroups;
import com.example.vietvan.lapitchat.ui.activity.PickGroupChat;
import com.example.vietvan.lapitchat.ui.adapter.ChatViewHolder;
import com.example.vietvan.lapitchat.ui.adapter.ListChapAdapter;
import com.example.vietvan.lapitchat.utils.Common;
import com.example.vietvan.lapitchat.utils.RecyclerItemClickListener;
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
import java.util.Collections;
import java.util.Comparator;
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

    List<Message> list;
    ListChapAdapter listChapAdapter;

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

        mCurrentUid = Common.getUid();

        database = FirebaseDatabase.getInstance();
        mes = database.getReference("Messages").child(mCurrentUid);
        mes.keepSynced(true);
        chats = database.getReference("Chats").child(mCurrentUid);
        chats.keepSynced(true);
        users = database.getReference("Users");

        linearLayoutManager = new LinearLayoutManager(getContext());
        rvChats.setHasFixedSize(true);
        rvChats.setLayoutManager(linearLayoutManager);

        loadListMessages();

//        loadList();

        return v;
    }

    private void loadList() {

        chats.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                final String list_user_id = dataSnapshot.getKey();

                Query lastMessageQuery = mes.child(list_user_id).limitToLast(1);

                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Log.d(TAG, "onChildAdded: +++");
                        rlEmpty.setVisibility(View.GONE);

                        final String message = dataSnapshot.child("message").getValue().toString();
                        final String time = dataSnapshot.child("time").getValue().toString();
                        final String seen = dataSnapshot.child("seen").getValue().toString();
                        final String from = dataSnapshot.child("from").getValue().toString();

                        users.child(list_user_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                boolean iskey = false;

                                final String userName = dataSnapshot.child("name").getValue().toString();
                                final String userThumb = dataSnapshot.child("thumb_image").getValue().toString();
                                String userOnline = "false";
                                if (dataSnapshot.hasChild("online")) {
                                    userOnline = dataSnapshot.child("online").getValue().toString();
                                }

                                for(int i=0;i<list.size();i++)
                                    if(list.get(i).getKey().equals(list_user_id)){
                                        if(!list.get(i).getOnline().equals(userOnline)){
                                            Log.d(TAG, "onChildChanged: seen");
                                            List<Message> temp = new ArrayList<>();
                                            temp.addAll(list);
                                            list.clear();
                                            temp.get(i).setOnline(userOnline);
                                            list.addAll(temp);
                                            listChapAdapter.notifyDataSetChanged();
                                            iskey = true;
                                        }
                                    }

                                if(!iskey){

                                    Message mes1 = new Message(message, seen, from, Long.parseLong(time), userName, userThumb, userOnline);
                                    mes1.setKey(list_user_id);
                                    list.add(mes1);
                                    sortList();
                                    listChapAdapter.notifyDataSetChanged();
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        Log.d(TAG, "onChildChanged: 1----");

                        final String message = dataSnapshot.child("message").getValue().toString();
                        final String time = dataSnapshot.child("time").getValue().toString();
                        final String seen = dataSnapshot.child("seen").getValue().toString();
                        final String from = dataSnapshot.child("from").getValue().toString();

                        for(int i=0;i<list.size();i++)
                            if(list.get(i).getKey().equals(list_user_id)){
                                if(!list.get(i).getMessage().equals(""))
                                if(!list.get(i).getMessage().equals(message)){
                                    Log.d(TAG, "onChildChanged: message");
                                    list.remove(i);
                                    listChapAdapter.notifyDataSetChanged();
                                }
                                if(!list.get(i).getSeen().equals(seen)){
                                    Log.d(TAG, "onChildChanged: seen");
                                    List<Message> temp = new ArrayList<>();
                                    temp.addAll(list);
                                    list.clear();
                                    temp.get(i).setSeen(seen);
                                    list.addAll(temp);
                                    listChapAdapter.notifyDataSetChanged();
                                }

                            }

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onChildRemoved: ");
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                        Log.d(TAG, "onChildMoved: ");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled: ");
                    }
                });

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

    }

    public void sortList(){
        Collections.sort(list, new Comparator<Message>(){
            public int compare(Message obj1, Message obj2) {
                // ## Ascending order
//                return obj1.firstName.compareToIgnoreCase(obj2.firstName); // To compare string values
                // return Integer.valueOf(obj1.empId).compareTo(obj2.empId); // To compare integer values

                return Long.compare(obj2.getTime(), obj1.getTime());
                // ## Descending order
                // return obj2.firstName.compareToIgnoreCase(obj1.firstName); // To compare string values
                // return Integer.valueOf(obj2.empId).compareTo(obj1.empId); // To compare integer values
            }
        });
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
                        Log.d(TAG, "onChildAdded: ");
                        rlEmpty.setVisibility(View.GONE);

                        String data = dataSnapshot.child("message").getValue().toString();
                        String time = dataSnapshot.child("time").getValue().toString();
                        String seen = dataSnapshot.child("seen").getValue().toString();
                        String from = dataSnapshot.child("from").getValue().toString();

                        if(seen.equals("false") && !from.equals(mCurrentUid))
                            viewHolder.setNotSeen();
                        else
                            viewHolder.setSeen();

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
                        Log.d(TAG, "onChildChanged: ");
                        String seen = dataSnapshot.child("seen").getValue().toString();
                        String from = dataSnapshot.child("from").getValue().toString();

                        if(seen.equals("false") && !from.equals(mCurrentUid))
                            viewHolder.setNotSeen();
                        else
                            viewHolder.setSeen();

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onChildRemoved: ");
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                        Log.d(TAG, "onChildMoved: ");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled: ");
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
