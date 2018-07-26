package com.example.vietvan.lapitchat.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.vietvan.lapitchat.R;
import com.example.vietvan.lapitchat.ui.adapter.SectionsPagerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Home extends AppCompatActivity {

    private static final String TAG = "TAG";
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
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null){

            users.child("online").setValue("false");
            users.child("lastOnline").setValue(ServerValue.TIMESTAMP);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG , "onPause: ");
    }

    //    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//
//        FirebaseAuth.getInstance().signOut();
//        finish();
//    }

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
            case R.id.mn_account_settings:
                startActivity(new Intent(Home.this, UserInfo.class));
                break;
            case R.id.mn_all_users:
                startActivity(new Intent(Home.this, AllUsers.class));
                break;
            case R.id.mn_group_chat:
                startActivity(new Intent(Home.this, PickGroupChat.class));
                break;
            case R.id.mn_chats:
                startActivity(new Intent(Home.this, HaveGroups.class));
                break;
            case R.id.mn_log_out:
                users.child("online").setValue("false");
                users.child("lastOnline").setValue(ServerValue.TIMESTAMP);

                FirebaseAuth.getInstance().signOut();
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
