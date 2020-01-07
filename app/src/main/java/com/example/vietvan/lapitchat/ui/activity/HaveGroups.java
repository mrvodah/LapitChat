package com.example.vietvan.lapitchat.ui.activity;

import android.content.Intent;
import android.os.Build;
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
import java.util.Collections;
import java.util.Comparator;
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

    int count = 0;

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
//                finish();
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
                count++;
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

                InfoGroup info = new InfoGroup(avatar, name, content, key, Long.parseLong(time), Boolean.parseBoolean(read), "");
                list.add(info);
                sortList();
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                final String key = dataSnapshot.getKey();

                for(int i=0;i<list.size();i++)
                    if(list.get(i).getKey().equals(key)){
                        list.remove(i);
                        break;
                    }

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

                InfoGroup info = new InfoGroup(avatar, name, content, key, Long.parseLong(time), Boolean.parseBoolean(read), "");
                list.add(0, info);
                sortList();
                adapter.notifyDataSetChanged();

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
        Collections.sort(list, new Comparator<InfoGroup>(){
            public int compare(InfoGroup obj1, InfoGroup obj2) {
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

    @OnClick(R.id.iv_back)
    public void onViewClicked() {
        onBackPressed();
    }
}
