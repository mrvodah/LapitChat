package com.example.vietvan.lapitchat.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.vietvan.lapitchat.R;
import com.example.vietvan.lapitchat.model.Group;
import com.example.vietvan.lapitchat.model.InfoGroup;
import com.example.vietvan.lapitchat.model.Message;
import com.example.vietvan.lapitchat.ui.adapter.HaveGroupAdapter;
import com.example.vietvan.lapitchat.utils.RecyclerItemClickListener;
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

public class HaveGroups extends AppCompatActivity {

    private static final String TAG = "";
    @BindView(R.id.rv_chats)
    RecyclerView rvChats;

    FirebaseDatabase database;
    DatabaseReference groups, root, users;

    String uid;
    String content = "", time = "0", read = "", from = "";
    String avatar = "", name = "";
    List<InfoGroup> list;
    public static List<String> keyList;
    LinearLayoutManager mLinearLayout;
    HaveGroupAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_have_groups);
        ButterKnife.bind(this);

        uid = FirebaseAuth.getInstance().getUid();

        database = FirebaseDatabase.getInstance();
        groups = database.getReference("groups");
        root = database.getReference();
        users = database.getReference("Users");

        list = new ArrayList<>();
        keyList = new ArrayList<>();
        keyList.add(uid);
        mLinearLayout = new LinearLayoutManager(this);
        adapter = new HaveGroupAdapter(list, this);
        rvChats.setHasFixedSize(true);
        rvChats.setLayoutManager(mLinearLayout);
        rvChats.setAdapter(adapter);
        rvChats.addOnItemTouchListener(new RecyclerItemClickListener(this, rvChats, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(HaveGroups.this, ChatGroups.class);
                intent.putExtra("is", false);
                intent.putExtra("key", list.get(position).getKey());
                startActivity(intent);
                finish();
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }
        }));

        loadListGroups();

    }

    private void loadListGroups() {

        groups.child(uid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                final String key = dataSnapshot.getKey();
                if(dataSnapshot.hasChild("info")){

                    avatar = dataSnapshot.child("info").child("avatar").getValue().toString();
                    name = dataSnapshot.child("info").child("name").getValue().toString();
                }

                if(dataSnapshot.hasChild("last_message")){

                    content = dataSnapshot.child("last_message").child("content").getValue().toString();
                    time = dataSnapshot.child("last_message").child("time").getValue().toString();
                    read = dataSnapshot.child("last_message").child("read").getValue().toString();
                    from = dataSnapshot.child("last_message").child("fromID").getValue().toString();

                }
                Log.d(TAG, "onChildAdded: 1111" + name + "/");

//                if(!from.equals("")){
//                    Log.d(TAG, "onChildAdded: 2222");
//                    users.child(from).addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot dataSnapshot) {
//
//                            String fromID = dataSnapshot.child("name").getValue().toString();
//
//
//
//                        }
//
//                        @Override
//                        public void onCancelled(DatabaseError databaseError) {
//
//                        }
//                    });
//
//                }

                InfoGroup info = new InfoGroup(avatar, name, content, key, Long.parseLong(time), Boolean.parseBoolean(read), "");
                Log.d(TAG, "onDataChange: " + info);
                list.add(info);
                adapter.notifyDataSetChanged();

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

    @OnClick(R.id.iv_back)
    public void onViewClicked() {
        onBackPressed();
    }
}
