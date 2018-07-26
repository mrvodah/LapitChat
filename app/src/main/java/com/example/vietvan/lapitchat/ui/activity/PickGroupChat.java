package com.example.vietvan.lapitchat.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.example.vietvan.lapitchat.R;
import com.example.vietvan.lapitchat.model.Group;
import com.example.vietvan.lapitchat.ui.adapter.PickGroupChatAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PickGroupChat extends AppCompatActivity {

    private static final String TAG = "TAG";
    @BindView(R.id.lv_people)
    ListView lvPeople;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout swipeLayout;

    FirebaseDatabase database;
    DatabaseReference chats, users, groups, conversations, root;

    String uid;
    List<Group> list;
    Group group;
    PickGroupChatAdapter adapter;
    public static List<Group> grList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_group_chat);
        ButterKnife.bind(this);

        uid = FirebaseAuth.getInstance().getUid();
        grList = new ArrayList<>();
        group = new Group(uid);

        database = FirebaseDatabase.getInstance();
        chats = database.getReference("Chats");
        users = database.getReference("Users");
        groups = database.getReference("Groups");
        conversations = database.getReference("Conversations");

        list = new ArrayList<>();
        adapter = new PickGroupChatAdapter(this, R.layout.item_group_chat, list);
        lvPeople.setAdapter(adapter);

        users.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                group.setName(dataSnapshot.child("name").getValue().toString());
                grList.add(group);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        loadListFriends();

    }

    private void loadListFriends() {

        chats.child(uid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                final String key = dataSnapshot.getKey();

                users.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        Group user = dataSnapshot.getValue(Group.class);
                        user.setKey(key);
                        list.add(user);
                        adapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

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


    @OnClick({R.id.iv_back, R.id.iv_check})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.iv_check:

                Log.d(TAG, "onViewClicked: " + grList);

                if(grList.size() == 1){

                }
                else if(grList.size() == 2) {

                    Intent message = new Intent(PickGroupChat.this, Chats.class);
                    message.putExtra("uid", grList.get(1).getKey());
                    message.putExtra("image", grList.get(1).getThumb_image());
                    message.putExtra("user_name", grList.get(1).getName());
                    startActivity(message);
                    finish();

                }
                else {

                    Intent intent = new Intent(PickGroupChat.this, ChatGroups.class);
                    intent.putExtra("is", true);
                    startActivity(intent);
                    finish();

                }
                break;
        }
    }
}
