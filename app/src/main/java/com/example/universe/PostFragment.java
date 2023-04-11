package com.example.universe;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.universe.Models.Event;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;


public class PostFragment extends Fragment {
    private Spinner spinnerUnit;
    private EditText editTextTitle;
    private EditText editTextDuration;
    private TextView textViewLocation;
    private EditText editTextDescription;
    private EditText editTextCapacity;
    private TextView textViewDate;
    private TextView textViewTime;
    private ImageButton eventPic;
    private String postPicPath;

    private IPostFragmentAction mListener;
    private Event event;
    private Button buttonSelectDate;
    private Button buttonSelectTime;
    private Button buttonSave;
    private Button buttonPost;
    private String title;
    private Timestamp time;
    private double duration;
    private String durationUnit;
    private String address;
    private GeoPoint geoPoint;
    private int capacity;
    private String description;
    private double lat;
    private double lon;
    private String uid;

    private String selectedTime;
    private String selectedDate;

    private Event draft;

    private Util util;

    private OnBackPressedCallback callback;

    private ArrayAdapter<String> adapterUnit;
    private int mYear, mMonth, mDay, mHour, mMinute;

    private final static String ARG_EVENT = "event";


    public PostFragment() {
        // Required empty public constructor
    }


    public static PostFragment newInstance(Event draft) {
        PostFragment fragment = new PostFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_EVENT, draft);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            draft = (Event) getArguments().getSerializable(ARG_EVENT);
        }
        callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                mListener.backToPrevious();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
        util = Util.getInstance();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_post, container, false);
        spinnerUnit = view.findViewById(R.id.post_spinner_timeUnit);
        editTextTitle = view.findViewById(R.id.post_editText_Title);
        editTextCapacity = view.findViewById(R.id.post_editText_capacity);
        editTextDescription = view.findViewById(R.id.post_editText_description);
        textViewLocation = view.findViewById(R.id.post_textview_location);
        editTextDuration = view.findViewById(R.id.post_editText_duration);
        textViewDate = view.findViewById(R.id.post_textView_dateDisplay);
        textViewTime = view.findViewById(R.id.post_textView_timeDisplay);
        eventPic = view.findViewById(R.id.post_imageButton_eventPic);
        buttonSelectDate = view.findViewById(R.id.post_button_dateSelector);
        buttonSelectTime = view.findViewById(R.id.post_button_timeSelector);
        buttonSave = view.findViewById(R.id.post_button_save);
        buttonPost = view.findViewById(R.id.post_button_postNow);

        eventPic.setOnClickListener(v -> mListener.setEventPic());

        buttonSelectDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            mYear = calendar.get(Calendar.YEAR);
            mMonth = calendar.get(Calendar.MONTH);
            mDay = calendar.get(Calendar.DAY_OF_MONTH);
            @SuppressLint("SetTextI18n")
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view12, year, monthOfYear, dayOfMonth) -> {
                        textViewDate.setText((monthOfYear + 1) + "-" + dayOfMonth + "-" + year);
                        selectedDate = textViewDate.getText().toString();
                        },
                    mYear, mMonth, mDay);
            datePickerDialog.show();
        });

        buttonSelectTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            mHour = calendar.get(Calendar.HOUR_OF_DAY);
            mMinute = calendar.get(Calendar.MINUTE);

            @SuppressLint("SetTextI18n")
            TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(),
                    (view13, hourOfDay, minute) -> {
                        textViewTime.setText(hourOfDay + ":" + minute);
                        selectedTime = textViewTime.getText().toString();
                    }

                    , mHour, mMinute, true);
            timePickerDialog.show();
        });

        textViewLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.inputAddress();
            }
        });

        buttonSave.setOnClickListener(view1 -> {
                if (editTextTitle.getText().toString().equals("")) {
                    Toast.makeText(requireContext(), "Must input a title", Toast.LENGTH_SHORT).show();
                } else if (textViewDate.getText().toString().equals("")) {
                    Toast.makeText(requireContext(), "Must select a date", Toast.LENGTH_SHORT).show();
                } else if (textViewTime.getText().toString().equals("")) {
                    Toast.makeText(requireContext(), "Must select a time", Toast.LENGTH_SHORT).show();
                } else if (editTextDuration.getText().toString().equals("")) {
                    Toast.makeText(requireContext(), "Must input a duration", Toast.LENGTH_SHORT).show();
                } else if (editTextCapacity.getText().toString().equals("")) {
                    Toast.makeText(requireContext(), "Must give a capacity", Toast.LENGTH_SHORT).show();
                } else if (editTextDescription.getText().toString().equals("")) {
                    Toast.makeText(requireContext(), "Must input a description", Toast.LENGTH_SHORT).show();
                } else if (postPicPath == null) {
                    Toast.makeText(requireContext(), "Must select a post picture", Toast.LENGTH_SHORT).show();
                } else {
                    title = editTextTitle.getText().toString();
                    @SuppressLint("SimpleDateFormat")
                    DateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm");
                    String datetime = textViewDate.getText().toString() + " " + textViewTime.getText().toString();
                    try {
                        Date date = (Date) Objects.requireNonNull(formatter.parse(datetime));
                        time = new Timestamp(date);
                        duration = Double.parseDouble(editTextDuration.getText().toString());
                        durationUnit = spinnerUnit.getSelectedItem().toString();
                        address = textViewLocation.getText().toString();
                        geoPoint = new GeoPoint(lat, lon);
                        capacity = Integer.parseInt(editTextCapacity.getText().toString());
                        description = editTextDescription.getText().toString();
                        uid = time.toDate() + " " + geoPoint + " " + capacity + " " + duration;
                        event = new Event(uid, util.getCurrentUser(), title, time,
                                duration, durationUnit, address, geoPoint, capacity, description, postPicPath);
                        mListener.saveEvent(event);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }

        });

        buttonPost.setOnClickListener(view14 -> {
            if (editTextTitle.getText().toString().equals("")) {
                Toast.makeText(requireContext(), "Must input a title", Toast.LENGTH_SHORT).show();
            } else if (textViewDate.getText().toString().equals("")) {
                Toast.makeText(requireContext(), "Must select a date", Toast.LENGTH_SHORT).show();
            } else if (textViewTime.getText().toString().equals("")) {
                Toast.makeText(requireContext(), "Must select a time", Toast.LENGTH_SHORT).show();
            } else if (editTextDuration.getText().toString().equals("")) {
                Toast.makeText(requireContext(), "Must input a duration", Toast.LENGTH_SHORT).show();
            } else if (editTextCapacity.getText().toString().equals("")) {
                Toast.makeText(requireContext(), "Must give a capacity", Toast.LENGTH_SHORT).show();
            } else if (editTextDescription.getText().toString().equals("")) {
                Toast.makeText(requireContext(), "Must input a description", Toast.LENGTH_SHORT).show();
            } else if (postPicPath == null) {
                Toast.makeText(requireContext(), "Must select a post picture", Toast.LENGTH_SHORT).show();
            } else {
                title = editTextTitle.getText().toString();
                @SuppressLint("SimpleDateFormat")
                DateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm");
                String datetime = textViewDate.getText().toString() + " " + textViewTime.getText().toString();
                try {
                    Date date = (Date) Objects.requireNonNull(formatter.parse(datetime));
                    time = new Timestamp(date);
                    duration = Double.parseDouble(editTextDuration.getText().toString());
                    durationUnit = spinnerUnit.getSelectedItem().toString();
                    address = textViewLocation.getText().toString();
                    geoPoint = new GeoPoint(lat, lon);
                    capacity = Integer.parseInt(editTextCapacity.getText().toString());
                    description = editTextDescription.getText().toString();
                    uid = time.toDate() + " " + geoPoint + " " + capacity + " " + duration;
                    event = new Event(uid, util.getCurrentUser(), title, time,
                            duration, durationUnit, address, geoPoint, capacity, description, postPicPath);
                    mListener.postEvent(event);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        if (postPicPath != null) {
            util.getDownloadUrlFromPath(postPicPath, uri -> Glide.with(requireContext())
                    .load(uri).override(350,200).into(eventPic), Util.DEFAULT_F_LISTENER);

        }


        String[] units = {"Hour","Min","Second"};
        adapterUnit = new ArrayAdapter<>(requireActivity(),
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, units);
        spinnerUnit.setAdapter(adapterUnit);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getArguments() != null) {
            String address = getArguments().getString("Place");
            LatLng latLng = getArguments().getParcelable("LatLng");
            textViewLocation.setText((address != null)? address:textViewLocation.getText().toString());
            lon = (latLng != null) ? latLng.longitude:lon;
            lat = (latLng != null) ? latLng.latitude:lat;
            textViewDate.setText((selectedDate != null)? selectedDate:"");
            textViewTime.setText((selectedTime != null)? selectedTime:"");

            if (draft != null) {
                try {
                    restoreFromDraft(draft);
                    draft = null;
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void restoreFromDraft(Event event) throws ParseException {
        editTextDescription.setText(event.getDescription());
        editTextCapacity.setText(String.valueOf(event.getCapacity()));
        editTextTitle.setText(event.getTitle());
        editTextDuration.setText(String.valueOf(event.getDuration()));
        String[] dateTime = event.getTime().toDate().toString().split(" ");
        String dateSaved = dateTime[1] + "-" + dateTime[2] + "-" + dateTime[5];
        String timeSaved = dateTime[3];

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdfToSave = new SimpleDateFormat("MMM-dd-yyyy");

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdfRe = new SimpleDateFormat("MM-dd-yyyy");

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdfToSaveTime = new SimpleDateFormat("hh:mm:ss");

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdfReTime = new SimpleDateFormat("hh:mm");


        String dateToRe = sdfRe.format(Objects.requireNonNull(sdfToSave.parse(dateSaved)));

        String timeToRe = sdfReTime.format(Objects.requireNonNull(sdfToSaveTime.parse(timeSaved)));

        textViewTime.setText(timeToRe);

        selectedDate = dateToRe;

        selectedTime = timeToRe;

        textViewDate.setText(dateToRe);

        textViewLocation.setText(event.getAddress());

        if (event.getImagePath() != null) {
            util.getDownloadUrlFromPath(event.getImagePath(),
                    uri -> {
                        Glide.with(requireContext())
                            .load(uri)
                            .override(300,200)
                            .into(eventPic);
                        postPicPath = event.getImagePath();
                    }, Util.DEFAULT_F_LISTENER);
        }

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

    public void setPostPicPath(String path) {
        this.postPicPath = path;
    }

    public interface IPostFragmentAction {
        void setEventPic();
        void backToPrevious();
        void saveEvent(Event event);
        void postEvent(Event event);
        void inputAddress();
    }
}