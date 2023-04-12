package com.example.universe;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainer;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.universe.Models.Event;
import com.example.universe.Models.GeocodingResult;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class PostFragment extends Fragment {
    private Spinner spinnerUnit;
    private EditText editTextTitle;
    private EditText editTextDuration;
    private EditText textViewLocation;
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

    private AutocompleteSessionToken sessionToken;

    private PlacesClient placesClient;
    private final Handler handler = new Handler();

    private RecyclerView recyclerView;

    private LinearLayoutManager layoutManager;

    private PlacePredictionAdapter adapter = new PlacePredictionAdapter();

    private RequestQueue queue;
    private Gson gson = new GsonBuilder().registerTypeAdapter(LatLng.class, new LatLngAdapter())
            .create();



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
                save();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
        util = Util.getInstance();
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getString(R.string.google_api_key), Locale.US);
        }
        placesClient = Places.createClient(requireContext());
        queue = Volley.newRequestQueue(requireContext());
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
        textViewLocation = view.findViewById(R.id.post_textview_locationInput);
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

        textViewLocation.setOnClickListener(v -> {
            sessionToken = AutocompleteSessionToken.newInstance();
        });
        textViewLocation.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean userChange = Math.abs(count - before) == 1;
                if (userChange) {
                    handler.removeCallbacksAndMessages(null);
                    handler.postDelayed(() -> getPlacePredictions(s.toString()), 300);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}

        });

        textViewLocation.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect rect = new Rect();
            view.getWindowVisibleDisplayFrame(rect);
            int keypadHeight = view.getRootView().getHeight() - rect.bottom;
            if (keypadHeight <= 126) {
                recyclerView.setVisibility(View.INVISIBLE);
                editTextCapacity.setVisibility(View.VISIBLE);
                editTextDescription.setVisibility(View.VISIBLE);
            }
        });

        buttonSave.setOnClickListener(view1 -> save());

        buttonPost.setOnClickListener(view14 -> post());

        if (postPicPath != null) {
            util.getDownloadUrlFromPath(postPicPath, uri -> Glide.with(requireContext())
                    .load(uri).override(350,200).into(eventPic), Util.DEFAULT_F_LISTENER);
        }


        String[] units = {"Hour","Min"};
        adapterUnit = new ArrayAdapter<>(requireActivity(),
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, units);
        spinnerUnit.setAdapter(adapterUnit);


        recyclerView = view.findViewById(R.id.post_recycler_location);
        layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(),
                        layoutManager.getOrientation()));
        adapter.setPlaceClickListener(this::getLocationData);

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

    private void save() {
        title = editTextTitle.getText().toString();
        @SuppressLint("SimpleDateFormat")
        DateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm");
        String datetime = textViewDate.getText().toString() + " " + textViewTime.getText().toString();
        try {
            if (!datetime.equals(" ")) {
                Date date = Objects.requireNonNull(formatter.parse(datetime));
                time = new Timestamp(date);
            }
            if (!editTextDuration.getText().toString().equals("")) {
                duration = Double.parseDouble(editTextDuration.getText().toString());
            }
            durationUnit = spinnerUnit.getSelectedItem().toString();
            address = Objects.requireNonNull(textViewLocation.getText()).toString();
            geoPoint = new GeoPoint(lat, lon);
            if (!editTextCapacity.getText().toString().equals("")) {
                capacity = Integer.parseInt(editTextCapacity.getText().toString());
            }
            description = editTextDescription.getText().toString();

            if (time != null) {
                uid = time.toDate() + " " + geoPoint + " " + capacity + " " + duration;
            } else {
                uid = Timestamp.now().toDate() + " " + geoPoint + " " + capacity + " " + duration;
            }

            event = new Event(uid, util.getCurrentUser(), title, time,
                    duration, durationUnit, address, geoPoint, capacity, description, postPicPath);
            mListener.saveEvent(event);

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void post() {
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
                Date date = Objects.requireNonNull(formatter.parse(datetime));
                time = new Timestamp(date);
                duration = Double.parseDouble(editTextDuration.getText().toString());
                durationUnit = spinnerUnit.getSelectedItem().toString();
                address = Objects.requireNonNull(textViewLocation.getText()).toString();
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
    }

    private void restoreFromDraft(Event event) throws ParseException {
        editTextDescription.setText(event.getDescription());
        if (event.getCapacity() != 0) {
            editTextCapacity.setText(String.valueOf(event.getCapacity()));
        }

        editTextTitle.setText(event.getTitle());

        if (event.getDuration() != 0) {
            editTextDuration.setText(String.valueOf(event.getDuration()));
        }

        if (event.getTime() != null) {
            String[] dateTime = event.getTime().toDate().toString().split(" ");
            String dateSaved = dateTime[1] + "-" + dateTime[2] + "-" + dateTime[5];
            String timeSaved = dateTime[3];
            lat = event.getGeoPoint().getLatitude();
            lon = event.getGeoPoint().getLongitude();

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
        }

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


    private void getLocationData(AutocompletePrediction placePrediction) {
        final String apiKey = getResources().getString(R.string.google_api_key);
        final String url = "https://maps.googleapis.com/maps/api/geocode/json?place_id=%s&key=%s";
        final String requestURL = String.format(url, placePrediction.getPlaceId(), apiKey);
        recyclerView.setVisibility(View.INVISIBLE);
        editTextCapacity.setVisibility(View.VISIBLE);
        editTextDescription.setVisibility(View.VISIBLE);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, requestURL, null,
                response -> {
                    try {
                        JSONArray results = response.getJSONArray("results");
                        if (results.length() == 0) {
                            return;
                        }
                        GeocodingResult result = gson.fromJson(
                                results.getString(0), GeocodingResult.class);

                        address = result.getFormattedAddress();
                        lat = result.getGeometry().getLocation().latitude;
                        lon = result.getGeometry().getLocation().longitude;
                        textViewLocation.setText(address);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {});
                queue.add(request);
    }

    private void getPlacePredictions(String query) {
        recyclerView.setVisibility(View.VISIBLE);
        editTextCapacity.setVisibility(View.INVISIBLE);
        editTextDescription.setVisibility(View.INVISIBLE);

        final FindAutocompletePredictionsRequest newRequest = FindAutocompletePredictionsRequest
                .builder()
                .setSessionToken(sessionToken)
                .setTypeFilter(TypeFilter.ESTABLISHMENT)
                .setQuery(query)
                .setCountries("US")
                .build();

        placesClient.findAutocompletePredictions(newRequest).addOnSuccessListener((response) -> {
            List<AutocompletePrediction> predictions = response.getAutocompletePredictions();
            adapter.setPredictions(predictions);


        }).addOnFailureListener((exception) -> {

        });
    }




    public interface IPostFragmentAction {
        void setEventPic();
        void backToPrevious();
        void saveEvent(Event event);
        void postEvent(Event event);
    }
}