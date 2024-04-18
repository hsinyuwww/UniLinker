package edu.northeastern.numad24sp_group4unilink.community;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.northeastern.numad24sp_group4unilink.R;

public class ViewACommunity extends AppCompatActivity {

    private Spinner spinner;
    private Spinner spinnerUsers;
    private FirebaseFirestore db;
    private CollectionReference postsRef;
    private String commTag,commId,currentUserId;

    Map<String, String> userIdToEmailMap;
    Button joinCommButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_acommunity);
        spinner = findViewById(R.id.spinner);
        spinnerUsers= findViewById(R.id.spinnerUsers);

        db = FirebaseFirestore.getInstance();

        // Get the commTag from the intent
        commTag = getIntent().getStringExtra("commTag");
        commId = getIntent().getStringExtra("commId");
        currentUserId= getIntent().getStringExtra("userId");
        TextView userEmailTextView = findViewById(R.id.textView4);
        userEmailTextView.setText("Posts in : " + commTag);


        // Initialize Firestore reference to posts collection
        postsRef = db.collection("posts");

        // Populate spinner with posts based on the commTag
        populateSpinnerWithPosts();
        populateSpinnerUsers();
        joinCommButton = findViewById(R.id.joinComm);
        joinCommButton.setOnClickListener(v -> {
            // Get the community ID you want to join (replace "communityId" with the actual ID)
            String communityId = "communityId";
            // Update the community document in Firestore
            updateCommunityDocument(commId);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void populateSpinnerWithPosts() {
        postsRef.whereEqualTo("tag", commTag).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<String> postTitles = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String title = document.getString("title");
                    if (title != null && !title.isEmpty()) {
                        postTitles.add(title);
                        //fetches the right posts for each community, now all the fileds have to be extracted and displayed using recycler view similar to title!
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, postTitles);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
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

}