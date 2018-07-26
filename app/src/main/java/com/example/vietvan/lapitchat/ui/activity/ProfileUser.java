package com.example.vietvan.lapitchat.ui.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vietvan.lapitchat.R;
import com.example.vietvan.lapitchat.model.Friend;
import com.example.vietvan.lapitchat.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProfileUser extends AppCompatActivity {

    private static final String TAG = "TAG";
    @BindView(R.id.pf_iv)
    ImageView pfIv;
    @BindView(R.id.pf_name)
    TextView pfName;
    @BindView(R.id.pf_status)
    TextView pfStatus;
    @BindView(R.id.pf_friends)
    TextView pfFriends;
    @BindView(R.id.pf_btn_send_rq)
    Button pfBtnSendRq;
    @BindView(R.id.pf_btn_decline_rq)
    Button pfBtnDeclineRq;

    String uid;
    String uid_user;
    String mCurrent_state;

    FirebaseDatabase database;
    DatabaseReference friends_req;
    DatabaseReference friends;
    DatabaseReference notif;

    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_user);
        ButterKnife.bind(this);

        database = FirebaseDatabase.getInstance();
        friends_req = database.getReference("Friends_req");
        friends = database.getReference("Friends");
        notif = database.getReference("Noti");

        user = (User) getIntent().getSerializableExtra("user");
        uid = getIntent().getStringExtra("uid");
        uid_user = FirebaseAuth.getInstance().getUid();

        pfName.setText(user.getName());
        pfStatus.setText(user.getStatus());
        mCurrent_state = "not_friend";

        if (!user.getImage().equals("link_image"))
            Picasso.get().load(user.getImage())
                    .placeholder(R.drawable.user)
                    .into(pfIv);

        // -------------------------- REQUESTS FRIEND / FRIEND STATE -----------------------

        friends_req.child(uid_user).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild(uid)){

                    String req_type = dataSnapshot.child(uid).child("request_type").getValue().toString();

                    if(req_type.equals("received")){
                        Log.d(TAG, "onDataChange: 1");
                        mCurrent_state = "req_received";
                        pfBtnSendRq.setText("Accept Friend Request");

                        pfBtnDeclineRq.setVisibility(View.VISIBLE);
                        pfBtnDeclineRq.setEnabled(true);

                    }
                    else if(req_type.equals("sent")){
                        Log.d(TAG, "onDataChange: 2");
                        mCurrent_state = "req_sent";
                        pfBtnSendRq.setText("Cancel Friend Request");

                        pfBtnDeclineRq.setVisibility(View.INVISIBLE);
                        pfBtnDeclineRq.setEnabled(false);

                    }

                }
                else{

                    friends.child(uid_user).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if(dataSnapshot.hasChild(uid)){
                                Log.d(TAG, "onDataChange: 3");
                                mCurrent_state = "friends";
                                pfBtnSendRq.setText("Unfriend");

                                pfBtnDeclineRq.setVisibility(View.INVISIBLE);
                                pfBtnDeclineRq.setEnabled(false);

                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    @OnClick({R.id.pf_btn_send_rq, R.id.pf_btn_decline_rq})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.pf_btn_send_rq:

                pfBtnSendRq.setEnabled(false);

                // -------------------------- NOT FRIEND STATE -----------------------

                if(mCurrent_state.equals("not_friend")){

                    friends_req.child(uid).child(uid_user).child("request_type").setValue("received")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {

                                        friends_req.child(uid_user).child(uid).child("request_type").setValue("sent")
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        HashMap<String, String> map = new HashMap<>();
                                                        map.put("from", uid_user);
                                                        map.put("type", "request");

                                                        notif.child(uid).push().setValue(map)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {

                                                                        pfBtnSendRq.setEnabled(true);
                                                                        mCurrent_state = "req_sent";
                                                                        pfBtnSendRq.setText("Cancel Friend Request");

                                                                        pfBtnDeclineRq.setVisibility(View.INVISIBLE);
                                                                        pfBtnDeclineRq.setEnabled(false);

                                                                    }
                                                                });

                                                        Toast.makeText(ProfileUser.this, "Request Sent Success~", Toast.LENGTH_SHORT).show();

                                                    }
                                                });

                                    } else {

                                        Toast.makeText(ProfileUser.this, "Failure Sending Request!", Toast.LENGTH_SHORT).show();

                                    }

                                }
                            });

                }

                // -------------------------- CANCEL FRIEND STATE -----------------------

                if(mCurrent_state.equals("req_sent")){

                    friends_req.child(uid).child(uid_user).child("request_type").removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){

                                        friends_req.child(uid_user).child(uid).child("request_type").removeValue()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        pfBtnSendRq.setEnabled(true);
                                                        mCurrent_state = "req_sent";
                                                        pfBtnSendRq.setText("Send Friend Request");

                                                        pfBtnDeclineRq.setVisibility(View.INVISIBLE);
                                                        pfBtnDeclineRq.setEnabled(false);

                                                        Toast.makeText(ProfileUser.this, "Cancel Request Friend Success~", Toast.LENGTH_SHORT).show();

                                                    }
                                                });

                                    } else {

                                        Toast.makeText(ProfileUser.this, "Failure Cancel Request!", Toast.LENGTH_SHORT).show();

                                    }

                                }
                            });

                }

                // -------------------------- REQ FRIEND STATE -----------------------

                if(mCurrent_state.equals("req_received")){

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    friends.child(uid).child(uid_user).setValue(new Friend(currentDate))
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){

                                        friends.child(uid_user).child(uid).setValue(new Friend(currentDate))
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        friends_req.child(uid).child(uid_user).child("request_type").removeValue()
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                        if(task.isSuccessful()){

                                                                            friends_req.child(uid_user).child(uid).child("request_type").removeValue()
                                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                        @Override
                                                                                        public void onSuccess(Void aVoid) {

                                                                                            pfBtnSendRq.setEnabled(true);
                                                                                            mCurrent_state = "friends";
                                                                                            pfBtnSendRq.setText("Unfriend");

                                                                                            pfBtnDeclineRq.setVisibility(View.INVISIBLE);
                                                                                            pfBtnDeclineRq.setEnabled(false);

                                                                                            Toast.makeText(ProfileUser.this, "Cancel Request Friend Success~", Toast.LENGTH_SHORT).show();

                                                                                        }
                                                                                    });

                                                                        } else {

                                                                            Toast.makeText(ProfileUser.this, "Failure Cancel Request!", Toast.LENGTH_SHORT).show();

                                                                        }

                                                                    }
                                                                });

                                                    }
                                                });

                                    }

                                }
                            });

                }

                if(mCurrent_state.equals("friends")){

                    friends.child(uid).child(uid_user).child("date").removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){

                                        friends.child(uid_user).child(uid).child("date").removeValue()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        pfBtnSendRq.setEnabled(true);
                                                        mCurrent_state = "not_friend";
                                                        pfBtnSendRq.setText("Send friend request");

                                                        pfBtnDeclineRq.setVisibility(View.INVISIBLE);
                                                        pfBtnDeclineRq.setEnabled(false);

                                                        Toast.makeText(ProfileUser.this, "Unfriend Success~", Toast.LENGTH_SHORT).show();

                                                    }
                                                });

                                    } else {

                                        Toast.makeText(ProfileUser.this, "Failure Unfriend Request!", Toast.LENGTH_SHORT).show();

                                    }

                                }
                            });

                }

                break;
            case R.id.pf_btn_decline_rq:

                friends_req.child(uid).child(uid_user).child("request_type").removeValue()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if(task.isSuccessful()){

                                    friends_req.child(uid_user).child(uid).child("request_type").removeValue()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    pfBtnSendRq.setEnabled(true);
                                                    mCurrent_state = "not_friend";
                                                    pfBtnSendRq.setText("Send friend request");

                                                    pfBtnDeclineRq.setVisibility(View.INVISIBLE);
                                                    pfBtnDeclineRq.setEnabled(false);

                                                    Toast.makeText(ProfileUser.this, "Decline Friend Request Success~", Toast.LENGTH_SHORT).show();

                                                }
                                            });

                                } else {

                                    Toast.makeText(ProfileUser.this, "Failure Decline Friend Request!", Toast.LENGTH_SHORT).show();

                                }

                            }
                        });

                break;
        }
    }
}
