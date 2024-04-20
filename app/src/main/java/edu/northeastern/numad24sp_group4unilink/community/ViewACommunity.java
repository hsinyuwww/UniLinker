package edu.northeastern.numad24sp_group4unilink.community;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Consumer;

import edu.northeastern.numad24sp_group4unilink.Attendees.AttendeesActivity;
import edu.northeastern.numad24sp_group4unilink.BaseActivity;
import edu.northeastern.numad24sp_group4unilink.R;
import edu.northeastern.numad24sp_group4unilink.comments.CommentsActivity;
import edu.northeastern.numad24sp_group4unilink.databinding.ActivityCreateEventBinding;
import edu.northeastern.numad24sp_group4unilink.databinding.ActivityViewAcommunityBinding;
import edu.northeastern.numad24sp_group4unilink.events.DeleteCallback;
import edu.northeastern.numad24sp_group4unilink.events.EditEvent;
import edu.northeastern.numad24sp_group4unilink.events.EventAdapter;
import edu.northeastern.numad24sp_group4unilink.events.EventInterface;
import edu.northeastern.numad24sp_group4unilink.events.EventItem;
import edu.northeastern.numad24sp_group4unilink.events.EventsActivity;

public class ViewACommunity extends BaseActivity {
    ActivityViewAcommunityBinding activityViewAcommunityBinding;
    private static final int PERMISSIONS_REQUEST_CALENDAR = 100;

    private ArrayList<EventItem> eventList = new ArrayList<>();
    private RecyclerView recyclerView;

    private EventAdapter eventAdapter;
    private RecyclerView.LayoutManager rLayoutManger;
    private static final String KEY_OF_INSTANCE = "KEY_OF_INSTANCE";
    private static final String NUMBER_OF_ITEMS = "NUMBER_OF_ITEMS";

    public String userEmail, userID, EVENTS_TYPE;
    private Spinner spinnerUsers;
    private FirebaseFirestore db;
    private CollectionReference eventsRef;
    private String commTag,commId,currentUserId, commImage;

    Map<String, String> userIdToEmailMap;
    Button joinCommButton;
    int selectedItemId;
    BottomNavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        activityViewAcommunityBinding = ActivityViewAcommunityBinding.inflate(getLayoutInflater());
        setContentView(activityViewAcommunityBinding.getRoot());

        spinnerUsers= findViewById(R.id.spinnerUsers);

        db = FirebaseFirestore.getInstance();
        navigationView = findViewById(R.id.bottomNavigationView);
        navigationView.setVisibility(View.GONE);
        // Get the commTag from the intent
        commTag = getIntent().getStringExtra("commTag");
        commId = getIntent().getStringExtra("commId");
        currentUserId= getIntent().getStringExtra("userId");
        commImage= getIntent().getStringExtra("imageURL");
        userEmail=getIntent().getStringExtra("userEmail");


        // Initialize Firestore reference to posts collection
        eventsRef = db.collection("posts");

        EVENTS_TYPE = "COMMUNITY_EVENTS";





        joinCommButton = findViewById(R.id.joinComm);
        joinCommButton.setOnClickListener(v -> {
            // Get the community ID you want to join (replace "communityId" with the actual ID)
            String communityId = "communityId";
            // Update the community document in Firestore
            updateCommunityDocument(commId);
        });

