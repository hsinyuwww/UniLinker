package edu.northeastern.numad24sp_group4unilink.events;

import android.Manifest;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import edu.northeastern.numad24sp_group4unilink.BaseActivity;
import edu.northeastern.numad24sp_group4unilink.R;
import edu.northeastern.numad24sp_group4unilink.databinding.ActivityCreateEventBinding;



import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import android.net.Uri;

import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Map;



public class CreateEvent extends BaseActivity {

    ActivityCreateEventBinding activityCreateEventBinding;

    private String userId;
    private String selectedCommunity;
    EditText editTextTime, editTextDate, editTextEventTitle, editTextEventDesc;
    private EditText editTextLocation;
    private Spinner eventTagSpinner;
    private ImageView eventImage;
    private Button createPostButton;
    private Uri selectedImageUri;
    private StorageReference storageReference;
    private CollectionReference eventsRef;
    private BottomNavigationView navigationView;
    ActivityResultLauncher<PickVisualMediaRequest> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.PickVisualMedia(),
            result -> {
                if (result != null) {
                    selectedImageUri = result;
                    eventImage.setImageURI(selectedImageUri);
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
        activityCreateEventBinding = ActivityCreateEventBinding.inflate(getLayoutInflater());
        setContentView(activityCreateEventBinding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        navigationView = findViewById(R.id.bottomNavigationView);
        navigationView.setVisibility(View.GONE);

        editTextEventTitle=findViewById(R.id.eventTitle);
        editTextEventDesc=findViewById(R.id.eventDesc);
        editTextTime = findViewById(R.id.editTextTime);
        editTextDate = findViewById(R.id.editTextDate);
        editTextLocation = findViewById(R.id.eventlocation);
        eventImage = findViewById(R.id.eventImage);
        createPostButton= findViewById(R.id.createPostButton);
        createPostButton.setOnClickListener(c->saveEvent());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Initializes the Firebase Storage and gets a reference to event_pics in storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference("event_pics");
        eventImage.setOnClickListener(v -> checkPermissionAndPickImage());
        eventImage.setOnClickListener(v -> checkPermissionAndPickImage());


        eventsRef = db.collection("posts");


        CollectionReference communitiesRef = db.collection("communities");

        //SPINNER
        eventTagSpinner = findViewById(R.id.eventTag);

        communitiesRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<String> tagList = new ArrayList<>();
            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                String tag = document.getString("tag");
                if (tag != null && !tag.isEmpty() && !tagList.contains(tag)) {
                    tagList.add(tag);
                }
            }
            // Populate the spinner with the tagList data
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tagList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            eventTagSpinner.setAdapter(adapter);

        }).addOnFailureListener(e -> {
            Log.e("Firestore", "Error getting tags: ", e);
        });

