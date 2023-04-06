package com.example.universe;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

public class Setting extends Fragment {

    private ImageButton imageButtonBack;

    private ImageView imageViewEdit;

    private Button buttonSave;
    private ImageButton imageButtonLogOut;

    private ISettingFragmentAction mListener;


    public Setting() {
        // Required empty public constructor
    }

    public static Setting newInstance() {
        Setting fragment = new Setting();
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
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        imageButtonBack = view.findViewById(R.id.setting_imagebutton_backbutton);
        imageViewEdit = view.findViewById(R.id.setting_imageview_edit);
        buttonSave = view.findViewById(R.id.setting_button_save);
        imageButtonLogOut = view.findViewById(R.id.setting_imageButton_logout);

        imageButtonBack.setOnClickListener(v -> mListener.populateProfileFragment());
        imageButtonLogOut.setOnClickListener(v -> mListener.logOut());

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
            throw new RuntimeException(context + "must implement profile Fragment Action");
        }
    }

    public interface ISettingFragmentAction {
        void populateProfileFragment();
        void logOut();
    }
}