package com.example.universe;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private String TAG = Util.TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        Log.d(TAG, "onCreate Activity: " + mAuth.getCurrentUser().getEmail());
        //TODO: comment this out when testing is not needed
        test();
    }

    public void test(){
        Test test = new Test();
        Thread thread = new Thread(test);
        thread.start();
    }
}