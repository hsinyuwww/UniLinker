package edu.northeastern.numad24sp_group4unilink.profile;

import static edu.northeastern.numad24sp_group4unilink.Login.loggedInUser;
import static edu.northeastern.numad24sp_group4unilink.Register.defaultPicUrl;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import edu.northeastern.numad24sp_group4unilink.BaseActivity;

import android.annotation.SuppressLint;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Objects;

import edu.northeastern.numad24sp_group4unilink.Login;
import edu.northeastern.numad24sp_group4unilink.R;
import edu.northeastern.numad24sp_group4unilink.community.Community;
import edu.northeastern.numad24sp_group4unilink.databinding.ActivityProfileBinding;
import edu.northeastern.numad24sp_group4unilink.events.Event;

public class ProfileActivity extends BaseActivity {
    ActivityProfileBinding activityProfileBinding;
    private TextView firstName, lastName, aboutContent, genderContent, levelContent, emailContent, aboutHeader, genderHeader, levelHeader, emailHeader;
    private ImageView profilePic;
    private FirebaseFirestore userDB;
    private boolean isDeleteDialogOpen, isUpdateDialogOpen;
    static public String currentUserDocId, email, userID;
    private ProgressBar progressBar;
    private FirebaseUser currentUser, signInUser;

    private MyEventsAdapter myEventsAdapter;
    private MyCommunitiesAdapter myCommunityAdapter;
    private ArrayList<Event> myEventsList;
    private ArrayList<Community> myCommunitiesList;

    private static final int CAMERA_REQUEST = 100;
    private DocumentSnapshot document;
    private Uri imageuri;
    BottomNavigationView navigationView;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        activityProfileBinding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(activityProfileBinding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        navigationView = findViewById(R.id.bottomNavigationView);
        if(!isLoggedInUser){
            navigationView.setVisibility(View.GONE);
        } else{
        int selectedItemId = getIntent().getIntExtra("NAV_ITEM_ID", R.id.home); // Default to home
        navigationView.setSelectedItemId(selectedItemId);
        }

        userDB = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.progressBarId);
        aboutHeader = findViewById(R.id.aboutHeaderID);
        aboutContent = findViewById(R.id.aboutContentID);
        genderHeader = findViewById(R.id.genderHeaderID);
        genderContent = findViewById(R.id.genderContentID);
        levelHeader = findViewById(R.id.levelHeaderID);
        levelContent = findViewById(R.id.levelContentID);
        emailHeader = findViewById(R.id.emailHeaderID);
        emailContent = findViewById(R.id.emailContentID);
        profilePic = findViewById(R.id.profilePicId);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        email = getIntent().getStringExtra("email");

        if(loggedInUser != null){
            if(Objects.equals(loggedInUser.getEmail(), email)){
                isLoggedInUser = true;
                setUpHamburgerMenu();
            }}

        setUpActivityResultLaunchers();
        initialSetup();


