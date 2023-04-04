package com.example.universe;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

import android.util.Log;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ModelTest {
    private String TAG = Util.TAG;
    private Util util;
    @BeforeClass
    public void beforeClass(){
        util = Util.getInstance();
    }

    @Test
    public void createUserWithEmailAndPassword(){
        AuthResult result = util.createUserWithEmailAndPassword("abcd1234@gmai.com", "abcd1234");
        Log.d(TAG, "createUserWithEmailAndPassword: " + result);
        if (result.getUser() != null){
            Log.d(TAG, "createUserWithEmailAndPassword: ");
        }
    }

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }
}