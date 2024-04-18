package edu.northeastern.numad24sp_group4unilink.Attendees;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.northeastern.numad24sp_group4unilink.R;
import edu.northeastern.numad24sp_group4unilink.comments.CommentsAdapter;
import edu.northeastern.numad24sp_group4unilink.comments.CommentsInterface;
import edu.northeastern.numad24sp_group4unilink.comments.CommentsItem;

public class AttendeesActivity extends AppCompatActivity {

    private ArrayList<AttendeesItem> attendeesList = new ArrayList<>();
    private RecyclerView recyclerView;

    private AttendeesAdapter attendeesAdapter;
    private FirebaseFirestore db;
    private RecyclerView.LayoutManager rLayoutManger;
    private static final String KEY_OF_INSTANCE = "KEY_OF_INSTANCE";
    private static final String NUMBER_OF_ITEMS = "NUMBER_OF_ITEMS";

    public String userEmail, userID, postID;

    private ImageView closeButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_attendees);
        Intent intent = getIntent();

        userEmail = intent.getStringExtra("userEmail");
        userID =  intent.getStringExtra("userID");
        postID = intent.getStringExtra("postID");

        closeButton = findViewById(R.id.closeButton);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        db = FirebaseFirestore.getInstance();
        init(savedInstanceState);
    }

    private void init(Bundle savedInstanceState) {


        createRecyclerView();

        if (savedInstanceState != null ) {
            if (attendeesList == null || attendeesList.size() == 0) {

                int size = savedInstanceState.getInt(NUMBER_OF_ITEMS);

                // Retrieve keys we stored in the instance
                for (int i = 0; i < size; i++) {

                    String username = savedInstanceState.getString(KEY_OF_INSTANCE + i + "0");

                    AttendeesItem attendeesCard = new AttendeesItem(username);

                    attendeesList.add(attendeesCard);

                }
                for(int i=0;i<attendeesList.size();i++){
                    attendeesAdapter.notifyItemInserted(i);
                }
            }


        }else{

            callAttendeesList();
        }





    }


    private void createRecyclerView() {


        rLayoutManger = new LinearLayoutManager(this);

        recyclerView = findViewById(R.id.attendeesRecyclerView);

        recyclerView.setNestedScrollingEnabled(true);

        attendeesAdapter = new AttendeesAdapter(attendeesList);
        AttendeesInterface itemClickListener = new AttendeesInterface() {

        };
        attendeesAdapter.setOnItemClickListener(itemClickListener);
        recyclerView.setLayoutManager(rLayoutManger);
        recyclerView.setAdapter(attendeesAdapter);




    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {

        int size = attendeesAdapter  == null ? 0 : attendeesList.size();
        outState.putInt(NUMBER_OF_ITEMS, size);

        // Need to generate unique key for each item
        // This is only a possible way to do, please find your own way to generate the key
        for (int i = 0; i < size; i++) {

            outState.putString(KEY_OF_INSTANCE + i + "0", attendeesList.get(i).getUserEmail());


        }

        super.onSaveInstanceState(outState);

    }

    public void callAttendeesList(){

        List<String> userIdsL=new ArrayList<>();

        db.collection("posts").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String documentId = document.getId();
                    if (documentId.equals(postID)) {
                        List<String> users = (List<String>) document.get("attendees");
                        if (users != null) {
                            userIdsL.addAll(users);
                        }
                        break;
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
                                //sO here im only taking the id and email right, email form user, you have to take all attributes like photo when making recycler view for more info if you want

                                //TAKE MORE THAN JUST EMAIL, save as a user and use a RECYCLER VIEW HERE
                                Log.v("ids-name",name);
                                if (name != null && !name.isEmpty()) {
                                    // Save the mapping of userID to name for later use
                                    AttendeesItem attendeesCard = new AttendeesItem(name);
                                    attendeesList.add(attendeesCard);
                                    attendeesAdapter.notifyItemInserted(attendeesList.size() - 1);
                                    Log.v("Attendees : ",name);
                                }
                            }

                        } else {
                            // Handle failure to retrieve user's name
                            Log.e("populateSpinnerUsers", "Error getting user's name: " + userTask.getException());
                        }
                    });
                }



            } else {
                // Handle failure to retrieve users
                Toast.makeText(getApplicationContext(), "Failed to retrieve users", Toast.LENGTH_SHORT).show();
            }
        });



    }
}