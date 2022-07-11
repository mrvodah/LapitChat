package com.example.vietvan.lapitchat.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.example.vietvan.lapitchat.R;
import com.example.vietvan.lapitchat.model.Message;
import com.example.vietvan.lapitchat.model.User;
import com.example.vietvan.lapitchat.phonecall.PlaceCall;
import com.example.vietvan.lapitchat.ui.adapter.MessagesAdapter;
import com.example.vietvan.lapitchat.utils.Common;
import com.example.vietvan.lapitchat.utils.GetTimeAgo;
import com.example.vietvan.lapitchat.utils.ImageUtils;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class Chats extends AppCompatActivity implements MessagesAdapter.OnClickItem {

    private static final String TAG = "TAG";
    private static final int GALLERY_ITEM = 1;
    private static final int MY_CAMERA_PERMISSION_CODE = 2;
    private static final int CAMERA_REQUEST = 3;
    @BindView(R.id.iv_add)
    ImageView ivAdd;
    @BindView(R.id.edt_message)
    EmojiconEditText edtMessage;
    @BindView(R.id.iv_send)
    ImageView ivSend;
    @BindView(R.id.rv_messages)
    RecyclerView rvMessages;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout swLayout;
    @BindView(R.id.iv_ava)
    CircleImageView ivAva;
    @BindView(R.id.tv_name)
    TextView tvName;
    @BindView(R.id.tv_lastSeen)
    TextView tvLastSeen;
    @BindView(R.id.ll_collapse)
    LinearLayout llCollapse;
    @BindView(R.id.iv_icon)
    ImageView ivIcon;

    String mChatUser, mCurrentUserId, userName, thumb_image, location;
    List<Message> mMessageList;
    MessagesAdapter mAdapter;
    LinearLayoutManager mLinearLayout;

    public static final int TOTAL_ITEMS_TO_LOAD = 10;
    public int mCurrentPage = 1;
    public int itemPos = 0;
    public String mLastKey = "", mPrevKey = "";
    public boolean is = false, time = true;
    public Handler handler;
    public Runnable runnable;
    public EmojIconActions emojIconActions;
    String mCurrentPhotoPath;

    public User user;
    Uri photoURI;

    DatabaseReference root, messages;
    FirebaseAuth mAth;
    StorageReference mImageStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        root = FirebaseDatabase.getInstance().getReference();
        messages = FirebaseDatabase.getInstance().getReference();
        mAth = FirebaseAuth.getInstance();
        mImageStorage = FirebaseStorage.getInstance().getReference();

        mCurrentUserId = mAth.getCurrentUser().getUid();
        mChatUser = getIntent().getStringExtra("uid");
        userName = getIntent().getStringExtra("user_name");
        thumb_image = getIntent().getStringExtra("image");

        updateReadMess();

        tvName.setText(userName);

        // emoji init
        View v = findViewById(R.id.root_view);
        emojIconActions = new EmojIconActions(getApplicationContext(), v, edtMessage, ivIcon);
        emojIconActions.ShowEmojIcon();

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

        root.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                user = dataSnapshot.getValue(User.class);

                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("thumb_image").getValue().toString();
                String lastOnline = dataSnapshot.child("lastOnline").getValue().toString();

                thumb_image = image;
                if (online.equals("true"))
                    tvLastSeen.setText("Online");
                else {

                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    long lastTime = Long.parseLong(lastOnline);
                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());
                    tvLastSeen.setText(lastSeenTime);
                }

                if (!image.equals("thumb_image"))
                    Picasso.get().load(image)
                            .placeholder(R.drawable.user)
                            .into(ivAva);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        root.child("Chats").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChild(mChatUser)) {

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", "false");
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chats/" + mCurrentUserId + "/" + mChatUser, chatAddMap);
                    chatUserMap.put("Chats/" + mChatUser + "/" + mCurrentUserId, chatAddMap);

                    root.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError != null)
                                Log.d(TAG, "onComplete: " + databaseError.getMessage().toString());

                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mMessageList = new ArrayList<>();
        mLinearLayout = new LinearLayoutManager(this);
        mLinearLayout.setStackFromEnd(true);
        mAdapter = new MessagesAdapter(mMessageList, thumb_image, this, this);
        rvMessages.setHasFixedSize(true);
        rvMessages.setLayoutManager(mLinearLayout);
        rvMessages.setAdapter(mAdapter);

        edtMessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                Log.d(TAG, "onFocusChange: focus");
                time = true;
                updateReadMess();
                is = true;
                setChangeImage();
                setAnimation(edtMessage, 750, 300);
                setAnimation(llCollapse, 0, 300);
                llCollapse.setVisibility(View.GONE);

                handler = new Handler();
                handler.postDelayed(runnable = new Runnable() {
                    @Override
                    public void run() {
                        is = false;
                        setChangeImage();
                        setAnimation(edtMessage, 480, 300);
                        setAnimation(llCollapse, 267, 300);
                        llCollapse.setVisibility(View.VISIBLE);

                    }
                }, 5000);

            }
        });

        edtMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: click");
                time = true;
                updateReadMess();
                is = true;
                setChangeImage();
                setAnimation(edtMessage, 750, 300);
                setAnimation(llCollapse, 0, 300);
                llCollapse.setVisibility(View.GONE);

                handler = new Handler();
                handler.postDelayed(runnable = new Runnable() {
                    @Override
                    public void run() {
                        is = false;
                        setChangeImage();
                        setAnimation(edtMessage, 480, 300);
                        setAnimation(llCollapse, 267, 300);
                        llCollapse.setVisibility(View.VISIBLE);

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
                if(!is && s.length() > 0){
                    is = true;
                    setChangeImage();
                    setAnimation(edtMessage, 750, 300);
                    setAnimation(llCollapse, 0, 300);
                    llCollapse.setVisibility(View.GONE);
                }
                if (s.equals("") || count == 0) {
                    is = false;
                    setChangeImage();
                    setAnimation(edtMessage, 480, 300);
                    setAnimation(llCollapse, 267, 300);
                    llCollapse.setVisibility(View.VISIBLE);
                } else {

                    handler.removeCallbacks(runnable);

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void updateReadMess() {
        Log.d(TAG, "updateReadMess: ");
        root.child("Messages").child(mCurrentUserId).child(mChatUser).orderByKey().limitToLast(1)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Log.d(TAG, "onChildAdded: 123456");
                        if(time){
                            time = false;
                            String key = dataSnapshot.getKey();

                            String ref_2 = "Messages/" + mCurrentUserId + "/" + mChatUser + "/" + key;

                            Map local = new HashMap();
                            local.put(ref_2 + "/seen", "true");

                            messages.updateChildren(local);
                        }

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

    public void setAnimation(View v, int width, int duration) {
//        ResizeWidthAnimation ll = new ResizeWidthAnimation(v, width);
//        ll.setDuration(duration);
//        v.clearAnimation();
//        v.setAnimation(ll);
    }

    private void loadMoreMessage() {

        DatabaseReference messageRef = root.child("Messages").child(mCurrentUserId).child(mChatUser);

        Query query = messageRef.orderByKey().endAt(mPrevKey).limitToLast(TOTAL_ITEMS_TO_LOAD);

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Message message = dataSnapshot.getValue(Message.class);

                if (!mPrevKey.equals(dataSnapshot.getKey()))
                    mMessageList.add(itemPos++, message);
                else
                    mPrevKey = mLastKey;

                if (itemPos == 1) {
                    mLastKey = dataSnapshot.getKey();
                }

                mAdapter.notifyDataSetChanged();
//                rvMessages.scrollToPosition(mMessageList.size() - 1);

                swLayout.setRefreshing(false);
                Log.d(TAG, "onChildAdded: " + itemPos);
                mLinearLayout.scrollToPositionWithOffset(itemPos, 0);

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

        DatabaseReference messageRef = root.child("Messages").child(mCurrentUserId).child(mChatUser);

        Query query = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Message message = dataSnapshot.getValue(Message.class);

                itemPos++;
                if (itemPos == 1) {
                    mLastKey = dataSnapshot.getKey();
                    mPrevKey = dataSnapshot.getKey();
                }

                mMessageList.add(message);
                mAdapter.notifyDataSetChanged();
                rvMessages.scrollToPosition(mMessageList.size() - 1);

                swLayout.setRefreshing(false);
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

    private void sendMessage() {
        Log.d(TAG, "sendMessage: 1111");
        String message = edtMessage.getText().toString();

        if (!TextUtils.isEmpty(message)) {
            edtMessage.setText("");

            String current_user_ref = "Messages/" + mCurrentUserId + "/" + mChatUser;
            String chat_user_ref = "Messages/" + mChatUser + "/" + mCurrentUserId;

            DatabaseReference user_message_push = root.child("Messages")
                    .child(mCurrentUserId).child(mChatUser).push();

            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", "false");
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserId);

            Map messageeUserMap = new HashMap();
            messageeUserMap.put(current_user_ref + "/" + push_id, messageMap);
            messageeUserMap.put(chat_user_ref + "/" + push_id, messageMap);

            root.updateChildren(messageeUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null)
                        Log.d(TAG, "onComplete: " + databaseError.getMessage().toString());

                }
            });

        }

    }

    @OnClick({R.id.iv_back, R.id.iv_info, R.id.iv_video, R.id.iv_call,
            R.id.iv_add, R.id.iv_send, R.id.iv_photo, R.id.iv_camera, R.id.iv_icon})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.iv_info:

                Intent profile = new Intent(Chats.this, ProfileUser.class);
                profile.putExtra("uid", mChatUser);
                profile.putExtra("user", user);
                startActivity(profile);

                break;
            case R.id.iv_video:

                Common.click = 2;
                Intent video = new Intent(Chats.this, Video_PlaceCall.class);
                video.putExtra("uid", mChatUser);
                video.putExtra("name", userName);
                startActivity(video);

                break;
            case R.id.iv_call:

                Common.click = 1;
                Intent call = new Intent(Chats.this, PlaceCall.class);
                call.putExtra("uid", mChatUser);
                call.putExtra("name", userName);
                startActivity(call);

                break;
            case R.id.iv_add:

                if (is) {

                    is = false;
                    setChangeImage();
                    setAnimation(edtMessage, 480, 300);
                    setAnimation(llCollapse, 267, 300);
                    llCollapse.setVisibility(View.VISIBLE);

                } else {


                }

                break;
            case R.id.iv_camera:
                if (Build.VERSION.SDK_INT >= 23) {
                    if (checkSelfPermission(Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA},
                                MY_CAMERA_PERMISSION_CODE);
                    } else {
                        dispatchTakePictureIntent();
                    }
                }

                break;
            case R.id.iv_photo:

                Intent gallery = new Intent();
                gallery.setType("image/*");
                gallery.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(gallery, "SELECT IMAGE"), GALLERY_ITEM);

                break;
            case R.id.iv_send:

                sendMessage();

                break;
            case R.id.iv_icon:
                Log.d(TAG, "onViewClicked: iconClick");
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                openCamera();
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }

        }
    }

    public void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, CAMERA_REQUEST);
        }
    }

    public void sortArray(){
        Collections.sort(mMessageList, new Comparator<Message>(){
            public int compare(Message obj1, Message obj2) {
                // ## Ascending order
//                return obj1.firstName.compareToIgnoreCase(obj2.firstName); // To compare string values
                // return Integer.valueOf(obj1.empId).compareTo(obj2.empId); // To compare integer values

                // ## Descending order
                // return obj2.firstName.compareToIgnoreCase(obj1.firstName); // To compare string values
                 return Long.valueOf(obj2.getTime()).compareTo(obj1.getTime()); // To compare integer values
            }
        });
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File...
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        this.getPackageName() + ".provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_ITEM && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();

            final String current_user_ref = "Messages/" + mCurrentUserId + "/" + mChatUser;
            final String chat_user_ref = "Messages/" + mChatUser + "/" + mCurrentUserId;

            DatabaseReference user_message_push = root.child("Messages")
                    .child(mCurrentUserId).child(mChatUser).push();

            final String push_id = user_message_push.getKey();

            StorageReference filepath = mImageStorage.child("message_images/" + push_id + ".jpg");

            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if (task.isSuccessful()) {

                        final String downloadUri = task.getResult().getDownloadUrl().toString();

                        Map messageMap = new HashMap();
                        messageMap.put("message", downloadUri);
                        messageMap.put("seen", "false");
                        messageMap.put("type", "image");
                        messageMap.put("time", ServerValue.TIMESTAMP);
                        messageMap.put("from", mCurrentUserId);

                        Map messageeUserMap = new HashMap();
                        messageeUserMap.put(current_user_ref + "/" + push_id, messageMap);
                        messageeUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                        root.updateChildren(messageeUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError != null)
                                    Log.d(TAG, "onComplete: " + databaseError.getMessage().toString());

                            }
                        });

                    }

                }
            });
        }

        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {

            final String current_user_ref = "Messages/" + mCurrentUserId + "/" + mChatUser;
            final String chat_user_ref = "Messages/" + mChatUser + "/" + mCurrentUserId;

            DatabaseReference user_message_push = root.child("Messages")
                    .child(mCurrentUserId).child(mChatUser).push();

            final String push_id = user_message_push.getKey();

            StorageReference filepath = mImageStorage.child("message_images/" + push_id + ".jpg");

            filepath.putFile(photoURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if (task.isSuccessful()) {

                        final String downloadUri = task.getResult().getDownloadUrl().toString();

                        Map messageMap = new HashMap();
                        messageMap.put("message", downloadUri);
                        messageMap.put("seen", "false");
                        messageMap.put("type", "image");
                        messageMap.put("time", ServerValue.TIMESTAMP);
                        messageMap.put("from", mCurrentUserId);

                        Map messageeUserMap = new HashMap();
                        messageeUserMap.put(current_user_ref + "/" + push_id, messageMap);
                        messageeUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                        root.updateChildren(messageeUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError != null)
                                    Log.d(TAG, "onComplete: " + databaseError.getMessage().toString());

                            }
                        });

                    }

                }
            });

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri imageUri = result.getUri();

                Log.d(TAG, "onActivityResult: " + imageUri);

                final String current_user_ref = "Messages/" + mCurrentUserId + "/" + mChatUser;
                final String chat_user_ref = "Messages/" + mChatUser + "/" + mCurrentUserId;

                DatabaseReference user_message_push = root.child("Messages")
                        .child(mCurrentUserId).child(mChatUser).push();

                final String push_id = user_message_push.getKey();

                StorageReference filepath = mImageStorage.child("message_images/" + push_id + ".jpg");

                filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {

                            final String downloadUri = task.getResult().getDownloadUrl().toString();

                            Map messageMap = new HashMap();
                            messageMap.put("message", downloadUri);
                            messageMap.put("seen", "false");
                            messageMap.put("type", "image");
                            messageMap.put("time", ServerValue.TIMESTAMP);
                            messageMap.put("from", mCurrentUserId);

                            Map messageeUserMap = new HashMap();
                            messageeUserMap.put(current_user_ref + "/" + push_id, messageMap);
                            messageeUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                            root.updateChildren(messageeUserMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    if (databaseError != null)
                                        Log.d(TAG, "onComplete: " + databaseError.getMessage().toString());

                                }
                            });

                        }

                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

    public void setChangeImage() {
        if (!is)
            ivAdd.setImageResource(R.drawable.add);
        else
            ivAdd.setImageResource(R.drawable.fast_forward);
    }

    @Override
    public void onClick() {
        is = false;
        setChangeImage();
        setAnimation(edtMessage, 480, 300);
        setAnimation(llCollapse, 267, 300);
        llCollapse.setVisibility(View.VISIBLE);
        Common.hideKeyboard(Chats.this);
    }
}
