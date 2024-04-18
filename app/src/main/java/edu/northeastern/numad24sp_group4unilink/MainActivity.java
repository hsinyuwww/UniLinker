package edu.northeastern.numad24sp_group4unilink;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;



import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import edu.northeastern.numad24sp_group4unilink.Attendees.AttendeesActivity;
import edu.northeastern.numad24sp_group4unilink.comments.CommentsActivity;
import edu.northeastern.numad24sp_group4unilink.databinding.ActivityMainBinding;
import edu.northeastern.numad24sp_group4unilink.events.EventAdapter;
import edu.northeastern.numad24sp_group4unilink.events.EventInterface;
import edu.northeastern.numad24sp_group4unilink.events.EventItem;

public class MainActivity extends BaseActivity {
    private ArrayList<EventItem> eventList = new ArrayList<>();
    private RecyclerView recyclerView;
    private Map<String, String> postTitlesAndIds;
    private CollectionReference eventsRef;
    private EventAdapter eventAdapter;
    private FirebaseFirestore db;
    private RecyclerView.LayoutManager rLayoutManger;
    private static final String KEY_OF_INSTANCE = "KEY_OF_INSTANCE";
    private static final String NUMBER_OF_ITEMS = "NUMBER_OF_ITEMS";

    public String userEmail, userID, EVENT_TYPE;

    ActivityMainBinding activityMainBinding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();

