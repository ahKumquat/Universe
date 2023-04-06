package com.example.universe;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.example.universe.Models.Chat;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements Login.IloginFragmentAction,
        HomeFragment.IhomeFragmentAction, ChatAdapter.IchatListRecyclerAction,
        ChatManager.IchatManagerFragmentAction {
    private String TAG = Util.TAG;
    private Util util;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private String otherUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        util = Util.getInstance();
//        Log.d(TAG, "onCreate Activity: " + mAuth.getCurrentUser().getEmail());
        //TODO: comment this out when testing is not needed
        test();
    }

    public void test(){
        Test test = new Test();
        Thread thread = new Thread(test);
        thread.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        populateScreen();
    }

    private void populateScreen() {
        if (util.getCurrentUser() != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerMain, HomeFragment.newInstance(),"FragmentHome")
                    .addToBackStack(null)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerMain, Login.newInstance(),"FragmentLogin")
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void populateMainFragment(FirebaseUser mUser) {
        this.currentUser = mUser;
        populateScreen();
    }

    @Override
    public void openChatManager() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, ChatManager.newInstance(), "FragmentChatManager")
                .addToBackStack(null).commit();
    }

    @Override
    public void chatClickedFromRecyclerView(Chat chat) {
        util.readChat(chat.getOtherUserId(), new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                startChatPage(chat.getOtherUserId());
            }
        }, Util.DEFAULT_F_LISTENER);
    }

    @Override
    public void startChatPage(String otherUserId) {
        Bundle bundle = new Bundle();
        this.otherUserId = otherUserId;
        bundle.putString("otherUserId", otherUserId);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, ChatRoom.newInstance().getClass(),
                        bundle,"chatFragment")
                .commit();
        Log.d(TAG, "startChatPage: success");
    }
}