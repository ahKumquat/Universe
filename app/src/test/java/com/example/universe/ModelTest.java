package com.example.universe;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

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
@RunWith(MockitoJUnitRunner.class)
public class ModelTest {
    @Mock
    private static FirebaseAuth mAuth = Mockito.mock(FirebaseAuth.class);
    @Mock
    private static FirebaseFirestore db = Mockito.mock(FirebaseFirestore.class);
    @Mock
    private static FirebaseStorage storage = Mockito.mock(FirebaseStorage.class);
    private static String TAG = Util.TAG;
    private static Util util;
    @BeforeClass
    public static void beforeClass(){
        System.out.println(mAuth);
        util = Util.getInstance(mAuth, db, storage);
    }

    @Test
    public void createUserWithEmailAndPassword(){
//        System.out.println("test print out");
//        util.createUserWithEmailAndPassword("abcd1234@gmai.com", "abcd1234", "Tester");
    }

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }
}