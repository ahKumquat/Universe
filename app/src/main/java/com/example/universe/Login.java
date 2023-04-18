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

public class Login extends Fragment {
    private static Util util;
    private EditText editTextUserEmail, editTextPassword;
    private TextView textViewRegister;
    private Button buttonLogin, buttonLoginWithGoogle;
    private String userEmail, password;
    private IloginFragmentAction mListener;



    public Login() {
    }

    public static Login newInstance() {
        Login fragment = new Login();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        util = Util.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        editTextUserEmail = view.findViewById(R.id.login_edittext_username);
        editTextPassword = view.findViewById(R.id.login_edittext_password);
        buttonLogin = view.findViewById(R.id.login_button_login);
        buttonLoginWithGoogle = view.findViewById(R.id.login_button_login_google);
        textViewRegister = view.findViewById(R.id.login_textview_register);



        buttonLogin.setOnClickListener(view1 -> {
            userEmail = editTextUserEmail.getText().toString().trim();
            password = editTextPassword.getText().toString().trim();
            if(userEmail.equals("")){
                editTextUserEmail.setError("Must input user name!");
            }
            if(password.equals("")){
                editTextPassword.setError("Password must not be empty!");
            }
            if(!userEmail.equals("") && !password.equals("")){
                util.loginUserWithEmailAndPassword(userEmail, password, authResult -> {
                    Toast.makeText(getContext(), "Login Successful!", Toast.LENGTH_SHORT).show();
                    mListener.populateMainFragment(util.getCurrentUser());
                }, e -> Toast.makeText(getContext(), "Login Failed!"+e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });

        textViewRegister.setOnClickListener(v -> mListener.populateRegisterFragment());

        buttonLoginWithGoogle.setOnClickListener(v -> mListener.signWithGoogle());

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