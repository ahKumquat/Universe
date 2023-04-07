package com.example.universe;

import static androidx.core.content.ContextCompat.getSystemService;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.app.SearchManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.universe.Models.Chat;
import com.example.universe.Models.Event;
import com.example.universe.Models.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements Login.IloginFragmentAction,
        HomeFragment.IhomeFragmentAction, ChatAdapter.IchatListRecyclerAction,
        ChatManager.IchatManagerFragmentAction, Register.IRegisterFragmentAction,
        Profile.IProfileFragmentAction, Setting.ISettingFragmentAction,
        FragmentCameraController.DisplayTakenPhoto, FragmentDisplayImage.IdisplayImageAction,
        ChatRoom.IchatFragmentButtonAction, FragmentDisplayFile.IdisplayFileAction,
        HomeEventAdapter.IEventListRecyclerAction {
    private String TAG = Util.TAG;
    private Util util;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private String otherUserId;
    private Boolean cameraAllowed;
    private Boolean readAllowed;
    private Boolean writeAllowed;
    private static final int PERMISSIONS_CODE_POSTEVENT = 0x100;
    private static final int PERMISSIONS_CODE_PROFILE = 0x200;
    private static final int PERMISSIONS_CODE_CHATROOM= 0x300;
    private static final int PERMISSIONS_CODE_FILE = 0x400;
    private static final int PERMISSIONS_CODE_HOME = 0x500;
    private boolean takePhotoNotFromGallery;

    private GoogleSignInClient googleSignInClient;


    // For Google sign in
    private final ActivityResultLauncher<Intent> startActivityForResult =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK ) {
                    Task<GoogleSignInAccount> signInAccountTask =
                            GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    if (signInAccountTask.isSuccessful()) {
                        Toast.makeText(this,"Google sign in successful",Toast.LENGTH_SHORT).show();
                        try {
                            GoogleSignInAccount googleSignInAccount = signInAccountTask.getResult(ApiException.class);
                            if (googleSignInAccount != null) {
                                AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
                                mAuth.signInWithCredential(authCredential).addOnCompleteListener(this, task -> {
                                    if (task.isSuccessful()) {
                                        populateScreen();
                                    } else {
                                        Toast.makeText(MainActivity.this, "Authentication Failed :"
                                                + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } catch (ApiException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        takePhotoNotFromGallery = true;

        //For Google sign in
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        //        Asking for permissions in runtime......
        cameraAllowed = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        readAllowed = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        writeAllowed = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        Boolean coarseLocationAllowed = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;


//        if(cameraAllowed && readAllowed && writeAllowed && videoAllowed && locationAllowed && coarseLocationAllowed){
//            Toast.makeText(this, "All permissions granted!", Toast.LENGTH_SHORT).show();
//        }else{
//            requestPermissions(new String[]{
//                    Manifest.permission.CAMERA,
//                    Manifest.permission.READ_EXTERNAL_STORAGE,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                    Manifest.permission.RECORD_AUDIO,
//                    Manifest.permission.ACCESS_FINE_LOCATION,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//
//            }, PERMISSIONS_CODE_HOME);
//        }
        util = Util.getInstance();
//        Log.d(TAG, "onCreate Activity: " + mAuth.getCurrentUser().getEmail());
        //TODO: comment this out when testing is not needed
        //test();
    }



    public void test(){
        Test test = new Test();
        Thread thread = new Thread(test);
        thread.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        populateScreen();
    }

    private void populateScreen() {
        if (util.getCurrentUser() != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerMain, HomeFragment.newInstance(),"FragmentHome")
                    .addToBackStack("home")
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerMain, Login.newInstance(),"FragmentLogin")
                    .addToBackStack("login")
                    .commit();
        }
    }

    @Override
    public void populateMainFragment(FirebaseUser mUser) {
        this.currentUser = mUser;
        populateScreen();
    }

    @Override
    public void populateLoginFragment() {
       getSupportFragmentManager().popBackStack();
    }

    @Override
    public void populateRegisterFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, Register.newInstance(), "FragmentRegister")
                .addToBackStack("register").commit();
    }

    @Override
    public void openChatManager() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, ChatManager.newInstance(), "FragmentChatManager")
                .addToBackStack("chatmanager").commit();
    }

    @Override
    public void openProfile(User user) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, Profile.newInstance(user), "FragmentProfile")
                .addToBackStack("profile").commit();
    }

    @Override
    public void openPost() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, PostFragment.newInstance(), "FragmentPost")
                .addToBackStack("post").commit();
    }

    @Override
    public void logOut() {
        mAuth.signOut();
        populateScreen();
    }

    @Override
    public void chatClickedFromRecyclerView(Chat chat) {
        util.readChat(chat.getOtherUserId(), new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                startChatPage(chat.getOtherUserId());
            }
        }, Util.DEFAULT_F_LISTENER);
    }

    @Override
    public void startChatPage(String otherUserId) {
        Bundle bundle = new Bundle();
        this.otherUserId = otherUserId;
        bundle.putString("otherUserId", otherUserId);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, ChatRoom.newInstance().getClass(),
                        bundle,"chatFragment")
                .addToBackStack("chatroom") //TODO
                .commit();
        Log.d(TAG, "startChatPage: success");
    }

    public void signWithGoogle() {
        Intent intent = googleSignInClient.getSignInIntent();
        startActivityForResult.launch(intent);
    }

    @Override
    public void populateHomeFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("FragmentHome");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, Objects.requireNonNull(fragment))
                .addToBackStack("home").commit();
    }

    @Override
    public void populateSettingFragment(User user) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, Setting.newInstance(user), "FragmentSetting")
                .addToBackStack("settings").commit();
    }

    @Override
    public void populateProfileFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("FragmentProfile");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, Objects.requireNonNull(fragment))
                .addToBackStack("profile").commit();
    }

    @Override
    public void onTakePhoto(Uri imageUri) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, FragmentDisplayImage.newInstance(imageUri),"displayFragment")
                .addToBackStack("displayImage").commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>2 && requestCode==PERMISSIONS_CODE_POSTEVENT){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerMain, FragmentCameraController.newInstance(), "cameraFragment")
                    .addToBackStack("event").commit();
            Log.d(TAG, "onRequestPermissionsResult: permission granted + open camera from post event");
        }else if (grantResults.length>2 && requestCode==PERMISSIONS_CODE_PROFILE){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerMain, FragmentCameraController.newInstance(), "cameraFragment")
                    .addToBackStack("profile").commit();
            Log.d(TAG, "onRequestPermissionsResult: permission granted + open camera from profile");
        } else if (grantResults.length>2 && requestCode==PERMISSIONS_CODE_CHATROOM){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerMain, FragmentCameraController.newInstance(), "cameraFragment")
                    .addToBackStack("chatroom").commit();
            Log.d(TAG, "onRequestPermissionsResult: permission granted + open camera from chatroom");
        } else if (requestCode == PERMISSIONS_CODE_FILE && grantResults.length > 0 && grantResults[0]
                == PackageManager.PERMISSION_GRANTED) {
            selectFile();
        } else{
            Toast.makeText(this, "You must allow Camera and Storage permissions!", Toast.LENGTH_LONG).show();
        }
    }

    //Retrieving an image from gallery....
    ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode()==RESULT_OK){
                        Intent data = result.getData();
                        Uri selectedImageUri = data.getData();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.containerMain, FragmentDisplayImage.newInstance(selectedImageUri),"displayFragment")
                                .addToBackStack("displayImage").commit();
                    }
                }
            }
    );

    @Override
    public void onOpenGalleryPressed() {
        takePhotoNotFromGallery = false;
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
        galleryLauncher.launch(intent);
    }

    @Override
    public void onRetakePressed() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, FragmentCameraController.newInstance(), "cameraFragment")
                .addToBackStack("camera").commit();
    }

    @Override
    public void onUploadButtonPressed(Uri imageUri, ProgressBar progressBar) {
//        ProgressBar.......
        progressBar.setVisibility(View.VISIBLE);
//        Upload an image from local file....
        StorageReference storageReference = util.getStorage().getReference().child("images/"+imageUri.getLastPathSegment());
        UploadTask uploadImage = storageReference.putFile(imageUri);
        uploadImage.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Upload Failed! Try again!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(MainActivity.this, "Upload successful!", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        Log.d(TAG, "onProgress: "+progress);
                        progressBar.setProgress((int) progress);
                    }
                });

        Task<Uri> urlTask = uploadImage.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                // Continue with the task to get the download URL
                return storageReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();

                    int count = getSupportFragmentManager().getBackStackEntryCount();
