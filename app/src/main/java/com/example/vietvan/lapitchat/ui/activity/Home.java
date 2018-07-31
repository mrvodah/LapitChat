package com.example.vietvan.lapitchat.ui.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.vietvan.lapitchat.R;
import com.example.vietvan.lapitchat.service.ChatHeadService;
import com.example.vietvan.lapitchat.ui.adapter.SectionsPagerAdapter;
import com.example.vietvan.lapitchat.utils.Common;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.internal.Utils;

public class Home extends AppCompatActivity {

    private static final String TAG = "TAG";
    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 1;
    private static final int OVERLAY_PERMISSION_REQ_CODE_CHATHEAD = 1011;
    @BindView(R.id.main_app_bar)
    Toolbar toolBar;
    @BindView(R.id.main_tabs)
    TabLayout mainTabs;
    @BindView(R.id.main_views)
    ViewPager mainViews;

    SectionsPagerAdapter mSectionsPagerAdapter;

    FirebaseAuth mAuth;
    DatabaseReference users;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null)
            users.child("online").setValue("true");
        else{

            Intent start = new Intent(Home.this, Intro.class);
            startActivity(start);
            finish();

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        setSupportActionBar(toolBar);
        getSupportActionBar().setTitle("LapitChat~");

        // Tabs
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mainViews.setAdapter(mSectionsPagerAdapter);
        mainTabs.setupWithViewPager(mainViews);

        mAuth = FirebaseAuth.getInstance();
        users = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getCurrentUser().getUid());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.mn_chat:
                if(Common.canDrawOverlays(Home.this))
                    startChatHead();
                else{
                    requestPermission(OVERLAY_PERMISSION_REQ_CODE_CHATHEAD);
                }

                break;
            case R.id.mn_account_settings:
                startActivity(new Intent(Home.this, UserInfo.class));
                break;
            case R.id.mn_all_users:
                startActivity(new Intent(Home.this, AllUsers.class));
                break;
            case R.id.mn_log_out:
                users.child("online").setValue("false");
                users.child("lastOnline").setValue(ServerValue.TIMESTAMP);

                FirebaseAuth.getInstance().signOut();

                startActivity(new Intent(Home.this, Intro.class));
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startChatHead(){
        startService(new Intent(Home.this, ChatHeadService.class));
        finish();
    }

    private void needPermissionDialog(final int requestCode){
        AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
        builder.setMessage("You need to allow permission");
        builder.setPositiveButton("OK",
                new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        requestPermission(requestCode);
                    }
                });
        builder.setNegativeButton("Cancel", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void requestPermission(int requestCode){
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, requestCode);
    }

    private void initializeView() {
        startService(new Intent(Home.this, ChatHeadService.class));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE_CHATHEAD) {
            if (!Common.canDrawOverlays(Home.this)) {
                needPermissionDialog(requestCode);
            }else{
                startChatHead();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
