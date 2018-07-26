package com.example.vietvan.lapitchat.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vietvan.lapitchat.R;
import com.example.vietvan.lapitchat.model.User;
import com.example.vietvan.lapitchat.utils.Common;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class UserInfo extends AppCompatActivity {

    private static final int GALLERY_ITEM = 1;
    @BindView(R.id.settings_image)
    CircleImageView settingsImage;
    @BindView(R.id.settings_name)
    TextView settingsName;
    @BindView(R.id.settings_status)
    TextView settingsStatus;
    @BindView(R.id.settings_image_btn)
    Button settingsImageBtn;
    @BindView(R.id.settings_status_btn)
    Button settingsStatusBtn;

    FirebaseDatabase database;
    DatabaseReference users;
    StorageReference storage;
    StorageReference thumb_file;

    ProgressDialog progressDialog;
    String uid, status;
    byte[] thumb_byte;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        ButterKnife.bind(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading info");
        progressDialog.setMessage("Please wait ...");
        progressDialog.show();

        database = FirebaseDatabase.getInstance();
        users = database.getReference("Users");
        users.keepSynced(true);
        storage = FirebaseStorage.getInstance().getReference();
        thumb_file = FirebaseStorage.getInstance().getReference();

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        users.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                progressDialog.dismiss();

                final User user = dataSnapshot.getValue(User.class);

                settingsName.setText(user.getName());
                settingsStatus.setText(user.getStatus());
                status = user.getStatus();

                if (!user.getImage().equals("link_image"))
                    Picasso.get().load(user.getImage())
                            .placeholder(R.drawable.user)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(settingsImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError(Exception e) {

                                    Picasso.get().load(user.getImage())
                                            .placeholder(R.drawable.user)
                                            .into(settingsImage);

                                }
                            });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (!Common.isConnectedToInternet(this)) {
            settingsImageBtn.setEnabled(false);
            settingsStatusBtn.setEnabled(false);
        }

    }

    @OnClick({R.id.settings_image_btn, R.id.settings_status_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.settings_image_btn:

                Intent gallery = new Intent();
                gallery.setType("image/*");
                gallery.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(gallery, "SELECT IMAGE"), GALLERY_ITEM);

                break;
            case R.id.settings_status_btn:

                Intent status = new Intent(UserInfo.this, Status.class);
                status.putExtra("status", this.status);
                startActivity(status);

                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_ITEM && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();

            // start cropping activity for pre-acquired image saved on the device
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                Uri resultUri = result.getUri();

                File thumb_path = new File(resultUri.getPath());

                try {
                    Bitmap thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_path);

                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    thumb_byte = bytes.toByteArray();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                progressDialog.setTitle("Uploading Image");
                progressDialog.setMessage("Please wait ...");
                progressDialog.show();

                storage.child("profile_images/" + uid + ".jpg").putFile(resultUri)
                        .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

                                final String downloadUri = task.getResult().getDownloadUrl().toString();

                                UploadTask uploadTask = thumb_file.child("profile_images/thumbs/").child(uid + ".jpg").putBytes(thumb_byte);
                                uploadTask.addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Handle unsuccessful uploads
                                        progressDialog.dismiss();
                                        Toast.makeText(UserInfo.this, "Error in Uploading thumbnail!", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                        .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                                String thumb_link = thumb_task.getResult().getDownloadUrl().toString();

                                                Map<String, Object> map = new HashMap<>();
                                                map.put("image", downloadUri);
                                                map.put("thumb_image", thumb_link);

                                                users.child(uid).updateChildren(map)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                progressDialog.dismiss();
                                                            }
                                                        });

                                                Toast.makeText(UserInfo.this, "Successful Uploading ...", Toast.LENGTH_SHORT).show();
                                            }
                                        });


                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(UserInfo.this, "Error in Uploading image!", Toast.LENGTH_SHORT).show();
                            }
                        });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }

    }
}
