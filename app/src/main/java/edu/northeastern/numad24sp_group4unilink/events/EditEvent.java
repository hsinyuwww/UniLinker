package edu.northeastern.numad24sp_group4unilink.events;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.northeastern.numad24sp_group4unilink.R;

public class EditEvent extends AppCompatActivity {

    private EditText eventTitleEditText, eventDescEditText, eventLocationEditText, editTextTime, editTextDate;
    private Spinner eventTagSpinner;
    private ImageView eventImageView;

    private TextView postIdTextView;
    private Button editPostButton;
    String postId, userId;
    FirebaseFirestore db;
    CollectionReference communitiesRef;
    String selectedCommunity;

    String eventTitle, eventDesc, eventLoc, eventDate, eventTime, eventTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_event);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        postId = getIntent().getStringExtra("postID");
        userId = getIntent().getStringExtra("userID");
        // Find the TextView for displaying the post ID
        postIdTextView = findViewById(R.id.textView);

        // Display the post ID in the TextView
        postIdTextView.setText("EDIT POST : " + postId);

        eventTitleEditText=findViewById(R.id.eventTitle);
        eventDescEditText=findViewById(R.id.eventDesc);
        editTextTime = findViewById(R.id.editTextTime);
        editTextDate = findViewById(R.id.editTextDate);
        eventLocationEditText = findViewById(R.id.eventlocation);
        eventImageView = findViewById(R.id.eventImage);
        editPostButton= findViewById(R.id.editPostButton);
        // In your button click listener, call compareAndShowChanges()
        editPostButton.setOnClickListener(v -> compareAndShowChanges());

        db = FirebaseFirestore.getInstance();

        communitiesRef = db.collection("communities");
        setUpSpinnerOptions();

        // Trigger function to populate UI with Firestore data
        populateFieldsFromFirestore();
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

    private void setUpSpinnerOptions(){
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
    }

    private void populateFieldsFromFirestore() {

        db.collection("posts").document(postId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                Log.e("Firestore", "Snapshot:" +document);
                if (document.exists()) {
                    // Populate UI fields with Firestore data
                    eventTitle=document.getString("title");
                    eventDesc=document.getString("description");
                    eventLoc=document.getString("location");

                    eventTitleEditText.setText(eventTitle);
                    eventDescEditText.setText(eventDesc);
                    eventLocationEditText.setText(eventLoc);

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
                        eventDate=dateString;
                        eventTime=timeString;
                        editTextDate.setText(dateString);
                        editTextTime.setText(timeString);
                    }

                    //tag
                    // Set selected value in spinner
                    eventTag=document.getString("tag");
                    setSpinnerSelection(eventTag);

                    String imageUrl=document.getString("picture");
                    if (imageUrl!=null) {
                        Log.e("Image stored", "url: "+imageUrl);
                        //picture
                        Picasso.get()
                                .load(imageUrl)
                                .placeholder(R.drawable.event) // Optional placeholder image
                                .error(R.drawable.error) // Optional error image
                                .into(eventImageView);
                    }
                }
            } else {
                // Handle errors while fetching data
                Log.e("Firestore", "Error getting sent post from DB ");
            }
        });
    }

    private void setSpinnerSelection( String valueToSelect) {
        // Code to set selected value in spinner
        Log.v("Spinner", "Value:"+valueToSelect);
        if (eventTagSpinner != null && valueToSelect != null) {
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) eventTagSpinner.getAdapter();
            if (adapter != null) {
                int position = adapter.getPosition(valueToSelect);
                eventTagSpinner.setSelection(position);
            }
        }
    }

    // Method to compare values and show alert dialog
    private void compareAndShowChanges() {
        String newEventTitle = eventTitleEditText.getText().toString();
        String newEventDesc = eventDescEditText.getText().toString();
        String newEventLoc = eventLocationEditText.getText().toString();
        String newEventDate = editTextDate.getText().toString();
        String newEventTime = editTextTime.getText().toString();
        String newEventTag = selectedCommunity; // Assuming you set selectedCommunity from the spinner

        StringBuilder changesBuilder = new StringBuilder("Changes:\n");

        //eventTitle, eventDesc, eventLoc, eventDate, eventTime, eventTag;
        if (!newEventTitle.equals(eventTitle)) {
            changesBuilder.append("Title: ").append(eventTitle).append(" -> ").append(newEventTitle).append("\n");
        }
        if (!newEventDesc.equals(eventDesc)) {
            changesBuilder.append("Description: ").append(eventDesc).append(" -> ").append(newEventDesc).append("\n");
        }
        if (!newEventLoc.equals(eventLoc)) {
            changesBuilder.append("Location: ").append(eventLoc).append(" -> ").append(newEventLoc).append("\n");
        }
        if (!newEventDate.equals(eventDate)) {
            changesBuilder.append("Date: ").append(eventDate).append(" -> ").append(newEventDate).append("\n");
        }

        if (!newEventTime.equals(eventTime)) {
            changesBuilder.append("Time: ").append(eventTime).append(" -> ").append(newEventTime).append("\n");
        }
        if (!newEventTag.equals(eventTag)) {
            changesBuilder.append("Tag: ").append(eventTag).append(" -> ").append(newEventTag).append("\n");
        }


        // Add comparisons for other fields as needed

        // Show the changes in an alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Changes");
        builder.setMessage(changesBuilder.toString());
        builder.setPositiveButton("Save", (dialog, which) -> saveChanges( newEventTitle,  newEventDesc,  newEventLoc,  newEventDate,  newEventTime,  newEventTag));
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // Method to save changes to Firestore
    private void saveChanges(String newEventTitle, String newEventDesc, String newEventLoc, String newEventDate, String newEventTime, String newEventTag) {
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        Date combinedDateTime=currentDate;

        String dateFormat = "dd/MM/yyyy";
        String timeFormat = "HH:mm";
        SimpleDateFormat sdfDate = new SimpleDateFormat(dateFormat, Locale.getDefault());
        SimpleDateFormat sdfTime = new SimpleDateFormat(timeFormat, Locale.getDefault());

        try {
            String dateString = newEventDate;
            String timeString = newEventTime;

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
        // Update Firestore document with new values
        // Use postId to identify the document
        db.collection("posts").document(postId)
                .update("title", newEventTitle,
                        "description", newEventDesc,
                        "location", newEventLoc,
                        "eventDate", combinedDateTime,
                        "tag", newEventTag)
                .addOnSuccessListener(aVoid -> {
                    // Handle success
                    Log.d("Firestore", "Document updated successfully");
                    // Optionally show a toast or message to indicate success
                    Toast.makeText(this, "Changes saved successfully", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity after saving changes
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Log.e("Firestore", "Error updating document", e);
                    // Optionally show a toast or message to indicate failure
                    Toast.makeText(this, "Failed to save changes", Toast.LENGTH_SHORT).show();
                });
    }
}