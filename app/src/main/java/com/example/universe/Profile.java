package com.example.universe;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseUser;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Profile#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Profile extends Fragment {

    private IProfileFragmentAction mListener;
    private ImageButton imageButtonBack;
    private ImageButton imageButtonSetting;

    public Profile() {
        // Required empty public constructor
    }


    public static Profile newInstance() {
        Profile fragment = new Profile();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        imageButtonBack = view.findViewById(R.id.profile_imagebutton_backbutton);
        imageButtonSetting = view.findViewById(R.id.profile_imagebutton_setting);

        imageButtonBack.setOnClickListener(v -> mListener.populateHomeFragment());
        imageButtonSetting.setOnClickListener(v -> mListener.populateSettingFragment());
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IProfileFragmentAction){
            this.mListener = (IProfileFragmentAction) context;
        }else{
            throw new RuntimeException(context + "must implement profile Fragment Action");
        }
    }

    public interface IProfileFragmentAction {
        void populateHomeFragment();
        void populateSettingFragment();
    }
}