        init(savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void init(Bundle savedInstanceState) {


        createRecyclerView();

        if (savedInstanceState != null ) {
            if (eventList == null || eventList.size() == 0) {

                String activity = savedInstanceState.getString("ACTIVITY");
                if(activity=="COMMUNITY") {

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
                    populateSpinnerUsers();
                    callEventsList();
                }
            }


        }else{

            populateSpinnerUsers();
            callEventsList();
        }





    }

    private void createRecyclerView() {


        rLayoutManger = new LinearLayoutManager(this);

        recyclerView = findViewById(R.id.commEventsRecyclerView);

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
                        Toast.makeText(ViewACommunity.this, "Google Maps app not installed.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle the case where location query is empty
                    Toast.makeText(ViewACommunity.this, "Location not available.", Toast.LENGTH_SHORT).show();
                }

                eventAdapter.notifyItemChanged(position);
            }

            @Override
            public void onAttendeesClick(int position) {
                eventList.get(position).onAttendeesClick(position);
                EventItem event = eventList.get(position);
                if (event != null) {
                    Intent intent = new Intent(ViewACommunity.this, AttendeesActivity.class);
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
                checkIfAttending(postID, position);
                eventAdapter.notifyItemChanged(position);

            }

            @Override
            public void onCommentClick(int position) {
                eventList.get(position).onCommentClick(position);
                EventItem event = eventList.get(position);
                if (event != null) {
                    Intent intent = new Intent(ViewACommunity.this, CommentsActivity.class);
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
                    Intent intent = new Intent(ViewACommunity.this, EditEvent.class);
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
                deletePost(event.getEventID(), position, new DeleteCallback() {
                    @Override
                    public void onSuccess() {
                        eventList.remove(position);
                        eventAdapter.notifyItemRemoved(position);
                        eventAdapter.notifyItemRangeChanged(position, eventList.size());
                    }

                });


            }


        };
        eventAdapter.setOnItemClickListener(itemClickListener);
        recyclerView.setLayoutManager(rLayoutManger);
        recyclerView.setAdapter(eventAdapter);




    }

    public void deletePost(String postId, Integer position, DeleteCallback callback){

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
                                callback.onSuccess();
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

    public void checkIfAttending(String postID, Integer position){
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
                            checkCalendarPermission(position);
                            // Attendee added successfully
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


    private void callEventsList() {
        CollectionReference postsRef = eventsRef;
        postsRef.whereEqualTo("tag", commTag).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<String> postTitles = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
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

            } else {
                // Handle failure to retrieve posts
                Toast.makeText(getApplicationContext(), "Failed to retrieve posts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateSpinnerUsers() {
        userIdToEmailMap = new HashMap<>();
        List<String> userIdsL=new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUsers.setAdapter(adapter);
        db.collection("communities").whereEqualTo("tag", commTag).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    List<String> users = (List<String>) document.get("users");

                    if (users != null) {
                        userIdsL.addAll(users);
                    }
                }
                Log.v( "ids", "user ids is " + userIdsL );

                // Query the users collection to get names for each user ID
                adapter.add("See Members");
                for (String user : userIdsL) {
                    db.collection("users").whereEqualTo("userID", user).get().addOnCompleteListener(userTask -> {
                        if (userTask.isSuccessful()) {
                            Log.v("ids-name success", "user ids is " + user + " doc"+userTask.getResult());
                            for (QueryDocumentSnapshot userDoc : userTask.getResult()) {
                                String name = userDoc.getString("email");
                                //TAKE MORE THAN JUST EMAIL, save as a user and use a RECYCLER VIEW HERE
                                Log.v("ids-name",name);

                                if (name != null && !name.isEmpty()) {
                                    // Save the mapping of userID to name for later use
                                    userIdToEmailMap.put(user, name);
                                    adapter.add(name);
                                    Log.v("ids-name", "user ids is " + user + " name: " + name);
                                }
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            // Handle failure to retrieve user's name
                            Log.e("populateSpinnerUsers", "Error getting user's name: " + userTask.getException());
                        }
                    });
                }
                Log.v( "ids", "user emails is " + userIdToEmailMap );


            } else {
                // Handle failure to retrieve users
                Toast.makeText(getApplicationContext(), "Failed to retrieve users", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {

        int size = eventList  == null ? 0 : eventList.size();
        outState.putInt(NUMBER_OF_ITEMS, size);
        outState.putString("ACTIVITY","COMMUNITY");

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


    private void updateCommunityDocument(String communityId) {
        // Reference to the community document in Firestore
        CollectionReference communitiesRef = db.collection("communities");
        communitiesRef.document(communityId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Get the current users array
                    @SuppressWarnings("unchecked")
                    ArrayList<String> users = (ArrayList<String>) document.get("users");
                    if (users == null) {
                        users = new ArrayList<>();
                    }

                    // Add the current user ID to the users array if not already present
                    if (!users.contains(currentUserId)) {
                        users.add(currentUserId);

                        // Update the users array in Firestore
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("users", users);
                        communitiesRef.document(communityId).update(updates).addOnSuccessListener(aVoid -> {
                            // Community updated successfully
                            Toast.makeText(this, "Joined community!", Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(e -> {
                            // Failed to update community
                            Toast.makeText(this, "Failed to join community. Please try again.", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        // User already in the community
                        Toast.makeText(this, "You are already a member of this community.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Community document doesn't exist
                    Toast.makeText(this, "Community not found.", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Error getting community document
                Toast.makeText(this, "Error getting community information. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkCalendarPermission(Integer position) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CALENDAR) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_CALENDAR)) {

                // Explain to the user why we need to read the calendar
                new AlertDialog.Builder(this)
                        .setTitle("Permission Needed")
                        .setMessage("This app needs the Calendar permissions to read and write events.")
                        .setPositiveButton("OK", (dialog, which) -> requestCalendarPermissions())
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .create().show();
            } else {
                // No explanation needed, we can request the permission.
                requestCalendarPermissions();
            }

            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED){
                addCalendarEvent(position);
            }
        } else {
            // Permission has already been granted
            addCalendarEvent(position);
        }
    }

    private void requestCalendarPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR},
                PERMISSIONS_REQUEST_CALENDAR);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CALENDAR) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, do the calendar task you need to do.


            } else {
                // Permission denied, disable the functionality that depends on this permission.
                Toast.makeText(this, "Event is not added to your calendar. You can still attend the event.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void addCalendarEvent(Integer position) {
        EventItem event = eventList.get(position);

        // Get calendar ID through UI
        chooseCalendarId(this, calendarId -> {
            if (calendarId == null) {
                Toast.makeText(this, "No writable calendar selected", Toast.LENGTH_SHORT).show();
                return;
            }

            // Parse date and time
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            long startMillis = 0;
            long endMillis = 0;
            try {
                Date parsedDate = dateFormat.parse(event.getDate() + " " + event.getTime());
                if (parsedDate != null) {
                    Calendar beginTime = Calendar.getInstance();
                    beginTime.setTime(parsedDate);

                    Calendar endTime = (Calendar) beginTime.clone();
                    endTime.add(Calendar.HOUR_OF_DAY, 1); // Adjust end time as needed

                    startMillis = beginTime.getTimeInMillis();
                    endMillis = endTime.getTimeInMillis();
                }
            } catch (ParseException e) {
                Toast.makeText(this, "Failed to parse date or time", Toast.LENGTH_SHORT).show();
                return;
            }

            // Insert event
            ContentResolver cr = getContentResolver();
            ContentValues values = new ContentValues();
            values.put(CalendarContract.Events.DTSTART, startMillis);
            values.put(CalendarContract.Events.DTEND, endMillis);
            values.put(CalendarContract.Events.TITLE, event.getTitle());
            values.put(CalendarContract.Events.DESCRIPTION, event.getDescription());
            values.put(CalendarContract.Events.CALENDAR_ID, calendarId);
            values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

            try {
                Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
                long eventID = Long.parseLong(uri.getLastPathSegment());
                Toast.makeText(this, event.getTitle()+" event added to your calendar ", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Failed to add event to calendar", Toast.LENGTH_SHORT).show();
                Log.e("CalendarError", "Failed to insert event into calendar", e);
            }
        });
    }

    private void chooseCalendarId(Activity context, Consumer<Long> onCalendarChosen) {
        Cursor cursor = null;
        ContentResolver contentResolver = context.getContentResolver();
        Uri calendars = CalendarContract.Calendars.CONTENT_URI;

        String[] projection = {
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME
        };

        // Ensure the selection query checks for sufficient access level to the calendar
        String selection = "(" + CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL + " >= ? )";


        String[] selectionArgs = new String[]{
                Integer.toString(CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR)
        };

        cursor = contentResolver.query(calendars, projection, selection, selectionArgs, null);
        if (cursor != null) {
            Log.d("Calendar", "Number of calendars: " + cursor.getCount());
            ArrayList<String> calendarNames = new ArrayList<>();
            HashMap<String, Long> calendarMap = new HashMap<>();

            while (cursor.moveToNext()) {
                long id = cursor.getLong(0);
                String name = cursor.getString(1);
                calendarNames.add(name);
                calendarMap.put(name, id);
                Log.d("Calendar", "Found calendar: " + name + " with ID: " + id);
            }
            cursor.close();


            // Now create a dialog for the user to select a calendar
            // Now create a dialog for the user to select a calendar
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Choose a Calendar");
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.select_dialog_singlechoice, calendarNames);
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            builder.setAdapter(adapter, (dialog, which) -> {
                String calendarName = adapter.getItem(which);
                Long calendarId = calendarMap.get(calendarName);
                onCalendarChosen.accept(calendarId);
            });

            builder.show();
        } else {
            Log.d("Calendar", "Cursor is null");
        }

    }


}