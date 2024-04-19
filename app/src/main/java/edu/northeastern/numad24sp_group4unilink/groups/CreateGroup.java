package edu.northeastern.numad24sp_group4unilink.groups;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.northeastern.numad24sp_group4unilink.BaseActivity;
import edu.northeastern.numad24sp_group4unilink.R;
import edu.northeastern.numad24sp_group4unilink.databinding.ActivityCreateGroupBinding;

public class CreateGroup extends BaseActivity {

    ActivityCreateGroupBinding activityCreateGroupBinding;

    private EditText editTextCommunityName;
    private ImageView communityImage;
    private Button createCommunityButton;
    private FirebaseFirestore db;
    private CollectionReference communitiesRef;
    private String userId, userEmail;
    private Uri selectedImageUri;
    private StorageReference storageReference;
    ActivityResultLauncher<PickVisualMediaRequest> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.PickVisualMedia(),
            result -> {
                if (result != null) {
                    selectedImageUri = result;
                    communityImage.setImageURI(selectedImageUri);
                }
                else {
                    Log.d("pickImage", "result is null.");
                }
            });

    private static final int REQUEST_IMAGE_PICKER = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        activityCreateGroupBinding = ActivityCreateGroupBinding.inflate(getLayoutInflater());
        setContentView(activityCreateGroupBinding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        editTextCommunityName = findViewById(R.id.commName);
        communityImage = findViewById(R.id.imageView);
        createCommunityButton = findViewById(R.id.button);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        communitiesRef = db.collection("communities");

        // Retrieve user ID from intent
        userId = getIntent().getStringExtra("userID");
        userEmail =getIntent().getStringExtra("userEmail");

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference("event_pics");

        // Set click listener for communityImage to pick an image
        communityImage.setOnClickListener(v -> checkPermissionAndPickImage());

        createCommunityButton.setOnClickListener(v -> saveCommunity());
    }


    private void pickImage() {
        imagePickerLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
        Log.d("pickImage", "Called function.");
    }

    private void updateCommWithImageUrl(String documentId, String imageUrl) {
        String TAG="updating event with image url";
        DocumentReference eventRef = communitiesRef.document(documentId);
        eventRef.update("picture", imageUrl)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Image URL successfully updated!"))
                .addOnFailureListener(e -> Log.w(TAG, "Error updating image URL", e));
    }

    private void uploadImage(String documentId) {
        String TAG = "Uploading image to storage in firestore";
        Log.d(TAG, "Inside uploadImage()");
        if (selectedImageUri != null) {
            final StorageReference imageRef = storageReference.child(documentId + ".jpg");

            // Upload file to Firebase Storage
            imageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String imageUrl = uri.toString();
                                updateCommWithImageUrl(documentId, imageUrl);
                                Log.d(TAG, "Event image uploaded successfully: " + imageUrl);
                            })
                            .addOnFailureListener(e -> {
                                Log.d(TAG, "Failed to get image URL: ", e);
                                e.printStackTrace();
                            }))
                    .addOnFailureListener(e -> {
                        Log.d(TAG, "Failed to upload image: ", e);
                        e.printStackTrace();
                    })
                    .addOnProgressListener(snapshot -> {
                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        Log.d(TAG, "Upload is " + progress + "% done");
                    })
                    .addOnPausedListener(snapshot -> Log.d(TAG, "Upload is paused"));
        } else {
            Log.d(TAG, "SelectedImageUri is null.");
        }
    }


    private void checkPermissionAndPickImage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_IMAGE_PICKER);
        } else {
            // Permission is already granted, launch image picker
            pickImage();
        }
    }
    private void saveCommunity() {
        //String communityName, String imageUrl
        // Create a new community object with the provided details
        Map<String, Object> community = new HashMap<>();
        community.put("tag", editTextCommunityName.getText().toString().trim());
        String pic="https://firebasestorage.googleapis.com/v0/b/numad24sp-group4unilink.appspot.com/o/event_pics%2Fcommunity%20copy.jpg?alt=media&token=f0776a03-3328-4db3-9a02-07edf11213bc";

        community.put("picture", pic);


        List<String> users = new ArrayList<>();
        users.add(userId); // Add the current user ID to the users list
        community.put("users", users);

        // Add the community to Firestore
        communitiesRef.add(community)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Community created successfully!", Toast.LENGTH_SHORT).show();
                    uploadImage(documentReference.getId());
                    Intent intent = new Intent(this, GroupsActivity.class);
                    intent.putExtra("userEmail", userEmail);
                    intent.putExtra("userID", userId);
                    startActivity(intent);

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to create community.", Toast.LENGTH_SHORT).show();
                    Log.e("CreateCommunity", "Error adding community to Firestore", e);
                });
    }
}