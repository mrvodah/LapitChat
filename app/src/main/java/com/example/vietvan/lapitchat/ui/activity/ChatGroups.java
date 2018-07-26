package com.example.vietvan.lapitchat.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.vietvan.lapitchat.R;
import com.example.vietvan.lapitchat.model.LastMessage;
import com.example.vietvan.lapitchat.phonecall.PlaceCall;
import com.example.vietvan.lapitchat.ui.adapter.LastMessageAdapter;
import com.example.vietvan.lapitchat.utils.Common;
import com.example.vietvan.lapitchat.utils.ResizeWidthAnimation;
import com.example.vietvan.lapitchat.videocall.Video_PlaceCall;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class ChatGroups extends AppCompatActivity implements LastMessageAdapter.OnClickItem{

    private static final String TAG = "TAG";
    private static final int GALLERY_ITEM = 1;
    @BindView(R.id.rv_messages)
    RecyclerView rvMessages;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout swLayout;
    @BindView(R.id.ll_collapse)
    LinearLayout llCollapse;
    @BindView(R.id.edt_message)
    EmojiconEditText edtMessage;
    @BindView(R.id.ll_bot)
    LinearLayout llBot;
    @BindView(R.id.root_view)
    RelativeLayout rootView;
    @BindView(R.id.iv_add)
    ImageView ivAdd;
    @BindView(R.id.iv_icon)
    ImageView ivIcon;
    @BindView(R.id.tv_name)
    TextView tvName;
    @BindView(R.id.tv_lastSeen)
    TextView tvLastSeen;
    @BindView(R.id.iv_ava)
    CircleImageView ivAva;

    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference groups, conversations, root;
    StorageReference mImageStorage;

    String uid;
    boolean isNew = false, is = false, isadd = false;
    String push_convers_id, keygr;

    List<LastMessage> mMessageList;
    LastMessageAdapter mAdapter;
    LinearLayoutManager mLinearLayout;
    List<String> listPeople;

    public static final int TOTAL_ITEMS_TO_LOAD = 10;
    public int mCurrentPage = 1;
    public int itemPos = 0;
    public String mLastKey = "", mPrevKey = "";
    public Handler handler;
    public Runnable runnable;
    public EmojIconActions emojIconActions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_groups);
        ButterKnife.bind(this);

        uid = FirebaseAuth.getInstance().getUid();
        auth = FirebaseAuth.getInstance();
        isNew = getIntent().getBooleanExtra("is", false);
        if (!isNew)
            keygr = getIntent().getStringExtra("key");

        database = FirebaseDatabase.getInstance();
        groups = database.getReference("groups");
        conversations = database.getReference("conversations");
        root = database.getReference();
        mImageStorage = FirebaseStorage.getInstance().getReference();

        // emoji init
        View v = findViewById(R.id.root_view);
        emojIconActions = new EmojIconActions(getApplicationContext(), v, edtMessage, ivIcon);
        emojIconActions.ShowEmojIcon();

        listPeople = new ArrayList<>();
        mMessageList = new ArrayList<>();
        mLinearLayout = new LinearLayoutManager(this);
        mLinearLayout.setStackFromEnd(true);
        mAdapter = new LastMessageAdapter(mMessageList, this, this);
        rvMessages.setHasFixedSize(true);
        rvMessages.setLayoutManager(mLinearLayout);
        rvMessages.setAdapter(mAdapter);

        createNewConvers();
        updateMessageUnread();

        // init swift
        swLayout.setColorSchemeResources(
                R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark
        );
        swLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;
                itemPos = 0;
                loadMoreMessage();
            }
        });

        // load for first times
        swLayout.post(new Runnable() {
            @Override
            public void run() {
                loadMessages();
            }
        });

        groups.child(uid).child(push_convers_id)
                .child("info").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String avatar = dataSnapshot.child("avatar").getValue().toString();
                String name = dataSnapshot.child("name").getValue().toString();

                tvName.setText(name);
                if (!avatar.equals("something"))
                    Picasso.get().load(avatar)
                            .placeholder(R.drawable.user)
                            .into(ivAva);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        edtMessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                Log.d(TAG, "onFocusChange: focus");
                is = true;
                setChangeImage();
                setAnimation(edtMessage, 750, 300);
                setAnimation(llCollapse, 0, 300);

                handler = new Handler();
                handler.postDelayed(runnable = new Runnable() {
                    @Override
                    public void run() {
                        is = false;
                        setChangeImage();
                        setAnimation(edtMessage, 480, 300);
                        setAnimation(llCollapse, 267, 300);

                    }
                }, 5000);

            }
        });

        edtMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: click");
                is = true;
                setChangeImage();
                setAnimation(edtMessage, 750, 300);
                setAnimation(llCollapse, 0, 300);

                handler = new Handler();
                handler.postDelayed(runnable = new Runnable() {
                    @Override
                    public void run() {
                        is = false;
                        setChangeImage();
                        setAnimation(edtMessage, 480, 300);
                        setAnimation(llCollapse, 267, 300);

                    }
                }, 5000);
            }
        });

        edtMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "onTextChanged: " + s + "/" + start + "/" + before + "/" + count);
                if (s.equals("") || count == 0) {
                    is = false;
                    setChangeImage();
                    setAnimation(edtMessage, 480, 300);
                    setAnimation(llCollapse, 267, 300);
                } else {

                    handler.removeCallbacks(runnable);

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void updateMessageUnread() {

        String ref = "groups" + "/" + uid + "/" + push_convers_id + "/last_message/read";

        Map push = new HashMap();
        push.put(ref, true);

        root.updateChildren(push);

    }

    public void setAnimation(View v, int width, int duration) {
        ResizeWidthAnimation ll = new ResizeWidthAnimation(v, width);
        ll.setDuration(duration);
        v.clearAnimation();
        v.setAnimation(ll);
    }

    public void setChangeImage() {
        if (!is)
            ivAdd.setImageResource(R.drawable.add);
        else
            ivAdd.setImageResource(R.drawable.fast_forward);
    }

    private void createNewConvers() {

        if (isNew) {

            String n = "";
            n += PickGroupChat.grList.get(0).getName();

            for(int i=1;i<PickGroupChat.grList.size();i++)
                n += ", " + PickGroupChat.grList.get(i).getName();

            DatabaseReference databaseReference = conversations.push();
            push_convers_id = databaseReference.getKey();

            for (int i = 0; i < PickGroupChat.grList.size(); i++) {

                String key = PickGroupChat.grList.get(i).getKey();

                String chat_user_ref = "groups/" + key + "/" + push_convers_id;

                Map info = new HashMap();
                info.put("avatar", "something");
                info.put("name", n);

                Map messageUserMap = new HashMap();
                messageUserMap.put(chat_user_ref + "/" + "info", info);

                root.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null)
                            Log.d(TAG, "onComplete: " + databaseError.getMessage().toString());
                    }
                });

            }

            for (int i = 0; i < PickGroupChat.grList.size(); i++) {

                String key1 = PickGroupChat.grList.get(i).getKey();

                for (int j = 0; j < PickGroupChat.grList.size(); j++) {

                    String key2 = PickGroupChat.grList.get(j).getKey();

                    if (!key1.equals(key2)) {

                        String chat_user_ref = "groups/" + key1 + "/" + push_convers_id;

                        Map participants = new HashMap();
                        participants.put("name", PickGroupChat.grList.get(j).getName());
                        participants.put("time", ServerValue.TIMESTAMP);

                        Map messageUserMap = new HashMap();
                        messageUserMap.put(chat_user_ref + "/" + "participants" + "/" + key2, participants);

                        root.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError != null)
                                    Log.d(TAG, "onComplete: " + databaseError.getMessage().toString());
                            }
                        });

                    }

                }

            }

            for (int i = 0; i < PickGroupChat.grList.size(); i++)
                listPeople.add(PickGroupChat.grList.get(i).getKey());


        } else {
            push_convers_id = keygr;
            listPeople.add(uid);
            groups.child(uid).child(push_convers_id).child("participants").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    String k = dataSnapshot.getKey();
                    listPeople.add(k);

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

    }

    private void loadMoreMessage() {

        DatabaseReference messageRef = conversations.child(push_convers_id).child("messages");

        Query query = messageRef.orderByKey().endAt(mPrevKey).limitToLast(TOTAL_ITEMS_TO_LOAD);

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                final LastMessage lastMessage = dataSnapshot.getValue(LastMessage.class);

                final String key = dataSnapshot.getKey();

                root.child("Users").child(lastMessage.getFromID()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        lastMessage.setName(dataSnapshot.child("name").getValue().toString());
                        lastMessage.setImage(dataSnapshot.child("thumb_image").getValue().toString());

                        if (!mPrevKey.equals(key))
                            mMessageList.add(itemPos++, lastMessage);
                        else
                            mPrevKey = mLastKey;

                        if (itemPos == 1) {
                            mLastKey = key;
                        }

                        mAdapter.notifyDataSetChanged();
//                        rvMessages.scrollToPosition(mMessageList.size() - 1);
                        mLinearLayout.scrollToPositionWithOffset(itemPos, 0);

                        swLayout.setRefreshing(false);

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

    private void loadMessages() {

        DatabaseReference messageRef = conversations.child(push_convers_id).child("messages");

        Query query = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                final LastMessage lastMessage = dataSnapshot.getValue(LastMessage.class);

                final String key = dataSnapshot.getKey();

                root.child("Users").child(lastMessage.getFromID()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        lastMessage.setName(dataSnapshot.child("name").getValue().toString());
                        lastMessage.setImage(dataSnapshot.child("thumb_image").getValue().toString());

                        itemPos++;
                        if (itemPos == 1) {
                            mLastKey = key;
                            mPrevKey = key;
                        }

                        mMessageList.add(lastMessage);
                        mAdapter.notifyDataSetChanged();
                        rvMessages.scrollToPosition(mMessageList.size() - 1);

                        swLayout.setRefreshing(false);

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

    @OnClick({R.id.iv_back, R.id.iv_info, R.id.iv_video, R.id.iv_call,
            R.id.iv_add, R.id.iv_camera, R.id.iv_photo, R.id.iv_icon, R.id.iv_send})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.iv_info:

//                Intent profile = new Intent(ChatGroups.this, ProfileUser.class);
//                profile.putExtra("uid", uid);
//                profile.putExtra("user", user);
//                startActivity(profile);

                break;
            case R.id.iv_video:

//                Common.click = 2;
//                Intent video = new Intent(ChatGroups.this, Video_PlaceCall.class);
//                video.putExtra("uid", uid);
//                video.putExtra("name", userName);
//                startActivity(video);

                break;
            case R.id.iv_call:

//                Common.click = 1;
//                Intent call = new Intent(ChatGroups.this, PlaceCall.class);
//                call.putExtra("uid", uid);
//                call.putExtra("name", userName);
//                startActivity(call);

                break;
            case R.id.iv_add:

                if (is) {

                    is = false;
                    setChangeImage();
                    setAnimation(edtMessage, 480, 300);
                    setAnimation(llCollapse, 267, 300);

                } else {


                }

                break;
            case R.id.iv_camera:
                break;
            case R.id.iv_photo:

                Intent gallery = new Intent();
                gallery.setType("image/*");
                gallery.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(gallery, "SELECT IMAGE"), GALLERY_ITEM);

                break;
            case R.id.iv_icon:

                break;
            case R.id.iv_send:
                sendMessage("text", "");
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_ITEM && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();

            DatabaseReference user_message_push = conversations.child(push_convers_id)
                    .child("messages").push();

            final String push_id = user_message_push.getKey();

            StorageReference filepath = mImageStorage.child("message_images/" + push_id + ".jpg");

            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if (task.isSuccessful()) {

                        final String downloadUri = task.getResult().getDownloadUrl().toString();
                        sendMessage("image", downloadUri);
                    }

                }
            });
        }

    }

    private void sendMessage(String type, String src) {

        String message = "";

        if(type.equals("text")){
            message = edtMessage.getText().toString();
            edtMessage.setText("");

        } else if(type.equals("image")){
            message = src;
        }

        if (!TextUtils.isEmpty(message)) {

            Map local = new HashMap();
            local.put("fromID", uid);
            local.put("toID", "");
            local.put("content", message);
            local.put("type", type);
            local.put("time", ServerValue.TIMESTAMP);
            local.put("read", false);

            conversations.child(push_convers_id).child("messages").push()
                    .updateChildren(local);

            for (int i = 0; i < listPeople.size(); i++) {

                String key = listPeople.get(i);

                String chat_user_ref = "groups/" + key + "/" + push_convers_id;

                if(uid.equals(key))
                    local.put("read", true);
                else
                    local.put("read", false);

                Map messageUserMap = new HashMap();
                messageUserMap.put(chat_user_ref + "/" + "last_message", local);

                root.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null)
                            Log.d(TAG, "onComplete: " + databaseError.getMessage().toString());
                    }
                });

            }

        }

    }

    @Override
    public void onClick() {
        is = false;
        setChangeImage();
        setAnimation(edtMessage, 480, 300);
        setAnimation(llCollapse, 267, 300);
        Common.hideKeyboard(ChatGroups.this);
    }

}
