package com.example.universe;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class FragmentDisplayFile extends Fragment {

    protected static final String ARG_FILEURI = "fileUri";
    protected static final String ARG_FILEPATH = "filePath";
    private Uri fileUri;
    private Button buttonRetake;
    private Button buttonUpload;
    private IdisplayFileAction mListener;
    private ProgressBar progressBar;
    private String filePath;
    private TextView textViewFileUri;
    private TextView textViewFilePath;
    public FragmentDisplayFile() {
        // Required empty public constructor
    }

    public static FragmentDisplayFile newInstance(Uri fileUri, String filePath) {
        FragmentDisplayFile fragment = new FragmentDisplayFile();
        Bundle args = new Bundle();
        args.putParcelable(ARG_FILEURI, fileUri);
        args.putString(ARG_FILEPATH, filePath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fileUri = getArguments().getParcelable(ARG_FILEURI);
            filePath = getArguments().getString(ARG_FILEPATH);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_display_file, container, false);
        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);


        buttonRetake = view.findViewById(R.id.buttonRetake);
        buttonUpload = view.findViewById(R.id.buttonUpload);

        textViewFileUri = view.findViewById(R.id.displayFile_textView_fileUrl);
        textViewFileUri.setText(fileUri.toString());
        textViewFilePath = view.findViewById(R.id.displayFile_textView_filePath);
        textViewFilePath.setText(filePath);

        buttonRetake.setOnClickListener(v -> mListener.onReselectPressed());
        buttonUpload.setOnClickListener(v -> mListener.onUploadFileButtonPressed(fileUri,progressBar));
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof FragmentCameraController.DisplayTakenPhoto){
            mListener = (IdisplayFileAction) context;
        }else{
            throw new RuntimeException(context+" must implement RetakePhoto");
        }
    }

    public interface IdisplayFileAction{
        void onReselectPressed();
        void onUploadFileButtonPressed(Uri fileUri, ProgressBar progressBar);
    }
}