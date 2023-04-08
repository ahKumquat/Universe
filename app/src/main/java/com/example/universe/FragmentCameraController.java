package com.example.universe;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

public class FragmentCameraController extends Fragment implements View.OnClickListener{

    private static Util util;
    private String TAG = Util.TAG;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private PreviewView previewView;
    private CameraSelector cameraSelector;
    private Preview preview;
    private ImageCapture imageCapture;
    private ImageButton imageButtonBack;
    private ProcessCameraProvider cameraProvider = null;
    private int lenseFacing;
    private int lenseFacingBack;
    private int lenseFacingFront;
    private DisplayTakenPhoto mListener;

    private FloatingActionButton buttonTakePhoto;
    private FloatingActionButton buttonSwitchCamera;
    private FloatingActionButton buttonOpenGallery;
    private OnBackPressedCallback callback;


    public FragmentCameraController() {
        // Required empty public constructor
    }

    public static FragmentCameraController newInstance() {
        FragmentCameraController fragment = new FragmentCameraController();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lenseFacingBack = CameraSelector.LENS_FACING_BACK;
        lenseFacingFront = CameraSelector.LENS_FACING_FRONT;
        callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_camera_controller, container, false);
        previewView = view.findViewById(R.id.previewView);

        buttonTakePhoto = view.findViewById(R.id.buttonTakePhoto);
        buttonSwitchCamera = view.findViewById(R.id.buttonSwitchCamera);
        buttonOpenGallery = view.findViewById(R.id.buttonOpenGallery);

        buttonTakePhoto.setOnClickListener(this);
        buttonSwitchCamera.setOnClickListener(this);
        buttonOpenGallery.setOnClickListener(this);

//        default lense facing....
        lenseFacing = lenseFacingBack;

        setUpCamera(lenseFacing);
        return view;
    }

    private void setUpCamera(int lenseFacing) {
        //            binding hardware camera with preview, and imageCapture.......
        cameraProviderFuture = ProcessCameraProvider.getInstance(getContext());
        cameraProviderFuture.addListener(()->{
            preview = new Preview.Builder()
                    .build();
            preview.setSurfaceProvider(previewView.getSurfaceProvider());
            imageCapture = new ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build();
            try {
                cameraProvider = cameraProviderFuture.get();
                cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(lenseFacing)
                        .build();
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle((LifecycleOwner) getContext(),cameraSelector, preview, imageCapture);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(getContext()));

    }

    //  TakePhoto implementation....
    private void takePhoto() {
        long timestamp = System.currentTimeMillis();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timestamp);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE,"image/jpeg");
        contentValues.put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/CameraX-Image");

        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions
                .Builder(getContext().getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues).build();

        imageCapture.takePicture(outputFileOptions,
                ContextCompat.getMainExecutor(getContext()),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
//                        Log.d("demo", "onImageSaved: "+ outputFileResults.getSavedUri());
                        mListener.onTakePhoto(outputFileResults.getSavedUri());
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(getContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.buttonTakePhoto:
                takePhoto();
                break;
            case R.id.buttonOpenGallery:
                mListener.onOpenGalleryPressed();
                break;
            case R.id.buttonSwitchCamera:
                if(lenseFacing==lenseFacingBack){
                    lenseFacing = lenseFacingFront;
                    setUpCamera(lenseFacing);
                }else{
                    lenseFacing = lenseFacingBack;
                    setUpCamera(lenseFacing);
                }
                break;
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof DisplayTakenPhoto){
            mListener = (DisplayTakenPhoto) context;
        }else{
            throw new RuntimeException(context+" must implement DisplayTakenPhoto");
        }
    }

    public interface DisplayTakenPhoto{
        void onTakePhoto(Uri imageUri);
        void onOpenGalleryPressed();
    }
}