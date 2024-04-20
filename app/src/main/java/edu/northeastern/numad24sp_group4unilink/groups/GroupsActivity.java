package edu.northeastern.numad24sp_group4unilink.groups;


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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import edu.northeastern.numad24sp_group4unilink.Attendees.AttendeesActivity;
import edu.northeastern.numad24sp_group4unilink.BaseActivity;
import edu.northeastern.numad24sp_group4unilink.R;
import edu.northeastern.numad24sp_group4unilink.comments.CommentsActivity;
import edu.northeastern.numad24sp_group4unilink.comments.CommentsItem;
import edu.northeastern.numad24sp_group4unilink.community.ViewACommunity;
import edu.northeastern.numad24sp_group4unilink.databinding.ActivityGroupsBinding;
import edu.northeastern.numad24sp_group4unilink.events.DeleteCallback;
import edu.northeastern.numad24sp_group4unilink.events.EditEvent;
import edu.northeastern.numad24sp_group4unilink.events.EventAdapter;
import edu.northeastern.numad24sp_group4unilink.events.EventInterface;
import edu.northeastern.numad24sp_group4unilink.events.EventItem;
import edu.northeastern.numad24sp_group4unilink.events.EventsActivity;
import edu.northeastern.numad24sp_group4unilink.events.ViewEventActivity;


public class GroupsActivity extends BaseActivity {
    private ArrayList<GroupsItem> communityList = new ArrayList<>();
    private RecyclerView recyclerView;
    private int selectedItemId;
    private CollectionReference groupsRef;
    private GroupsAdapter groupsAdapter;
    private FirebaseFirestore db;
    private RecyclerView.LayoutManager rLayoutManger;
    private static final String KEY_OF_INSTANCE = "KEY_OF_INSTANCE";
    private static final String NUMBER_OF_ITEMS = "NUMBER_OF_ITEMS";

    public String userEmail, userID;
    BottomNavigationView navigationView;
    ActivityGroupsBinding activityGroupsBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        activityGroupsBinding = ActivityGroupsBinding.inflate(getLayoutInflater());
        setContentView(activityGroupsBinding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        navigationView = findViewById(R.id.bottomNavigationView);
        selectedItemId = intent.getIntExtra("NAV_ITEM_ID", R.id.home); // Default to home
        navigationView.setSelectedItemId(selectedItemId);

        userEmail = intent.getStringExtra("userEmail");
        userID =  intent.getStringExtra("userID");
        db = FirebaseFirestore.getInstance();
        groupsRef = db.collection("communities");
        init(savedInstanceState);

    }

    public void callCommunitiesList(){


        //My posts
        CollectionReference commRef = groupsRef;
        db = FirebaseFirestore.getInstance();

        commRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<String> commTitles = new ArrayList<>();
            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                // Assuming 'tag' is the attribute in your communities collection
                String tag = document.getString("tag");
                String commId = document.getId();
                // Assuming 'users' is an array of strings in Firestore
                List<String> users = document.contains("users") ? (List<String>) document.get("users") : new ArrayList<>();
                String pictureUrl = document.getString("picture");


                if (tag != null && !tag.isEmpty() && !commTitles.contains(tag)) {

                    GroupsItem groupsCard = new GroupsItem(tag, pictureUrl, commId);
                    communityList.add(groupsCard);
                    groupsAdapter.notifyItemInserted(communityList.size() - 1);
                }
            }

        }).addOnFailureListener(e -> {
            // Handle failure to retrieve communities
            Log.e("Firestore", "Error getting communities: ", e);
        });



    }

    public void checkIfJoined(String communityID){

        CollectionReference communitiesRef = groupsRef;
        communitiesRef.document(communityID).get().addOnCompleteListener(task -> {
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
                    if (!users.contains(userID)) {
                        users.add(userID);

                        // Update the users array in Firestore
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("users", users);
                        communitiesRef.document(communityID).update(updates).addOnSuccessListener(aVoid -> {
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


    private void init(Bundle savedInstanceState) {


        createRecyclerView();

        if (savedInstanceState != null ) {
            if (communityList == null || communityList.size() == 0) {


                    int size = savedInstanceState.getInt(NUMBER_OF_ITEMS);

                    // Retrieve keys we stored in the instance
                    for (int i = 0; i < size; i++) {

                        String title = savedInstanceState.getString(KEY_OF_INSTANCE + i + "0");
                        String image = savedInstanceState.getString(KEY_OF_INSTANCE + i + "1");
                        String communityID = savedInstanceState.getString(KEY_OF_INSTANCE + i + "2");


                        GroupsItem groupsCard = new GroupsItem(title, image, communityID);

                        communityList.add(groupsCard);

                    }
                    for (int i = 0; i < communityList.size(); i++) {
                        groupsAdapter.notifyItemInserted(i);
                    }

            }


        }else{

            callCommunitiesList();
        }

    }

    private void createRecyclerView() {


        rLayoutManger = new LinearLayoutManager(this);

        recyclerView = findViewById(R.id.groupsRecyclerView);

        recyclerView.setNestedScrollingEnabled(true);

         groupsAdapter= new GroupsAdapter(communityList);
        GroupsInterface itemClickListener = new GroupsInterface() {

            @Override
            public void onJoinClick(int position) {
                communityList.get(position).onJoinClick(position);
                GroupsItem group = communityList.get(position);
                String communityID = group.getGroupID();
                checkIfJoined(communityID);
                groupsAdapter.notifyItemChanged(position);

            }

            @Override
            public void onCommunityClick(int position) {
                communityList.get(position).onCommunityClick(position);
                GroupsItem group = communityList.get(position);
                String communityID = group.getGroupID();
                String tag = group.getTitle();
                String imageUrl = group.getImage();
                Intent intent = new Intent(GroupsActivity.this, ViewACommunity.class);
                intent.putExtra("commId", communityID);
                intent.putExtra("commTag", tag);
                intent.putExtra("userId", userID);
                intent.putExtra("imageURL", imageUrl);
                intent.putExtra("userEmail", userEmail);
                intent.putExtra("NAV_ITEM_ID", selectedItemId );

                startActivity(intent);
            }


        };
        groupsAdapter.setOnItemClickListener(itemClickListener);
        recyclerView.setLayoutManager(rLayoutManger);
        recyclerView.setAdapter(groupsAdapter);




    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {

        int size = communityList  == null ? 0 : communityList.size();
        outState.putInt(NUMBER_OF_ITEMS, size);


        // Need to generate unique key for each item
        // This is only a possible way to do, please find your own way to generate the key
        for (int i = 0; i < size; i++) {

            outState.putString(KEY_OF_INSTANCE + i + "0", communityList.get(i).getTitle());

            outState.putString(KEY_OF_INSTANCE + i + "1", communityList.get(i).getImage());

            outState.putString(KEY_OF_INSTANCE + i + "2", communityList.get(i).getGroupID());


        }

        super.onSaveInstanceState(outState);

    }




}