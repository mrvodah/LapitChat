package com.example.vietvan.lapitchat.ui.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.example.vietvan.lapitchat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Status extends AppCompatActivity {

    @BindView(R.id.main_app_bar)
    Toolbar toolbar;
    @BindView(R.id.edtStatus)
    TextInputLayout edtStatus;

    FirebaseDatabase database;
    DatabaseReference users;

    ProgressDialog progressDialog;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        edtStatus.getEditText().setText(getIntent().getStringExtra("status"));

        database = FirebaseDatabase.getInstance();
        users = database.getReference("Users");

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    }

    @OnClick(R.id.btnSave)
    public void onViewClicked() {

        progressDialog = new ProgressDialog(Status.this);
        progressDialog.setTitle("Changing Status");
        progressDialog.setMessage("Please wait ...");
        progressDialog.show();

        Map<String, Object> map = new HashMap<>();
        map.put("status", edtStatus.getEditText().getText().toString());

        users.child(uid).updateChildren(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        finish();
                    }
                });

    }
}
