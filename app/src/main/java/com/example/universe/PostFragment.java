package com.example.universe;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Arrays;
import java.util.stream.Stream;


public class PostFragment extends Fragment {
    private Spinner spinnerHour;
    private Spinner spinnerMin;
    private Spinner spinnerAMPM;
    private Spinner spinnerUnit;
    private EditText editTextTitle;
    private EditText editTextDate;
    private EditText editTextDuration;
    private EditText editTextLocation;
    private EditText editTextDescription;
    private ImageButton eventPic;
    private IPostFragmentAction mListener;

    private ArrayAdapter<String> adapterHour;
    private ArrayAdapter<String> adapterMin;
    private ArrayAdapter<String> adapterAMPM;
    private ArrayAdapter<String> adapterUnit;


    public PostFragment() {
        // Required empty public constructor
    }


    public static PostFragment newInstance() {
        PostFragment fragment = new PostFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_post, container, false);
        spinnerAMPM = view.findViewById(R.id.post_spinner_timeAMPM);
        spinnerUnit = view.findViewById(R.id.post_spinner_timeUnit);
        spinnerHour = view.findViewById(R.id.post_spinner_timeHour);
        spinnerMin = view.findViewById(R.id.post_spinner_timeMinute);
        editTextTitle = view.findViewById(R.id.post_editText_Title);
        editTextDate = view.findViewById(R.id.post_editText_date);
        editTextDescription = view.findViewById(R.id.post_editText_description);
        editTextLocation = view.findViewById(R.id.post_editText_location);
        editTextDuration = view.findViewById(R.id.post_editText_duration);
        eventPic = view.findViewById(R.id.post_imageButton_eventPic);
        eventPic.setOnClickListener(v -> mListener.setEventPic());

        String[] ampm = {"AM","PM"};
        adapterAMPM = new ArrayAdapter<>(requireActivity(),
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, ampm);
        spinnerAMPM.setAdapter(adapterAMPM);

        String[] units = {"Hour","Min","Second"};
        adapterUnit = new ArrayAdapter<>(requireActivity(),
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, units);
        spinnerUnit.setAdapter(adapterUnit);

        String[] hours = Stream.iterate(0, i -> i + 1).limit(13).map(String::valueOf).toArray(String[]::new);
        adapterHour = new ArrayAdapter<>(requireActivity(),
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, hours);
        spinnerHour.setAdapter(adapterHour);

        String[] mins = Stream.iterate(0, i -> i + 1).limit(60).map(String::valueOf).toArray(String[]::new);
        adapterMin = new ArrayAdapter<>(requireActivity(),
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, mins);
        spinnerMin.setAdapter(adapterMin);

        spinnerAMPM.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Setting.ISettingFragmentAction){
            this.mListener = (IPostFragmentAction) context;
        }else{
            throw new RuntimeException(context + "must implement Setting Fragment Action");
        }
    }

    public interface IPostFragmentAction {
        void setEventPic();
    }
}