//                    Log.d(TAG, "count of current fragments: " + count);
//                    Log.d(TAG, "current fragments: " + getSupportFragmentManager().getFragments().toString());
                    String name = getSupportFragmentManager().getBackStackEntryAt(count - 3).getName();
                    Log.d(TAG, "last fragment name: " + getSupportFragmentManager().getBackStackEntryAt(count - 1).getName());
                    Log.d(TAG, "second last fragment name: " + name);
                    Log.d(TAG, "third last fragment name: " + getSupportFragmentManager().getBackStackEntryAt(count - 3).getName());
                    if (name.equals("event")) {
                        //TODO: implement going back to the posting event page
//                        getSupportFragmentManager().beginTransaction()
//                                .replace(R.id.containerMain, FragmentRegister.newInstance(user, downloadUri),"registerFragment")
//                                .commit();
                    } else if (name.equals("profile")) {
                        //TODO: implement going back to profile page with updated avatar
//                        db.collection("users").document(currentUser.getEmail())
//                                .update("profilePhotoUri", downloadUri);
//                        getSupportFragmentManager().beginTransaction()
//                                .replace(R.id.containerMain, FragmentProfile.newInstance(downloadUri),"profileFragment")
//                                .commit();
                    } else if (name.equals("chatroom")) {
                        ChatRoom fragment = (ChatRoom) getSupportFragmentManager().findFragmentByTag("chatFragment");
                        fragment.sendImage(downloadUri);
                        if (takePhotoNotFromGallery) {
                            getSupportFragmentManager().popBackStack();
                            getSupportFragmentManager().popBackStack();
                        } else {
                            getSupportFragmentManager().popBackStack();
                            getSupportFragmentManager().popBackStack();
                            getSupportFragmentManager().popBackStack();
                        }
                        takePhotoNotFromGallery = true;
                    } else {
                        Log.d(TAG, "did not define back method for fragment: " + name);
                        getSupportFragmentManager().popBackStack();
                    }

                } else {
                    Log.d(TAG, "Error getting download Url");
                }
            }
        });
    }


    @Override
    public void sendImage() {
        if(cameraAllowed && readAllowed && writeAllowed){
            Toast.makeText(this, "All permissions granted!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "setProfilePhoto: all permissions granted");
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerMain, FragmentCameraController.newInstance(), "cameraFragment")
                    .addToBackStack("chatroom").commit();
        } else{
            requestPermissions(new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, PERMISSIONS_CODE_CHATROOM);
            Log.d(TAG, "send image: asking for permission");
        }
    }

    //Retrieving a file....
    ActivityResultLauncher<Intent> fileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Intent data = result.getData();
                    if (data != null) {
                        Uri fileUri = data.getData();
                        String filePath = fileUri.getPath();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.containerMain, FragmentDisplayFile.newInstance(fileUri, filePath),"displayFileFragment")
                                .addToBackStack("filePreview").commit();
                    }

                }
            }
    );

    @Override
    public void sendFile() {
        if (ActivityCompat.checkSelfPermission(
                MainActivity.this,
                Manifest.permission
                        .READ_EXTERNAL_STORAGE)
                != PackageManager
                .PERMISSION_GRANTED) {
            // When permission is not granted
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[] {Manifest.permission
                                    .READ_EXTERNAL_STORAGE },
                    PERMISSIONS_CODE_FILE);
        }
        else {
            selectFile();
        }
    }


    private void selectFile()
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/*");
        String[] mimeTypes = {"application/pdf", "application/docx"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        fileLauncher.launch(intent);

//        Intent intent = new Intent(Intent.ACTION_PICK);
//        intent.setType("application/*");
//        String[] mimeTypes = {"application/pdf", "application/docx"};
//        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
//        fileLauncher.launch(intent);

//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setType("application/pdf");
//        resultLauncher.launch(intent);
    }

    @Override
    public void onReselectPressed() {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onUploadFileButtonPressed(Uri fileUri, ProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);
        StorageReference storageReference = util.getStorage().getReference().child("files/"+fileUri.getLastPathSegment());
        UploadTask uploadFile = storageReference.putFile(fileUri);
        uploadFile.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Upload Failed! Try again!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(MainActivity.this, "Upload successful! Check Firestore", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        Log.d(TAG, "onProgress: "+progress);
                        progressBar.setProgress((int) progress);
                    }
                });

        Task<Uri> urlTask = uploadFile.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                // Continue with the task to get the download URL
                return storageReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                        ChatRoom fragment = (ChatRoom) getSupportFragmentManager().findFragmentByTag("chatFragment");
                        fragment.sendFile(downloadUri);
                        getSupportFragmentManager().popBackStack();
                        getSupportFragmentManager().popBackStack();
                    } else {
                        Log.d(TAG, "error sending file");
                    }
            }
        });
    }

    @Override
    public void eventClickedFromRecyclerView(Event event) {
      getSupportFragmentManager().beginTransaction()
              .replace(R.id.containerMain, EventFragment.newInstance(event))
              .addToBackStack("FragmentHome").commit();
    }
}