        userEmail = intent.getStringExtra("userEmail");
        userID =  intent.getStringExtra("userID");
        EVENT_TYPE = intent.getStringExtra("EVENT_TYPE");
        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("posts");
        init(savedInstanceState);





    }

    public void callEventsList(){


        //all posts
        CollectionReference postsRef = eventsRef;
        postsRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<String> postTitles = new ArrayList<>();
            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                // Assuming 'title' is the attribute in your posts collection
                String title = document.getString("title");
                String postId = document.getId();
                if (title != null && !title.isEmpty() && !postTitles.contains(title)) {
                    postTitles.add(title);

                    String description, dateString, timeString, imageURL, location, community;
                    dateString="";
                    timeString="";
                    description=document.getString("description");
                    location=document.getString("location");


                    Timestamp timestamp = document.getTimestamp("eventDate");
                    if (timestamp != null) {
                        // Convert timestamp to Date
                        Date date = timestamp.toDate();
                        // Format date and time
                        SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
                        dateString = sdfDate.format(date);
                        timeString = sdfTime.format(date);

                    }

                    community=document.getString("tag");

                    imageURL=document.getString("picture");
                    Log.v("Document details : ", title+" , "+description+" , "+dateString+" , "+timeString+" , "+imageURL+" , "+location+" , "+community+" , "+postId);
                    EventItem eventCard = new EventItem(title, description, dateString, timeString, imageURL, location, community, postId);

                    eventList.add(eventCard);
                }
            }


            for(int i=0;i<eventList.size();i++){
                eventAdapter.notifyItemInserted(i);

            }

            Log.v("Size of Event Array", Integer.toString(eventList.size()));



        }).addOnFailureListener(e -> {
            // Handle failure to retrieve posts
            Log.e("Firestore", "Error getting posts: ", e);
        });


    }




    private void init(Bundle savedInstanceState) {


        createRecyclerView();

        if (savedInstanceState != null ) {
            if (eventList == null || eventList.size() == 0) {

                int size = savedInstanceState.getInt(NUMBER_OF_ITEMS);

                // Retrieve keys we stored in the instance
                for (int i = 0; i < size; i++) {

                    String title = savedInstanceState.getString(KEY_OF_INSTANCE + i + "0");
                    String description = savedInstanceState.getString(KEY_OF_INSTANCE + i + "1");
                    String date = savedInstanceState.getString(KEY_OF_INSTANCE + i + "2");
                    String time = savedInstanceState.getString(KEY_OF_INSTANCE + i + "3");
                    String image = savedInstanceState.getString(KEY_OF_INSTANCE + i + "4");
                    String location = savedInstanceState.getString(KEY_OF_INSTANCE + i + "5");
                    String community = savedInstanceState.getString(KEY_OF_INSTANCE + i + "6");
                    String eventID = savedInstanceState.getString(KEY_OF_INSTANCE + i + "7");

                    EventItem eventCard = new EventItem(title, description, date, time, image, location, community, eventID);

                    eventList.add(eventCard);

                }
                for(int i=0;i<eventList.size();i++){
                    eventAdapter.notifyItemInserted(i);
                }
            }


        }else{

            callEventsList();
        }





    }

    private void createRecyclerView() {


        rLayoutManger = new LinearLayoutManager(this);

        recyclerView = findViewById(R.id.recyclerViewEvents);

        recyclerView.setNestedScrollingEnabled(true);

        eventAdapter = new EventAdapter(eventList);
        EventInterface itemClickListener = new EventInterface() {

            @Override
            public void onLocationClick(int position) {
                eventList.get(position).onLocationClick(position);
                EventItem event = eventList.get(position);
                String locationQuery = event.getLocation();
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
                        Toast.makeText(MainActivity.this, "Google Maps app not installed.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle the case where location query is empty
                    Toast.makeText(MainActivity.this, "Location not available.", Toast.LENGTH_SHORT).show();
                }

                eventAdapter.notifyItemChanged(position);
            }

            @Override
            public void onAttendeesClick(int position) {
                eventList.get(position).onAttendeesClick(position);
                EventItem event = eventList.get(position);
                if (event != null) {
                    Intent intent = new Intent(MainActivity.this, AttendeesActivity.class);
                    intent.putExtra("postID", event.getEventID());
                    intent.putExtra("userID", userID);
                    intent.putExtra("userEmail", userEmail);
                    startActivity(intent);
                    Log.v("Attendees Clicked", "PostID: " + event.getEventID());
                }
                eventAdapter.notifyItemChanged(position);
            }


            @Override
            public void onAttendClick(int position) {
                eventList.get(position).onAttendClick(position);
                EventItem event = eventList.get(position);
                String postID = event.getEventID();
                checkIfAttending(postID);
                eventAdapter.notifyItemChanged(position);

            }

            @Override
            public void onCommentClick(int position) {
                eventList.get(position).onCommentClick(position);
                EventItem event = eventList.get(position);
                if (event != null) {
                    Intent intent = new Intent(MainActivity.this, CommentsActivity.class);
                    intent.putExtra("postID", event.getEventID());
                    intent.putExtra("userID", userID);
                    intent.putExtra("userEmail", userEmail);
                    startActivity(intent);
                    Log.v("Comment Clicked", "PostID: " + event.getEventID());
                }

                eventAdapter.notifyItemChanged(position);

            }

            @Override
            public void onEditClick(int position) {
                eventList.get(position).onEditClick(position);

                eventAdapter.notifyItemChanged(position);
            }

            @Override
            public void onDeleteClick(int position) {
                eventList.get(position).onDeleteClick(position);

                eventAdapter.notifyItemChanged(position);
            }


        };
        eventAdapter.setOnItemClickListener(itemClickListener);
        recyclerView.setLayoutManager(rLayoutManger);
        recyclerView.setAdapter(eventAdapter);




    }

    public void checkIfAttending(String postID){
        CollectionReference postsRef = db.collection("posts");
        postsRef.document(postID).get().addOnCompleteListener(task -> {
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
                    if (!attendees.contains(userID)) {
                        // Add the current user to the attendees array
                        attendees.add(userID);

                        // Update the attendees array in Firestore
                        postsRef.document(postID).update("attendees", attendees).addOnSuccessListener(aVoid -> {
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

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {

        int size = eventList  == null ? 0 : eventList.size();
        outState.putInt(NUMBER_OF_ITEMS, size);

        // Need to generate unique key for each item
        // This is only a possible way to do, please find your own way to generate the key
        for (int i = 0; i < size; i++) {

            outState.putString(KEY_OF_INSTANCE + i + "0", eventList.get(i).getTitle());

            outState.putString(KEY_OF_INSTANCE + i + "1", eventList.get(i).getDescription());

            outState.putString(KEY_OF_INSTANCE + i + "2", eventList.get(i).getDate());

            outState.putString(KEY_OF_INSTANCE + i + "3", eventList.get(i).getTime());

            outState.putString(KEY_OF_INSTANCE + i + "4", eventList.get(i).getImage());
            outState.putString(KEY_OF_INSTANCE + i + "5", eventList.get(i).getLocation());
            outState.putString(KEY_OF_INSTANCE + i + "6", eventList.get(i).getCommunity());
            outState.putString(KEY_OF_INSTANCE + i + "7", eventList.get(i).getEventID());
        }

        super.onSaveInstanceState(outState);

    }

}