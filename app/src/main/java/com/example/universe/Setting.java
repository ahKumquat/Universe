package com.example.universe;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.universe.Models.User;
import com.google.firebase.auth.UserInfo;

import java.util.List;
import java.util.stream.Collectors;


public class Setting extends Fragment {

    private ImageButton imageButtonBack;
    private static final String ARG_USER = "user";
    private User user;

    private ImageView imageViewAvatar;

    private EditText editTextName;

    private EditText editTextEmail;

    private EditText editTextPassword;

    private EditText editTextAbout;

    private Button buttonSave;
    private ImageButton imageButtonLogOut;

    private ISettingFragmentAction mListener;

    private Util util;
    private ImageView imageViewEditAvatar;
    private String newAvatarPath;
    private OnBackPressedCallback callback;
    private List<UserInfo> signIn;


    public Setting() {
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
        util = Util.getInstance();
        if (getArguments() != null) {
            user = (User) getArguments().getSerializable(ARG_USER);
        }
        callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
               mListener.backToPrevious();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
        util = Util.getInstance();
        signIn = util.getCurrentUser().getProviderData().stream()
                .filter(userInfo -> userInfo.getProviderId().equals("google.com")).collect(Collectors.toList());
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        imageButtonBack = view.findViewById(R.id.setting_imagebutton_backbutton);
        imageViewAvatar = view.findViewById(R.id.setting_imageview_avatar);

        editTextName = view.findViewById(R.id.setting_editText_name);
        editTextAbout = view.findViewById(R.id.setting_editText_about);
        editTextEmail = view.findViewById(R.id.setting_editText_email);
        editTextPassword = view.findViewById(R.id.setting_editText_password);
        buttonSave = view.findViewById(R.id.setting_button_save);
        imageButtonLogOut = view.findViewById(R.id.setting_imageButton_logout);
        imageViewEditAvatar = view.findViewById(R.id.setting_imageView_editAvatar);

        imageButtonBack.setOnClickListener(v -> mListener.backToPrevious());

        imageButtonLogOut.setOnClickListener(v -> mListener.logOut());
        imageViewEditAvatar.setOnClickListener(v -> mListener.setAvatar());
        imageViewAvatar.setOnClickListener(v -> mListener.setAvatar());

        editTextName.setText(user.getUserName());
        editTextEmail.setText(user.getEmail());
        editTextAbout.setText(user.getAbout());

        if (signIn.size() > 0) {
            editTextPassword.setText("Cannot update your password");
            editTextPassword.setFocusable(false);
            editTextEmail.setFocusable(false);
            editTextEmail.setOnClickListener(v ->
                    Toast.makeText(requireContext(), "Cannot update your password here, " +
                                    "as you using Google sign-in!",
                            Toast.LENGTH_SHORT).show());
            editTextPassword.setOnClickListener(v ->  Toast.makeText(requireContext(),
                    "Cannot update your password here, as you using Google sign-in!",
                    Toast.LENGTH_SHORT).show());
        }

        buttonSave.setOnClickListener(v -> {
           if (editTextName.getText().toString().equals("")) {
               Toast.makeText(requireContext(),"Name cannot be empty", Toast.LENGTH_SHORT).show();
           }  else if (editTextEmail.getText().toString().equals("")) {
               Toast.makeText(requireContext(),"Empty cannot be empty", Toast.LENGTH_SHORT).show();
           } else {
               if (newAvatarPath == null) {
                   newAvatarPath = user.getAvatarPath();
               }
               if (editTextPassword.getText().toString().equals("") || signIn.size() > 0) {
                   util.updateProfile(
                           editTextName.getText().toString(),
                           newAvatarPath,
                           editTextAbout.getText().toString(),
                           editTextEmail.getText().toString(),
                           null,
                           unused -> {
                               user.setAvatarPath(newAvatarPath);
                               user.setUserName(editTextName.getText().toString());
                               user.setEmail(editTextEmail.getText().toString());
                               user.setAbout(editTextAbout.getText().toString());
                               Toast.makeText(requireContext(),
                                       "Update successful!",
                                   Toast.LENGTH_SHORT).show();
                       }, Util.DEFAULT_F_LISTENER);
               } else if (!editTextPassword.getText().toString().equals("") && signIn.size() == 0){
                   util.updateProfile(
                           editTextName.getText().toString(),
                           newAvatarPath,
                           editTextAbout.getText().toString(),
                           editTextEmail.getText().toString(),
                           editTextPassword.getText().toString(),
                           unused -> {
                               user.setAvatarPath(newAvatarPath);
                               user.setUserName(editTextName.getText().toString());
                               user.setEmail(editTextEmail.getText().toString());
                               user.setAbout(editTextAbout.getText().toString());
                               Toast.makeText(requireContext(),
                                       "Update successful!",
                                       Toast.LENGTH_SHORT).show();
                           }, e -> Toast.makeText(requireContext(),
                                   "Update unsuccessful!",
                                   Toast.LENGTH_SHORT).show());
               }
           }
        });



        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (newAvatarPath != null) {
            util.getDownloadUrlFromPath(newAvatarPath, uri -> Glide.with(requireContext())
                    .load(uri)
                    .into(imageViewAvatar), Util.DEFAULT_F_LISTENER);
        } else {
            if (user.getAvatarPath()!=null && !user.getAvatarPath().equals("")) {
                util.getDownloadUrlFromPath(user.getAvatarPath(), uri -> Glide.with(requireContext())
                        .load(uri)
                        .into(imageViewAvatar), Util.DEFAULT_F_LISTENER);
            }
        }
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

    public void setNewAvatarPath(String path) {
        this.newAvatarPath = path;
    }

    public interface ISettingFragmentAction {
        void backToPrevious();
        void logOut();
        void setAvatar();
    }
}