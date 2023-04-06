package com.example.universe;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;

public class Register extends Fragment {
    private Register.IRegisterFragmentAction mListener;
    private TextView textViewLogin;
    private Button buttonRegister;
    private static Util util;
    private String userName,userEmail, password;
    private EditText editTextUserName, editTextUserEmail, editTextPassword;


    public Register() {
        // Required empty public constructor
    }

    public static Register newInstance() {
        Register fragment = new Register();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        util = Util.getInstance();
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        buttonRegister = view.findViewById(R.id.register_button_login);
        textViewLogin = view.findViewById(R.id.register_textview_login);

        editTextUserName = view.findViewById(R.id.register_edittext_username);
        editTextUserEmail = view.findViewById(R.id.register_edittext_email);
        editTextPassword = view.findViewById(R.id.register_edittext_password);

        buttonRegister.setOnClickListener(
                view1 -> {
            userName = editTextUserName.getText().toString().trim();
            userEmail = editTextUserEmail.getText().toString().trim();
            password = editTextPassword.getText().toString().trim();
            if (userName.equals("")) {
                editTextUserName.setError("Must input user name!");
            } else if(userEmail.equals("")){
                editTextUserEmail.setError("Must input email!");
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
                editTextUserEmail.setError("Must input a valid email!");
            } else if(password.equals("")){
                editTextPassword.setError("Password must not be empty!");
            } else {
                util.createUserWithEmailAndPassword(userEmail, password, userName, unused -> {
                    Toast.makeText(getContext(), "Register Successful!", Toast.LENGTH_SHORT).show();
                    mListener.populateMainFragment(util.getCurrentUser());

                }, e -> Toast.makeText(getContext(), "Register Failed!" + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });

        textViewLogin.setOnClickListener(v -> mListener.populateLoginFragment());


        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IRegisterFragmentAction){
            this.mListener = (IRegisterFragmentAction) context;
        }else{
            throw new RuntimeException(context + "must implement register Fragment Action");
        }
    }

    public interface IRegisterFragmentAction {
        void populateMainFragment(FirebaseUser mUser);
        void populateLoginFragment();
    }
}