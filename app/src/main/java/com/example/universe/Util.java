package com.example.universe;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public class Util {
    public static final String TAG = "test";
    private static Util util;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private FirebaseAuth getAuth(){
        mAuth = FirebaseAuth.getInstance();
        return mAuth;
    }

    private Util(){
    }

    public static Util getInstance(){
        if (util == null){
            util = new Util();
        }
        return util;
    }

    public AuthResult createUserWithEmailAndPassword(String email, String password){
        Task<AuthResult> task = getAuth().createUserWithEmailAndPassword(email, password);
        return task.getResult();
    }
}
