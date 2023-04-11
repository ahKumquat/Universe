package com.example.universe;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity implements Login.IloginFragmentAction,
        HomeFragment.IhomeFragmentAction, ChatAdapter.IchatListRecyclerAction,
        ChatManager.IchatManagerFragmentAction, Register.IRegisterFragmentAction,
        Profile.IProfileFragmentAction, Setting.ISettingFragmentAction,
        FragmentCameraController.DisplayTakenPhoto, FragmentDisplayImage.IdisplayImageAction,
        ChatRoom.IchatFragmentButtonAction, FragmentDisplayFile.IdisplayFileAction,
        HomeEventAdapter.IEventListRecyclerAction, PostFragment.IPostFragmentAction,
        FollowerAdapter.IFollowerListRecyclerAction, SearchFragment.ISearchFragmentAction,
        EventFragment.IEventFragmentAction, Followers.IFollowerFragmentAction, ParticipantAdapter.IEventListRecyclerAction {
    private String TAG = Util.TAG;
    private Util util;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private String otherUserId;
    private Boolean cameraAllowed;
    private Boolean readAllowed;
    private Boolean writeAllowed;
    private static final int PERMISSIONS_CODE_POSTEVENT = 0x100;
    private static final int PERMISSIONS_CODE_SETTING = 0x200;
    private static final int PERMISSIONS_CODE_CHATROOM= 0x300;
    private static final int PERMISSIONS_CODE_FILE = 0x400;
    private static final int PERMISSIONS_CODE_HOME = 0x500;

    private static final String HOME_FRAGMENT = "FragmentHome";
    private static final String PROFILE_FRAGMENT = "FragmentProfile";
    private static final String OTHER_PROFILE_FRAGMENT = "FragmentOtherProfile";
    private static final String LOGIN_FRAGMENT = "FragmentLogin";
    private static final String CHAT_FRAGMENT = "FragmentChat";
    private static final String EVENT_FRAGMENT = "FragmentEvent";
    private static final String CHAT_MANAGER_FRAGMENT = "FragmentChatManager";
    private static final String FOLLOWERS_FRAGMENT = "FragmentFollowers";
    private static final String REGISTER_FRAGMENT = "FragmentRegister";
    private static final String SETTING_FRAGMENT = "FragmentSetting";
    private static final String SEARCH_FRAGMENT = "FragmentSearch";
    private static final String CAMERA_FRAGMENT = "FragmentCamera";
    private static final String DISPLAY_FILE_FRAGMENT = "FragmentDisplayFile";
    private static final String DISPLAY_IMAGE_FRAGMENT = "FragmentDisplayImage";
    private static final String POST_FRAGMENT = "FragmentPost";


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
//        takePhotoNotFromGallery = true;

        //For Google sign in
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        //        Asking for permissions in runtime......
        cameraAllowed = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        readAllowed = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        writeAllowed = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        Boolean locationAllowed = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        Boolean coarseLocationAllowed = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;


        if(cameraAllowed && readAllowed && writeAllowed && locationAllowed && coarseLocationAllowed){
            Toast.makeText(this, "All permissions granted!", Toast.LENGTH_SHORT).show();
        }else{
            requestPermissions(new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION

            }, PERMISSIONS_CODE_HOME);
        }
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
        int backStackCount = getSupportFragmentManager().getBackStackEntryCount();
        if (backStackCount < 2) {
            populateScreen();
        }
    }

    private void populateScreen() {
        if (util.getCurrentUser() != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerMain, HomeFragment.newInstance(),HOME_FRAGMENT)
                    .addToBackStack(null)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerMain, Login.newInstance(),LOGIN_FRAGMENT)
                    .addToBackStack(null)
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
                .replace(R.id.containerMain, Register.newInstance(), REGISTER_FRAGMENT)
                .addToBackStack(LOGIN_FRAGMENT).commit();
    }

    @Override
    public void openChatManager() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, ChatManager.newInstance(), CHAT_MANAGER_FRAGMENT)
                .addToBackStack(HOME_FRAGMENT).commit();
    }

    @Override
    public void openProfile(User user) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, Profile.newInstance(user), PROFILE_FRAGMENT)
                .addToBackStack(HOME_FRAGMENT).commit();
    }

    @Override
    public void openPost(Event event) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, PostFragment.newInstance(event), POST_FRAGMENT)
                .addToBackStack(HOME_FRAGMENT).commit();
    }

    @Override
    public void showResult(String query, User user) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, SearchFragment.newInstance(query,user), SEARCH_FRAGMENT)
                .addToBackStack(HOME_FRAGMENT).commit();
    }

    @Override
    public void logOut() {
        mAuth.signOut();
        populateScreen();
    }

    @Override
    public void setAvatar() {
        if(cameraAllowed && readAllowed && writeAllowed){
            //Log.d(TAG, "setAvatar: all permissions granted");
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerMain, FragmentCameraController.newInstance(), CAMERA_FRAGMENT)
                    .addToBackStack(SETTING_FRAGMENT).commit();
        } else{
            requestPermissions(new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, PERMISSIONS_CODE_SETTING);
            //Log.d(TAG, "set avatar: asking for permission");
        }
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
                        bundle,CHAT_FRAGMENT)
                .addToBackStack(CHAT_MANAGER_FRAGMENT)
                .commit();
    }

    public void signWithGoogle() {
        Intent intent = googleSignInClient.getSignInIntent();
        startActivityForResult.launch(intent);
    }

    @Override
    public void backToPrevious() {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void populateHomeFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(HOME_FRAGMENT);
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerMain, Objects.requireNonNull(fragment))
                    .addToBackStack(null).commit();
        } else {
            getSupportFragmentManager()
                    .beginTransaction().replace(R.id.containerMain, HomeFragment.newInstance()).
                    addToBackStack(null).commit();
        }
    }

    @Override
    public void editPost(Event event) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, PostFragment.newInstance(event), POST_FRAGMENT)
                .addToBackStack(EVENT_FRAGMENT).commit();
    }

    @Override
    public void startChatPageFromEvent(String otherUserId) {
        Bundle bundle = new Bundle();
        this.otherUserId = otherUserId;
        bundle.putString("otherUserId", otherUserId);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, ChatRoom.newInstance().getClass(),
                        bundle,CHAT_FRAGMENT)
                .addToBackStack(EVENT_FRAGMENT)
                .commit();
    }

    @Override
    public void openHostProfile(User user) {
        openProfile(user);
    }

    @Override
    public void populateSettingFragment(User user) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, Setting.newInstance(user), SETTING_FRAGMENT)
                .addToBackStack(PROFILE_FRAGMENT).commit();
    }

    @Override
    public void populateFollowerFragment(User user, int tabNum) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, Followers.newInstance(user, tabNum), FOLLOWERS_FRAGMENT)
                .addToBackStack(PROFILE_FRAGMENT).commit();
    }

    @Override
    public void startChatPageFromProfile(String otherUserId) {
        Bundle bundle = new Bundle();
        this.otherUserId = otherUserId;
        bundle.putString("otherUserId", otherUserId);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, ChatRoom.newInstance().getClass(),
                        bundle,CHAT_FRAGMENT)
                .addToBackStack(PROFILE_FRAGMENT)
                .commit();
    }

    @Override
    public void populateProfileFragment() {
        backToPrevious();
    }

    @Override
    public void onTakePhoto(Uri imageUri) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, FragmentDisplayImage.newInstance(imageUri), DISPLAY_IMAGE_FRAGMENT)
                .addToBackStack(null).commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>2 && requestCode==PERMISSIONS_CODE_POSTEVENT){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerMain, FragmentCameraController.newInstance(), CAMERA_FRAGMENT)
                    .addToBackStack(POST_FRAGMENT).commit();

            //Log.d(TAG, "onRequestPermissionsResult: permission granted + open camera from post event");
        }else if (grantResults.length>2 && requestCode==PERMISSIONS_CODE_SETTING){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerMain, FragmentCameraController.newInstance(), CAMERA_FRAGMENT)
                    .addToBackStack(SETTING_FRAGMENT).commit();

            //Log.d(TAG, "onRequestPermissionsResult: permission granted + open camera from profile");
        } else if (grantResults.length>2 && requestCode==PERMISSIONS_CODE_CHATROOM){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerMain, FragmentCameraController.newInstance(), CAMERA_FRAGMENT)
                    .addToBackStack(CHAT_FRAGMENT).commit();

            //Log.d(TAG, "onRequestPermissionsResult: permission granted + open camera from chatroom");
        } else if (requestCode == PERMISSIONS_CODE_FILE && grantResults.length > 0 && grantResults[0]
                == PackageManager.PERMISSION_GRANTED) {
            selectFile();
        } else{
            ///Toast.makeText(this, "You must allow Camera and Storage permissions!", Toast.LENGTH_LONG).show();
        }
    }

    //Retrieving an image from gallery....
    ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getData()!=null){
                    Intent data = result.getData();
                    Uri selectedImageUri = data.getData();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.containerMain,
                                    FragmentDisplayImage.newInstance(selectedImageUri),DISPLAY_IMAGE_FRAGMENT)
                            .addToBackStack(null).commit();
                }
            }
    );

    @Override
    public void onOpenGalleryPressed() {
//        takePhotoNotFromGallery = false;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
        galleryLauncher.launch(intent);
    }

    @Override
    public void onRetakePressed() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, FragmentCameraController.newInstance(),  CAMERA_FRAGMENT)
                .addToBackStack(null).commit();
    }

    @Override
    public void onUploadButtonPressed(Uri imageUri, ProgressBar progressBar) {
//        ProgressBar.......
        progressBar.setVisibility(View.VISIBLE);
//        Upload an image from local file....
        List<String> arrayList = Arrays.stream(imageUri.getPath().split("/")).collect(Collectors.toList());

        StorageReference storageReference = util.getStorage().getReference().child("images/"
                + arrayList.get(arrayList.size() - 1));
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
                        int count = getSupportFragmentManager().getBackStackEntryCount();
                        String name = getSupportFragmentManager().getBackStackEntryAt(count - 2).getName();
                        if (name != null) {
                            switch (name) {
                                case POST_FRAGMENT:
                                    //TODO: implement upload the event pic and save path in Post Fragment
                                    PostFragment p = (PostFragment) getSupportFragmentManager().findFragmentByTag(POST_FRAGMENT);
                                    p.setPostPicPath(storageReference.getPath());
                                    break;
                                case SETTING_FRAGMENT:
                                    Setting s = (Setting) getSupportFragmentManager().findFragmentByTag(SETTING_FRAGMENT);
                                    s.setNewAvatarPath(storageReference.getPath());
                                    break;
                                case CHAT_FRAGMENT:
                                    ChatRoom fragment = (ChatRoom) getSupportFragmentManager().findFragmentByTag(CHAT_FRAGMENT);
                                    fragment.sendImage(storageReference.getPath());
                                    break;
                            }
                            getSupportFragmentManager().popBackStack(getSupportFragmentManager().getBackStackEntryAt(count -3).getName(),0);
                        } else {
                            String otherName = getSupportFragmentManager().getBackStackEntryAt(count - 3).getName();
                            switch (otherName) {
                                case POST_FRAGMENT:
                                    PostFragment p = (PostFragment) getSupportFragmentManager().findFragmentByTag(POST_FRAGMENT);
                                    p.setPostPicPath(storageReference.getPath());
                                    break;
                                case SETTING_FRAGMENT:
                                    Setting s = (Setting) getSupportFragmentManager().findFragmentByTag(SETTING_FRAGMENT);
                                    s.setNewAvatarPath(storageReference.getPath());
                                    break;
                                case CHAT_FRAGMENT:
                                    ChatRoom fragment = (ChatRoom) getSupportFragmentManager().findFragmentByTag(CHAT_FRAGMENT);
                                    fragment.sendImage(storageReference.getPath());
                                    break;
                            }
                            getSupportFragmentManager().popBackStack(otherName,0);
                        }
                    }
                })
                .addOnProgressListener(snapshot -> {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    Log.d(TAG, "onProgress: "+progress);
                    progressBar.setProgress((int) progress);
                });
    }


    @Override
    public void sendImage() {
        if(cameraAllowed && readAllowed && writeAllowed){
            Toast.makeText(this, "All permissions granted!", Toast.LENGTH_SHORT).show();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerMain, FragmentCameraController.newInstance(), CAMERA_FRAGMENT)
                    .addToBackStack(CHAT_FRAGMENT).commit();
        } else{
            requestPermissions(new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, PERMISSIONS_CODE_CHATROOM);
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
                                .replace(R.id.containerMain, FragmentDisplayFile.newInstance(fileUri, filePath), DISPLAY_FILE_FRAGMENT)
                                .addToBackStack(null).commit();
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



    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/*");
        String[] mimeTypes = {"application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        fileLauncher.launch(intent);

    }

    @Override
    public void onReselectPressed() {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onUploadFileButtonPressed(Uri fileUri, ProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);

        //old method may get a name with "/"
        List<String> arrayList = Arrays.stream(fileUri.getPath().split("/")).collect(Collectors.toList());

        StorageReference storageReference = util.getStorage().getReference()
                .child("files/"+ arrayList.get(arrayList.size() - 1));

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
                        Toast.makeText(MainActivity.this, "Upload File successfully! ", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
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
                    ChatRoom fragment = (ChatRoom) getSupportFragmentManager().findFragmentByTag(CHAT_FRAGMENT);
                    fragment.sendFile(downloadUri);
                    backToPrevious();
                    } else {
                        Toast.makeText(MainActivity.this,"Sending file failed!",Toast.LENGTH_SHORT).show();
                    }
            }
        });
    }

    @Override
    public void eventClickedFromRecyclerView(Event event, User user) {
      getSupportFragmentManager().beginTransaction()
              .replace(R.id.containerMain, EventFragment.newInstance(event, user), EVENT_FRAGMENT)
              .addToBackStack(HOME_FRAGMENT).commit();
    }

    @Override
    public void setEventPic() {
        if(cameraAllowed && readAllowed && writeAllowed){
            Toast.makeText(this, "All permissions granted!", Toast.LENGTH_SHORT).show();
            Toast.makeText(MainActivity.this, "All permissions granted",Toast.LENGTH_SHORT).show();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerMain, PostFragment.newInstance(null), POST_FRAGMENT)
                    .addToBackStack(null).commit();
        } else{
            requestPermissions(new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, PERMISSIONS_CODE_POSTEVENT);
        }
    }


    @Override
    public void followerClickedFromRecyclerView(User user) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, Profile.newInstance(user),OTHER_PROFILE_FRAGMENT)
                .addToBackStack(FOLLOWERS_FRAGMENT).commit();
    }

    @Override
    public void saveEvent(Event event) {
        if (event != null) {
            util.saveDraftEvent(event, unused -> {
                Toast.makeText(MainActivity.this,"Event draft saved!",Toast.LENGTH_SHORT).show();
                getSupportFragmentManager().popBackStack();
            }, Util.DEFAULT_F_LISTENER);
        }
    }

    @Override
    public void postEvent(Event event) {
        if (event != null) {
            util.postEvent(event, new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(MainActivity.this,"Event posted!",Toast.LENGTH_SHORT).show();
                    getSupportFragmentManager().popBackStack();
                }
            }, Util.DEFAULT_F_LISTENER);
        }
    }

    @Override
    public void eventClickedFromRecyclerView(User user) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, Profile.newInstance(user),OTHER_PROFILE_FRAGMENT)
                .addToBackStack(EVENT_FRAGMENT).commit();
    }


//    ActivityResultLauncher<Intent> addressLauncher = registerForActivityResult(
//            new ActivityResultContracts.StartActivityForResult(),
//            result -> {
//                if(result.getData() != null){
//                    Intent data = result.getData();
//                    Place place = Autocomplete.getPlaceFromIntent(data);
//                    Bundle args = new Bundle();
//                    args.putString("Place",place.getAddress());
//                    args.putParcelable("LatLng", place.getLatLng());
//                    Objects.requireNonNull(getSupportFragmentManager().findFragmentByTag(POST_FRAGMENT)).setArguments(args);
//                }
//            }
//    );

//    @Override
//    public void inputAddress() {
//        if (!Places.isInitialized()) {
//            Places.initialize(getApplicationContext(), getString(R.string.google_api_key), Locale.US);
//        }
//        List<Place.Field> field = Arrays.asList(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG);
//
//        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, field)
//                .build(this);
//
//        addressLauncher.launch(intent);
//    }
}