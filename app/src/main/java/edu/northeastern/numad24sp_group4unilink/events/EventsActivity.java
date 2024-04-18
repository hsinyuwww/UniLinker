package edu.northeastern.numad24sp_group4unilink.events;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.northeastern.numad24sp_group4unilink.Attendees.AttendeesActivity;
import edu.northeastern.numad24sp_group4unilink.BaseActivity;
import edu.northeastern.numad24sp_group4unilink.MainActivity;
import edu.northeastern.numad24sp_group4unilink.R;
import edu.northeastern.numad24sp_group4unilink.comments.CommentsActivity;
import edu.northeastern.numad24sp_group4unilink.databinding.ActivityEventsBinding;


public class EventsActivity extends BaseActivity {

    private ArrayList<EventItem> eventList = new ArrayList<>();
    private RecyclerView recyclerView;

    private CollectionReference eventsRef;
    private EventAdapter eventAdapter;
    private FirebaseFirestore db;
    private RecyclerView.LayoutManager rLayoutManger;
    private static final String KEY_OF_INSTANCE = "KEY_OF_INSTANCE";
    private static final String NUMBER_OF_ITEMS = "NUMBER_OF_ITEMS";

    public String userEmail, userID, EVENTS_TYPE;
    ActivityEventsBinding activityEventsBinding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        activityEventsBinding = ActivityEventsBinding.inflate(getLayoutInflater());
        setContentView(activityEventsBinding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Intent intent = getIntent();

        userEmail = intent.getStringExtra("userEmail");
        userID =  intent.getStringExtra("userID");
        EVENTS_TYPE = intent.getStringExtra("EVENTS_TYPE");
        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("posts");
        init(savedInstanceState);



    }

    public void callEventsList(){


        //My posts
        CollectionReference postsRef = eventsRef;
        postsRef.whereEqualTo("authorId", userID).get().addOnSuccessListener(queryDocumentSnapshots -> {
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
                    Log.v("My Events User: ",userEmail);
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

                String activity = savedInstanceState.getString("ACTIVITY");
                if(activity=="EVENT") {

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
                    for (int i = 0; i < eventList.size(); i++) {
                        eventAdapter.notifyItemInserted(i);
                    }
                }else{
                    callEventsList();
                }
            }


        }else{

            callEventsList();
        }





    }

    private void createRecyclerView() {


        rLayoutManger = new LinearLayoutManager(this);

        recyclerView = findViewById(R.id.myEventsRecyclerView);

        recyclerView.setNestedScrollingEnabled(true);

        eventAdapter = new EventAdapter(eventList, EVENTS_TYPE);
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
                        Toast.makeText(EventsActivity.this, "Google Maps app not installed.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle the case where location query is empty
                    Toast.makeText(EventsActivity.this, "Location not available.", Toast.LENGTH_SHORT).show();
                }

                eventAdapter.notifyItemChanged(position);
            }

            @Override
            public void onAttendeesClick(int position) {
                eventList.get(position).onAttendeesClick(position);
                EventItem event = eventList.get(position);
                if (event != null) {
                    Intent intent = new Intent(EventsActivity.this, AttendeesActivity.class);
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
                    Intent intent = new Intent(EventsActivity.this, CommentsActivity.class);
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
                EventItem event = eventList.get(position);
                if (event != null) {
                    Intent intent = new Intent(EventsActivity.this, EditEvent.class);
                    intent.putExtra("postID", event.getEventID());
                    intent.putExtra("userID", userID);
                    intent.putExtra("userEmail", userEmail);
                    startActivity(intent);
                    Log.v("Edit Clicked", "PostID: " + event.getEventID());
                }

                eventAdapter.notifyDataSetChanged();
            }

            @Override
            public void onDeleteClick(int position) {
                eventList.get(position).onDeleteClick(position);
                EventItem event = eventList.get(position);
                deletePost(event.getEventID(), position);

            }


        };
        eventAdapter.setOnItemClickListener(itemClickListener);
        recyclerView.setLayoutManager(rLayoutManger);
        recyclerView.setAdapter(eventAdapter);




    }

    public void deletePost(String postId, Integer position){
        CollectionReference postsRef = eventsRef;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Confirm Delete");
        builder.setMessage("Do you want to delete this event?");

        // Add the buttons
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked Yes button
                postsRef.document(postId)
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Post deleted successfully
                                eventList.remove(position);
                                // Notify the adapter of the item removed
                                eventAdapter.notifyItemRemoved(position);
                                eventAdapter.notifyItemRangeChanged(position, eventList.size());
                                Toast.makeText(getApplicationContext(), "Post deleted successfully", Toast.LENGTH_SHORT).show();

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Handle the failure to delete the post
                                Log.e("Firestore", "Error deleting post: " + e.getMessage());
                                Toast.makeText(getApplicationContext(), "Failed to delete post", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                dialog.dismiss();
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();



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
        outState.putString("ACTIVITY","EVENT");

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