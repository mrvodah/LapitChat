package com.example.vietvan.lapitchat.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.vietvan.lapitchat.ItemClickListener;
import com.example.vietvan.lapitchat.R;
import com.example.vietvan.lapitchat.model.Friend;
import com.example.vietvan.lapitchat.model.Request;
import com.example.vietvan.lapitchat.model.User;
import com.example.vietvan.lapitchat.ui.activity.ProfileUser;
import com.example.vietvan.lapitchat.ui.adapter.RequestViewHolder;
import com.example.vietvan.lapitchat.utils.Common;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RequestsFragment extends Fragment {

    @BindView(R.id.rv_requests)
    RecyclerView rvRequests;
    @BindView(R.id.rl_empty)
    RelativeLayout rlEmpty;

    FirebaseDatabase database;
    DatabaseReference users;
    DatabaseReference friends_req;
    DatabaseReference friends;
    DatabaseReference req;

    FirebaseRecyclerAdapter<Request, RequestViewHolder> adapter;

    String uid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_requests, container, false);
        ButterKnife.bind(this, v);

        uid = Common.getUid();

        database = FirebaseDatabase.getInstance();
        users = database.getReference("Users");
        users.keepSynced(true);
        friends_req = database.getReference("Friends_req").child(uid);
        friends_req.keepSynced(true);
        req = database.getReference("Friends_req");
        req.keepSynced(true);
        friends = database.getReference("Friends");
        friends.keepSynced(true);

        rvRequests.setHasFixedSize(true);
        rvRequests.setLayoutManager(new LinearLayoutManager(getContext()));

        loadListFriendReq();

        return v;
    }

    private void loadListFriendReq() {

        adapter = new FirebaseRecyclerAdapter<Request, RequestViewHolder>(
                Request.class,
                R.layout.item_request,
                RequestViewHolder.class,
                friends_req.orderByChild("request_type").equalTo("received")
        ) {
            @Override
            protected void populateViewHolder(final RequestViewHolder viewHolder, Request request, final int position) {

                final String user_id = getRef(position).getKey();

                users.child(user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        rlEmpty.setVisibility(View.GONE);

                        final User model = dataSnapshot.getValue(User.class);

                        viewHolder.setName(model.getName());
                        viewHolder.setStatus(model.getStatus());
                        viewHolder.setImage(model.getThumb_image());

                        viewHolder.tick.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                                friends.child(uid).child(user_id).setValue(new Friend(currentDate))
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()) {

                                                    friends.child(user_id).child(uid).setValue(new Friend(currentDate))
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {

                                                                    req.child(user_id).child(uid).child("request_type").removeValue()
                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {

                                                                                    req.child(uid).child(user_id).child("request_type").removeValue()
                                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                @Override
                                                                                                public void onSuccess(Void aVoid) {

                                                                                                    Toast.makeText(getContext(), "Accept request friend from " + model.getName() + " successful~", Toast.LENGTH_SHORT).show();

                                                                                                }
                                                                                            });

                                                                                }
                                                                            });

                                                                }
                                                            });

                                                }

                                            }
                                        });

                            }
                        });

                        viewHolder.cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                req.child(user_id).child(uid).child("request_type").removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                req.child(uid).child(user_id).child("request_type").removeValue()
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {

                                                                Toast.makeText(getContext(), "Cancel request friend from " + model.getName() + " successful~", Toast.LENGTH_SHORT).show();

                                                            }
                                                        });

                                            }
                                        });

                            }
                        });

                        viewHolder.setItemClickListener(new ItemClickListener() {
                            @Override
                            public void onClick(View v, int position) {

                                Intent profile = new Intent(getActivity(), ProfileUser.class);
                                profile.putExtra("uid", adapter.getRef(position).getKey());
                                profile.putExtra("user", model);
                                startActivity(profile);

                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };

        rvRequests.setAdapter(adapter);

    }
}