        if(savedInstanceState != null){
            isDeleteDialogOpen = savedInstanceState.getBoolean("isDeleteDialogOpen");
            isUpdateDialogOpen = savedInstanceState.getBoolean("isUpdateDialogOpen");
            if(isDeleteDialogOpen){
                showDeleteDialog();
            }
            if(isUpdateDialogOpen){
                showUpdateProfilePicDialog();
            }
        }

    }

    interface FireStoreCallback{
        void onCallback();
    }
    private void initialSetup() {
        progressBar.setVisibility(View.VISIBLE);

        if(isLoggedInUser && loggedInUser != null){
            email = loggedInUser.getEmail();
        }

        if (email != null) {
            setProfileDataUI(email, new FireStoreCallback(){
                @Override
                public void onCallback() {
                    if(currentUserDocId != null) {
                        runOnUiThread(() -> {
                            myEvents();
                            myCommunities();
                            setupExpandableInfoListeners();
                            progressBar.setVisibility(View.GONE);
                        });
                    } else{
                        startActivity(new Intent(ProfileActivity.this, Login.class));
                    }
                }
            });
        } else{
            startActivity(new Intent(this, Login.class));
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isDeleteDialogOpen", isDeleteDialogOpen);
        outState.putBoolean("isUpdateDialogOpen", isUpdateDialogOpen);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(email == null){
            email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        }
        initialSetup();
    }

    private void setProfileDataUI(String email, FireStoreCallback callback) {
        firstName = findViewById(R.id.firstNameId);
        lastName = findViewById(R.id.lastNameId);

        CollectionReference usersCollection = userDB.collection("users");
        Query query = usersCollection.whereEqualTo("email", email).limit(1);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        document = querySnapshot.getDocuments().get(0);
                        String fName = document.getString("firstName");
                        String lName = document.getString("lastName");
                        String about = document.getString("about");
                        String gender = document.getString("gender");
                        String level = document.getString("level");
                        String email = document.getString("email");
                        String picUrl = document.getString("profilePic");

                        firstName.setText(fName);
                        lastName.setText(lName);
                        aboutContent.setText(about);
                        genderContent.setText(gender);
                        levelContent.setText(level);
                        emailContent.setText(email);
                        Glide.with(ProfileActivity.this).load(picUrl).placeholder(R.drawable.default_profile_pic).error(R.drawable.default_profile_pic).into(profilePic);

                        // stores the current user's document ID for future use
                        currentUserDocId = document.getId();
                        userID = document.getString("userID");

                        if(callback != null){
                            callback.onCallback();
                        }

                    } else {
                        Log.e("Firebase", "No such document exists.");
                    }

                } else {
                    Log.w("Firebase", "Error getting user document", task.getException());
                }
            }

        });

    }


    private void myEvents() {
        TextView noEventsText = findViewById(R.id.noEventsText);

        RecyclerView myEventsRecyclerView = findViewById(R.id.eventsView);
        myEventsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        myEventsList = new ArrayList<>();
        myEventsAdapter = new MyEventsAdapter(this, myEventsList);
        myEventsRecyclerView.setAdapter(myEventsAdapter);

        userDB.collection("posts").whereArrayContains("attendees", userID).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                myEventsList.clear();
                for(QueryDocumentSnapshot document : queryDocumentSnapshots){
                    Event event = document.toObject(Event.class);
                    event.setDocumentId(document.getId());
                    myEventsList.add(event);
                }
                myEventsAdapter.notifyDataSetChanged();

                noEventsText.setVisibility(myEventsList.isEmpty() ? View.VISIBLE : View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Firestore error", "Error getting document", e);
                noEventsText.setVisibility(View.VISIBLE);
            }
        });
    }

    private void myCommunities() {
        TextView noCommunitiesText = findViewById(R.id.noCommunitiesText);

        RecyclerView myCommunityRecyclerView = findViewById(R.id.CommunitiesView);
        myCommunityRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        myCommunitiesList = new ArrayList<>();
        myCommunityAdapter = new MyCommunitiesAdapter(this, myCommunitiesList);
        myCommunityRecyclerView.setAdapter(myCommunityAdapter);

        userDB.collection("communities").whereArrayContains("users", userID).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                myCommunitiesList.clear();
                for(QueryDocumentSnapshot document : queryDocumentSnapshots){
                    Community community = document.toObject(Community.class);
                    myCommunitiesList.add(community);
                }
                myCommunityAdapter.notifyDataSetChanged();

                noCommunitiesText.setVisibility(myCommunitiesList.isEmpty() ? View.VISIBLE : View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Firestore error", "Error getting document", e);
                noCommunitiesText.setVisibility(View.VISIBLE);
            }
        });
    }


    private void setupExpandableInfoListeners() {
        aboutHeader.setOnClickListener(v -> contentVisibilityHelper(aboutContent));
        genderHeader.setOnClickListener(v -> contentVisibilityHelper(genderContent));
        levelHeader.setOnClickListener(v -> contentVisibilityHelper(levelContent));
        emailHeader.setOnClickListener(v -> contentVisibilityHelper(emailContent));
    }
    private void contentVisibilityHelper(TextView content){
        if(content.getVisibility() == View.VISIBLE){
            content.setVisibility(View.GONE);
        } else{
            content.setVisibility(View.VISIBLE);
        }
    }


    private void setUpHamburgerMenu() {
        ImageView hamBurgerBtn = findViewById(R.id.hamBurgerId);
        hamBurgerBtn.setVisibility(View.VISIBLE);

        PopupMenu popupMenu = new PopupMenu(this, hamBurgerBtn);
        popupMenu.getMenuInflater().inflate(R.menu.hamburger_profile_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.editProfile){
                    editProfile();
                    return true;
                } else if (item.getItemId() == R.id.updateProfilePic) {
                    showUpdateProfilePicDialog();
                    return true;
                } else if (item.getItemId() == R.id.deleteProfile) {
                    showDeleteDialog();
                    return true;
                } else{
                    return false;
                }
            }
        });
        hamBurgerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.show();
            }
        });
    }


    private void setUpActivityResultLaunchers() {

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        imageuri = result.getData().getData();
                        uploadImageToFireStore(imageuri);
                        profilePic.setImageURI(imageuri);
                    }
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        if (uri != null) {
                            imageuri = uri;
                            uploadImageToFireStore(uri);
                            profilePic.setImageURI(uri);
                        }
                    }
                });
    }


    private void showUpdateProfilePicDialog() {
        isUpdateDialogOpen = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setTitle("Update Profile Pic");
        builder.setItems(new CharSequence[]{"Camera", "Gallery", "Remove Pic"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0){
                    if(!checkCameraPermission()){
                        requestCameraPermission();
                    } else{
                        pickFromCamera();
                    }
                } else if (which == 1){
                    pickFromGallery();
                }else if (which == 2){
                    removeProfilePic();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isUpdateDialogOpen = false;
                dialog.dismiss();
            }
        });
        builder.create().show();

    }


    // check camera permission
    private Boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
    }

    // request for camera permission if not given
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickFromCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to use this feature", Toast.LENGTH_LONG).show();
            }
        }

    }

    // click photo from camera
    private void pickFromCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "NewProfilePic");
        imageuri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageuri);
        cameraLauncher.launch(cameraIntent);
    }

    // pick image from gallery
    private void pickFromGallery() {
        galleryLauncher.launch("image/*")   ;
    }

    // removes current profile picture
    private void removeProfilePic() {
        DocumentReference userDoc = userDB.collection("users").document(currentUserDocId);
        userDoc.update("profilePic", defaultPicUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Glide.with(ProfileActivity.this).load(defaultPicUrl).into(profilePic);
                    Toast.makeText(ProfileActivity.this, "Profile pic is removed successfully.", Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileActivity.this, "Failed to remove profile pic.", Toast.LENGTH_LONG).show();
            }
        });
    }


    private void editProfile() {
        Intent intent = new Intent(ProfileActivity.this, UpdateProfileActivity.class);
        intent.putExtra("firstName", firstName.getText().toString());
        intent.putExtra("lastName", lastName.getText().toString());
        intent.putExtra("about", aboutContent.getText().toString());
        intent.putExtra("gender", genderContent.getText().toString());
        intent.putExtra("level", levelContent.getText().toString());
        startActivity(intent); // starts new activity
    }

    private void showDeleteDialog(){
        isDeleteDialogOpen = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setTitle("Delete Profile");
        builder.setMessage("Are you sure you want to delete your UniLink account??");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isDeleteDialogOpen = false;
                deleteProfile();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isDeleteDialogOpen = false;
                dialog.dismiss();
            }
        });
        builder.create().show();
    }


    private void deleteProfile() {

        DocumentReference userDoc = userDB.collection("users").document(currentUserDocId);
        userDoc.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                if (currentUser != null){
                    currentUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Log.d("FireAuth", "Account deletion:Success");
                                FirebaseAuth.getInstance().signOut();
                                finish();
                                Intent intent = new Intent(getApplicationContext(), Login.class);
                                startActivity(intent);
                                Toast.makeText(ProfileActivity.this, "User account deleted.", Toast.LENGTH_SHORT).show();
                            } else{
                                Log.w("FireAuth", "Account deletion:Failure" + task.getException());
                                Toast.makeText(ProfileActivity.this, "Failed to delete account.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else{
                    Log.d("Firebase", "Login : deleteProfile");
                    startActivity(new Intent(ProfileActivity.this, Login.class));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("Firebase", "Profile deletion:Failure" + e);
            }
        });
    }

    private void uploadImageToFireStore(Uri uri) {
        if (uri != null) {
            final StorageReference imageRef = FirebaseStorage.getInstance().getReference().child("profile_Pics/" + currentUserDocId + ".jpg");

            progressBar.setVisibility(View.VISIBLE);
            imageRef.putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String picUrl = uri.toString();
                                    DocumentReference userDocRef = FirebaseFirestore.getInstance().collection("users").document(currentUserDocId);
                                    userDocRef.update("profilePic", picUrl).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Glide.with(ProfileActivity.this).load(picUrl).into(profilePic);
                                            Toast.makeText(ProfileActivity.this, "Profile picture is updated.", Toast.LENGTH_LONG).show();
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(ProfileActivity.this, "Failed to update profile picture.", Toast.LENGTH_LONG).show();
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    });
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ProfileActivity.this, "Failed to upload profile picture." + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

}
