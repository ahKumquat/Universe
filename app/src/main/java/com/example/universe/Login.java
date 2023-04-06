package com.example.universe;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;

public class Login extends Fragment {
    private static Util util;
    private String TAG = Util.TAG;
    private EditText editTextUserEmail, editTextPassword;
    private TextView textViewRegister;
    private Button buttonLogin, buttonLoginWithGoogle;
    private String userEmail, password;
    private IloginFragmentAction mListener;



    public Login() {
        // Required empty public constructor
    }

    public static Login newInstance() {
        Login fragment = new Login();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        util = Util.getInstance();
        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        editTextUserEmail = view.findViewById(R.id.login_edittext_username);
        editTextPassword = view.findViewById(R.id.login_edittext_password);
        buttonLogin = view.findViewById(R.id.login_button_login);
        buttonLoginWithGoogle = view.findViewById(R.id.login_button_login_google);
        textViewRegister = view.findViewById(R.id.login_textview_register);



        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Login");
                userEmail = editTextUserEmail.getText().toString().trim();
                password = editTextPassword.getText().toString().trim();
                if(userEmail.equals("")){
                    editTextUserEmail.setError("Must input user name!");
                }
                if(password.equals("")){
                    editTextPassword.setError("Password must not be empty!");
                }
                if(!userEmail.equals("") && !password.equals("")){
                    util.loginUserWithEmailAndPassword(userEmail, password, new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            Toast.makeText(getContext(), "Login Successful!", Toast.LENGTH_SHORT).show();
                            mListener.populateMainFragment(util.getCurrentUser());
                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), "Login Failed!"+e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
//                    //sign in the account
//                    util.getmAuth().signInWithEmailAndPassword(userEmail,password)
//                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
//                                @Override
//                                public void onSuccess(AuthResult authResult) {
//                                    Toast.makeText(getContext(), "Login Successful!", Toast.LENGTH_SHORT).show();
//                                }
//                            })
//                            .addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    Toast.makeText(getContext(), "Login Failed!"+e.getMessage(), Toast.LENGTH_LONG).show();
//                                }
//                            })
//                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                                @Override
//                                public void onComplete(@NonNull Task<AuthResult> task) {
//                                    if(task.isSuccessful()){
//                                        mListener.populateMainFragment(util.getCurrentUser());
//                                    }
//                                }
//                            });
                }
            }
        });

        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.populateRegisterFragment();
            }
        });

        buttonLoginWithGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.signWithGoogle();
            }
        });

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IloginFragmentAction){
            this.mListener = (IloginFragmentAction) context;
        }else{
            throw new RuntimeException(context + "must implement login Fragment Action");
        }
    }

    public interface IloginFragmentAction {
        void populateMainFragment(FirebaseUser mUser);
        void populateRegisterFragment();
        void signWithGoogle();
    }
}