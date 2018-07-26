package com.example.vietvan.lapitchat.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.vietvan.lapitchat.R;
import com.example.vietvan.lapitchat.phonecall.BaseActivity;
import com.example.vietvan.lapitchat.phonecall.SinchService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sinch.android.rtc.SinchError;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignIn extends BaseActivity implements SinchService.StartFailedListener{

    private static final String TAG = "TAG";
    @BindView(R.id.main_app_bar)
    Toolbar toolBar;
    @BindView(R.id.edtEmail)
    TextInputLayout edtEmail;
    @BindView(R.id.edtPassword)
    TextInputLayout edtPassword;
    @BindView(R.id.rl_load)
    RelativeLayout rlLoad;
    private ProgressDialog mSpinner;

    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference users;

    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        ButterKnife.bind(this);

        setSupportActionBar(toolBar);
        getSupportActionBar().setTitle("Login");
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

    @OnClick(R.id.si_btnSignIn)
    public void onViewClicked() {

        rlLoad.setVisibility(View.VISIBLE);

        auth.signInWithEmailAndPassword(edtEmail.getEditText().getText().toString(),
                edtPassword.getEditText().getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        rlLoad.setVisibility(View.GONE);

                        if (task.isSuccessful()) {

                            users.child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    uid = auth.getUid();

                                    if (!uid.equals(getSinchServiceInterface().getUserName())) {
                                        getSinchServiceInterface().stopClient();
                                    }

                                    if (!getSinchServiceInterface().isStarted()) {
                                        getSinchServiceInterface().startClient(uid);
                                    } else {
                                        openPlaceCallActivity();
                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        } else {
                            Toast.makeText(SignIn.this, "Login Failure~",
                                    Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });

    }

    @Override
    protected void onServiceConnected() {
        getSinchServiceInterface().setStartListener(this);
    }

    @Override
    public void onStartFailed(SinchError error) {
        Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show();
        if (mSpinner != null) {
            mSpinner.dismiss();
        }
    }

    @Override
    public void onStarted() {
        if(FirebaseAuth.getInstance().getCurrentUser() != null)
            openPlaceCallActivity();
    }

    @Override
    protected void onPause() {
        if (mSpinner != null) {
            mSpinner.dismiss();
        }
        super.onPause();
    }

    private void openPlaceCallActivity() {
        Intent signIn = new Intent(SignIn.this, Home.class);
        startActivity(signIn);
        finish();
    }

    private void showSpinner() {
        mSpinner = new ProgressDialog(this);
        mSpinner.setTitle("Logging in");
        mSpinner.setMessage("Please wait...");
        mSpinner.show();
    }

}
