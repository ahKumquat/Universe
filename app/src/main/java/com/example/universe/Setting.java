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
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.universe.Models.User;

public class Setting extends Fragment {

    private ImageButton imageButtonBack;
    private static final String ARG_USER = "user";
    private User user;

    private ImageView imageViewEdit;

    private EditText editTextName;

    private EditText editTextEmail;

    private EditText editTextPassword;

    private EditText editTextAbout;

    private Button buttonSave;
    private ImageButton imageButtonLogOut;

    private ISettingFragmentAction mListener;

    private Util util;
    private ImageView imageViewEditAvatar;


    public Setting() {
        // Required empty public constructor
    }

    public static Setting newInstance(User user) {
        Setting fragment = new Setting();
        Bundle args = new Bundle();
        args.putSerializable(ARG_USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = (User) getArguments().getSerializable(ARG_USER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        imageButtonBack = view.findViewById(R.id.setting_imagebutton_backbutton);
        imageViewEdit = view.findViewById(R.id.setting_imageview_edit);
        editTextName = view.findViewById(R.id.setting_editText_name);
        editTextAbout = view.findViewById(R.id.setting_editText_about);
        editTextEmail = view.findViewById(R.id.setting_editText_email);
        editTextPassword = view.findViewById(R.id.setting_editText_password);
        buttonSave = view.findViewById(R.id.setting_button_save);
        imageButtonLogOut = view.findViewById(R.id.setting_imageButton_logout);
        imageViewEditAvatar = view.findViewById(R.id.setting_imageView_editAvatar);

        imageButtonBack.setOnClickListener(v -> mListener.populateProfileFragment());
        imageButtonLogOut.setOnClickListener(v -> mListener.logOut());
        imageViewEditAvatar.setOnClickListener(v -> mListener.setAvatar());

        editTextName.setText(user.getUserName());
        editTextEmail.setText(user.getEmail());
        editTextAbout.setText(user.getAbout());

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        imageViewEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ISettingFragmentAction){
            this.mListener = (ISettingFragmentAction) context;
        }else{
            throw new RuntimeException(context + "must implement Setting Fragment Action");
        }
    }

    public interface ISettingFragmentAction {
        void populateProfileFragment();
        void logOut();
        void setAvatar();
    }
}