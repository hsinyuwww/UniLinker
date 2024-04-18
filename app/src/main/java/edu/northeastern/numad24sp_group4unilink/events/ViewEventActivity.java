package edu.northeastern.numad24sp_group4unilink.events;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import edu.northeastern.numad24sp_group4unilink.R;
import edu.northeastern.numad24sp_group4unilink.databinding.ActivityProfileBinding;

import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

public class ViewEventActivity extends AppCompatActivity {

    ActivityProfileBinding activityProfileBinding;
    private TextView eventTitle, eventDesc, editTextDate, editTextTime, eventlocation, eventTag;
    private ImageView eventImage;
    private Button Attend, Comment;
    private Spinner attendeesSpinner,commentsSpinner;
    String postId, userId, userEmail;
    FirebaseFirestore db;
    CollectionReference communitiesRef;
    String selectedCommunity;
    Map<String, String> userIdToEmailMap, commentIdtoComment;
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
        setContentView(R.layout.activity_view_event);

        // Get the post ID from the intent
        postId = getIntent().getStringExtra("postId");
        userId = getIntent().getStringExtra("userId");
        userEmail = getIntent().getStringExtra("userEmail");

        eventTitle=findViewById(R.id.eventTitle);
        eventDesc=findViewById(R.id.eventDesc);
        editTextTime = findViewById(R.id.editTextTime);
        editTextDate = findViewById(R.id.editTextDate);
        eventlocation = findViewById(R.id.eventlocation);
        eventImage = findViewById(R.id.eventImage);
        eventTag=findViewById(R.id.eventTag);
        Attend= findViewById(R.id.Attend);
        db = FirebaseFirestore.getInstance();

        communitiesRef = db.collection("communities");

        // Trigger function to populate UI with Firestore data
        populateFieldsFromFirestore();

        // Assuming you have already initialized eventlocation TextView
        eventlocation.setOnClickListener(v -> {
            String locationQuery = eventlocation.getText().toString();
            Log.d("loc to open:", " is "+locationQuery);
            if (!locationQuery.isEmpty()) {
                // Create a Uri for the Google Maps search query
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(locationQuery));

                // Create an Intent to launch Google Maps with the search query
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps"); // Specify package to ensure Maps app is used
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    // Handle the case where Google Maps app is not installed
                    Toast.makeText(this, "Google Maps app not installed.", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Handle the case where location query is empty
                Toast.makeText(this, "Location not available.", Toast.LENGTH_SHORT).show();
            }
        });

        Attend.setOnClickListener(v -> {
            // Check if the current user is already attending the post
            checkIfAttendingPost();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }


    private void populateFieldsFromFirestore() {

        db.collection("posts").document(postId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                Log.e("Firestore", "Snapshot:" +document);
                if (document.exists()) {
                    // Populate UI fields with Firestore data
                    eventTitle.setText(document.getString("title"));
                    eventDesc.setText(document.getString("description"));
                    eventlocation.setText(document.getString("location"));

                    //event Date - 2
                    // Get timestamp from Firestore
                    Timestamp timestamp = document.getTimestamp("eventDate");
                    if (timestamp != null) {
                        // Convert timestamp to Date
                        Date date = timestamp.toDate();
                        // Format date and time
                        SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
                        String dateString = sdfDate.format(date);
                        String timeString = sdfTime.format(date);
                        // Set date and time in EditText fields
                        editTextDate.setText(dateString);
                        editTextTime.setText(timeString);
                    }

                    //tag
                    eventTag.setText(document.getString("tag"));

                    String imageUrl=document.getString("picture");
                    if (imageUrl!=null) {
                        Log.e("Image stored", "url: "+imageUrl);
                        //picture
                        Picasso.get()
                                .load(imageUrl)
                                .placeholder(R.drawable.event) // Optional placeholder image
                                .error(R.drawable.event) // Optional error image
                                .into(eventImage);
                    }
                }
            } else {
                // Handle errors while fetching data
                Log.e("Firestore", "Error getting sent post from DB ");
            }
        });
    }

    private void checkIfAttendingPost() {
        //THIS FUNCTION LITERALLY ADDS THE LOGGED IN USER TO THE EVEMNT"S LIST OF ATTENDEES
        // Reference to the post document in Firestore
        CollectionReference postsRef = db.collection("posts");
        postsRef.document(postId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Get the current attendees array
                    @SuppressWarnings("unchecked")
                    List<String> attendees = (List<String>) document.get("attendees");
                    if (attendees == null) {
                        attendees = new ArrayList<>();
                    }

                    // Check if the current user is already attending the post
                    if (!attendees.contains(userId)) {
                        // Add the current user to the attendees array
                        attendees.add(userId);

                        // Update the attendees array in Firestore
                        postsRef.document(postId).update("attendees", attendees).addOnSuccessListener(aVoid -> {
                            // Attendee added successfully
                            Toast.makeText(this, "You are now attending the event.", Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(e -> {
                            // Failed to update attendees array
                            Toast.makeText(this, "Failed to attend the event. Please try again.", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        // User is already attending the post
                        Toast.makeText(this, "You are already attending this event.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Post document doesn't exist
                    Toast.makeText(this, "Event not found.", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Error getting post document
                Toast.makeText(this, "Error getting post information. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }


}