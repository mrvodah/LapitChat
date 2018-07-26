package com.example.vietvan.lapitchat.ui.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.vietvan.lapitchat.R;
import com.example.vietvan.lapitchat.model.User;
import com.example.vietvan.lapitchat.utils.Common;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignUp extends AppCompatActivity {

    private static final String TAG = "TAG";
    @BindView(R.id.main_app_bar)
    Toolbar toolBar;
    @BindView(R.id.edtName)
    TextInputLayout edtName;
    @BindView(R.id.edtEmail)
    TextInputLayout edtEmail;
    @BindView(R.id.edtPassword)
    TextInputLayout edtPassword;
    @BindView(R.id.rl_load)
    RelativeLayout rlLoad;

    FirebaseDatabase database;
    FirebaseAuth auth;
    DatabaseReference users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);

        setSupportActionBar(toolBar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        users = database.getReference("Users");

    }

    @OnClick(R.id.btnSignUp)
    public void onViewClicked() {

        rlLoad.setVisibility(View.VISIBLE);

        auth.createUserWithEmailAndPassword(edtEmail.getEditText().getText().toString(),
                edtPassword.getEditText().getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        rlLoad.setVisibility(View.GONE);

                        if (task.isSuccessful()) {


                            String uid = auth.getCurrentUser().getUid();

                            User user = new User(
                                    edtName.getEditText().getText().toString(),
                                    "link_image",
                                    "Hi there, I'm using LapitChat~",
                                    "thumb_link"
                            );
                            users.child(uid).setValue(user);

                            Toast.makeText(SignUp.this, "Create User Successful~",
                                    Toast.LENGTH_SHORT).show();

                            finish();

                        } else {
                            Toast.makeText(SignUp.this, "Failure Create User!",
                                    Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });

    }
}