        // Set up the OnItemSelectedListener for the spinner
        eventTagSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Get the selected tag name
                selectedCommunity = (String) parentView.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Handle the case where nothing is selected (if needed)
                selectedCommunity="";
            }
        });

        // Retrieve user's email address from intent
        String userEmail = getIntent().getStringExtra("userEmail");
        userId=getIntent().getStringExtra("userID");

        if (userEmail != null) {
            // Update UI with user's email address (e.g., set text in a TextView)
            TextView userEmailTextView = findViewById(R.id.textView);
            userEmailTextView.setText("Create post as: " + userEmail);
            Toast.makeText(this, "User  id:"+userId, Toast.LENGTH_SHORT).show();
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    private void saveEvent() {
        //attendees;
        //comments;
        String authorId=userId;
        String description=editTextEventDesc.getText().toString().trim();
        String title=editTextEventTitle.getText().toString().trim();
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        Date combinedDateTime=currentDate;

        String dateFormat = "dd/MM/yyyy";
        String timeFormat = "HH:mm";
        SimpleDateFormat sdfDate = new SimpleDateFormat(dateFormat, Locale.getDefault());
        SimpleDateFormat sdfTime = new SimpleDateFormat(timeFormat, Locale.getDefault());

        try {
            // Get the text from editTextDate and editTextTime
            String dateString = editTextDate.getText().toString();
            String timeString = editTextTime.getText().toString();

            // Parse the strings into Date objects
            Date date = sdfDate.parse(dateString);
            Date time = sdfTime.parse(timeString);

            // Combine date and time into a Calendar instance

            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, time.getHours());
            calendar.set(Calendar.MINUTE, time.getMinutes());

            // Now 'calendar' contains the combined date and time value
            combinedDateTime = calendar.getTime();

            // You can use 'combinedDateTime' as needed, e.g., store it in Firestore
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Date eventDate=combinedDateTime;


        Date postedDate=currentDate;
        String eventLocation=editTextLocation.getText().toString().trim();
        String picture="https://firebasestorage.googleapis.com/v0/b/numad24sp-group4unilink.appspot.com/o/event_pics%2Fevent.avif?alt=media&token=5a12990b-6d69-4fa4-8dd4-5f89d90848de";
        String tag=selectedCommunity;

        //String eventCreator = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        // Assemble the event
        Map<String, Object> event = new HashMap<>();
        event.put("title", title);
        event.put("description",description);
        event.put("postedDate", postedDate);
        event.put("tag", tag);
        event.put("picture", picture);
        event.put("location", eventLocation);
        event.put("eventDate", eventDate);
        event.put("comments", new ArrayList<>());
        event.put("attendees", new ArrayList<>());
        event.put("authorId", authorId);
        eventsRef.add(event)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Event created successfully!", Toast.LENGTH_SHORT).show();
                    clearFields();
                    uploadImage(documentReference.getId());

                    String newEventId = documentReference.getId();
                    Toast.makeText(this, "Event id:"+newEventId, Toast.LENGTH_SHORT).show();
//                    userRef.document(eventCreator)
//                            .update("hosting", FieldValue.arrayUnion(newEventId))
//                            .addOnSuccessListener(void1 -> Log.d(TAG, "Event added to hosting array successfully"))
//                            .addOnFailureListener(e -> Log.e(TAG, "Error adding event to hosting array", e));
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to create event.", Toast.LENGTH_SHORT).show());
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

    private void pickImage() {

        imagePickerLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
        Log.d("pickImage", "Called function.");

    }
    private void updateEventWithImageUrl(String documentId, String imageUrl) {
        String TAG="updating event with image url";
        DocumentReference eventRef = eventsRef.document(documentId);
        eventRef.update("picture", imageUrl)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Image URL successfully updated!"))
                .addOnFailureListener(e -> Log.w(TAG, "Error updating image URL", e));
    }

    /**
     * Upload the selected image to Firebase storage.
     * @param documentId The id of the event document.
     */
    private void uploadImage(String documentId) {
        String TAG="Uploading image to storage in firestore";
        Log.d(TAG, "Inside uploadImage()");
        if (selectedImageUri != null) {
            final StorageReference imageRef = storageReference.child(documentId + ".jpg");

            // Upload file to Firebase Storage
            imageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String imageUrl = uri.toString();
                                updateEventWithImageUrl(documentId, imageUrl);
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






    public void showTimePickerDialog(View v) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                // Set the selected time in the EditText field
                String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                editTextTime.setText(selectedTime);
            }
        }, Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), true);
        timePickerDialog.show();
    }
    public void showDatePickerDialog(View v) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                // Set the selected date in the EditText field
                String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                editTextDate.setText(selectedDate);
            }
        }, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void clearFields() {
        editTextEventTitle.setText("");
        editTextEventDesc.setText("");
        editTextTime.setText("");
        editTextDate.setText("");
        editTextLocation.setText("");
        //eventTagSpinner.setTe
        //eventImage.setText("");
        eventImage.setImageResource(R.drawable.event);
    }
}