package com.example.vietvan.lapitchat.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.example.vietvan.lapitchat.ItemClickListener;
import com.example.vietvan.lapitchat.R;
import com.example.vietvan.lapitchat.model.User;
import com.example.vietvan.lapitchat.ui.adapter.UserViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AllUsers extends AppCompatActivity {

    private static final String TAG = "TAG";
    @BindView(R.id.main_app_bar)
    Toolbar toolBar;
    @BindView(R.id.rv_users)
    RecyclerView rvUsers;

    FirebaseDatabase database;
    DatabaseReference users;

    FirebaseRecyclerAdapter<User, UserViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        ButterKnife.bind(this);

        setSupportActionBar(toolBar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        rvUsers.setHasFixedSize(true);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));

        database = FirebaseDatabase.getInstance();
        users = database.getReference("Users");

        adapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(
                User.class,
                R.layout.item_user,
                UserViewHolder.class,
                users
        ) {
            @Override
            protected void populateViewHolder(UserViewHolder viewHolder, final User model, int position) {

                Log.d(TAG, "populateViewHolder: " + model);
                viewHolder.setName(model.getName());
                viewHolder.setStatus(model.getStatus());
                viewHolder.setImage(model.getThumb_image());

                viewHolder.add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {



                    }
                });

                viewHolder.hide.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {



                    }
                });

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View v, int position) {

                        Intent profile = new Intent(AllUsers.this, ProfileUser.class);
                        profile.putExtra("uid", adapter.getRef(position).getKey());
                        profile.putExtra("user", model);
                        startActivity(profile);

                    }
                });

            }
        };

        rvUsers.setAdapter(adapter);

    }
}
