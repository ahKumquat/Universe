package com.example.universe;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.universe.Models.Chat;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements Login.IloginFragmentAction,
        HomeFragment.IhomeFragmentAction, ChatAdapter.IchatListRecyclerAction,
        ChatManager.IchatManagerFragmentAction, Register.IRegisterFragmentAction,
        Profile.IProfileFragmentAction, Setting.ISettingFragmentAction{
    private String TAG = Util.TAG;
    private Util util;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private String otherUserId;

    private GoogleSignInClient googleSignInClient;


    // For Google sign in
    private final ActivityResultLauncher<Intent> startActivityForResult =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK ) {
                    Task<GoogleSignInAccount> signInAccountTask =
                            GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    if (signInAccountTask.isSuccessful()) {
                        Toast.makeText(this,"Google sign in successful",Toast.LENGTH_SHORT).show();
                        try {
                            GoogleSignInAccount googleSignInAccount = signInAccountTask.getResult(ApiException.class);
                            if (googleSignInAccount != null) {
                                AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
                                mAuth.signInWithCredential(authCredential).addOnCompleteListener(this, task -> {
                                    if (task.isSuccessful()) {
                                        populateScreen();
                                    } else {
                                        Toast.makeText(MainActivity.this, "Authentication Failed :"
                                                + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } catch (ApiException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        //For Google sign in
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        util = Util.getInstance();
//        Log.d(TAG, "onCreate Activity: " + mAuth.getCurrentUser().getEmail());
        //TODO: comment this out when testing is not needed
        //test();
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
    public void populateLoginFragment() {
       getSupportFragmentManager().popBackStack();
    }

    @Override
    public void populateRegisterFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, Register.newInstance(), "FragmentRegister")
                .addToBackStack("FragmentLogin").commit();
    }

    @Override
    public void openChatManager() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, ChatManager.newInstance(), "FragmentChatManager")
                .addToBackStack("FragmentHome").commit();
    }

    @Override
    public void openProfile() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, Profile.newInstance(), "FragmentProfile")
                .addToBackStack("FragmentHome").commit();
    }

    @Override
    public void openPost() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, PostFragment.newInstance(), "FragmentPost")
                .addToBackStack("FragmentProfile").commit();
    }

    @Override
    public void logOut() {
        mAuth.signOut();
        populateScreen();
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

    public void signWithGoogle() {
        Intent intent = googleSignInClient.getSignInIntent();
        startActivityForResult.launch(intent);
    }

    @Override
    public void populateHomeFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("FragmentHome");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, Objects.requireNonNull(fragment))
                .addToBackStack("FragmentProfile").commit();
    }

    @Override
    public void populateSettingFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, Setting.newInstance(), "FragmentSetting")
                .addToBackStack("FragmentProfile").commit();
    }

    @Override
    public void populateProfileFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("FragmentProfile");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, Objects.requireNonNull(fragment))
                .addToBackStack("FragmentSetting").commit();
    }
}