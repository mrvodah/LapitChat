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
import com.example.vietvan.lapitchat.model.Friend;
import com.example.vietvan.lapitchat.model.User;
import com.example.vietvan.lapitchat.phonecall.PlaceCall;
import com.example.vietvan.lapitchat.ui.activity.Chats;
import com.example.vietvan.lapitchat.ui.activity.PickGroupChat;
import com.example.vietvan.lapitchat.ui.activity.ProfileUser;
import com.example.vietvan.lapitchat.ui.adapter.FriendsViewHolder;
import com.example.vietvan.lapitchat.utils.Common;
import com.example.vietvan.lapitchat.videocall.Video_PlaceCall;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private static final String TAG = "TAG";
    @BindView(R.id.rv_friends)
    RecyclerView rvFriends;
    @BindView(R.id.rl_empty)
    RelativeLayout rlEmpty;

    FirebaseDatabase database;
    DatabaseReference users;
    DatabaseReference friends;

    FirebaseRecyclerAdapter<Friend, FriendsViewHolder> adapter;

    String uid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_friends, container, false);
        ButterKnife.bind(this, v);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        rvFriends.setHasFixedSize(true);
        rvFriends.setLayoutManager(new LinearLayoutManager(getContext()));

        database = FirebaseDatabase.getInstance();
        users = database.getReference("Users");
        users.keepSynced(true);
        friends = database.getReference("Friends").child(uid);
        friends.keepSynced(true);

        loadListFriends();

        return v;
    }

    private void loadListFriends() {

        adapter = new FirebaseRecyclerAdapter<Friend, FriendsViewHolder>(
                Friend.class,
                R.layout.item_friends,
                FriendsViewHolder.class,
                friends
        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, final Friend friend, final int position) {

                final String user_id = getRef(position).getKey();

                users.child(user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        rlEmpty.setVisibility(View.GONE);

                        final User model = dataSnapshot.getValue(User.class);

                        viewHolder.setName(model.getName());
                        viewHolder.setStatus(model.getStatus());
                        viewHolder.setImage(model.getThumb_image());
                        viewHolder.setOnline(model.getOnline());

                        viewHolder.call.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Common.click = 1;
                                Intent call = new Intent(getActivity(), PlaceCall.class);
                                call.putExtra("uid", user_id);
                                call.putExtra("name", model.getName());
                                startActivity(call);

                            }
                        });

                        viewHolder.video.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Common.click = 2;
                                Intent call = new Intent(getActivity(), Video_PlaceCall.class);
                                call.putExtra("uid", user_id);
                                call.putExtra("name", model.getName());
                                startActivity(call);

                            }
                        });

                        viewHolder.message.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent message = new Intent(getActivity(), Chats.class);
                                message.putExtra("uid", user_id);
                                message.putExtra("image", model.getThumb_image());
                                message.putExtra("user_name", model.getName());
                                startActivity(message);

                            }
                        });

                        viewHolder.info.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent profile = new Intent(getActivity(), ProfileUser.class);
                                profile.putExtra("uid", user_id);
                                profile.putExtra("user", model);
                                startActivity(profile);

                            }
                        });

                        viewHolder.setItemClickListener(new ItemClickListener() {
                            @Override
                            public void onClick(View v, int position) {

//                                Toast.makeText(getContext(), "" + position, Toast.LENGTH_SHORT).show();

                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };

        rvFriends.setAdapter(adapter);

    }

    @OnClick(R.id.fab_gr)
    public void onViewClicked() {
        startActivity(new Intent(getContext(), PickGroupChat.class));
    }
}
