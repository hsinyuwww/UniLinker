package edu.northeastern.numad24sp_group4unilink.comments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.northeastern.numad24sp_group4unilink.R;


public class CommentsActivity extends AppCompatActivity {

    private ArrayList<CommentsItem> commentList = new ArrayList<>();
    private RecyclerView recyclerView;

    private CommentsAdapter commentsAdapter;
    private FirebaseFirestore db;
    private RecyclerView.LayoutManager rLayoutManger;
    private static final String KEY_OF_INSTANCE = "KEY_OF_INSTANCE";
    private static final String NUMBER_OF_ITEMS = "NUMBER_OF_ITEMS";

    public String userEmail, userID, postID;
    private EditText postComment;
    private Button postCommentButton;

    private ImageView closeButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_comments);
        Intent intent = getIntent();

        userEmail = intent.getStringExtra("userEmail");
        userID =  intent.getStringExtra("userID");
        postID = intent.getStringExtra("postID");



        postComment = findViewById(R.id.commentEditText);
        postCommentButton = findViewById(R.id.postCommentButton);
        closeButton = findViewById(R.id.closeButton);

        postCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String commentText = postComment.getText().toString();
                if (!commentText.isEmpty()) {
                    postComment(commentText);
                } else {
                    Toast.makeText(CommentsActivity.this, "Please enter a comment.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        db = FirebaseFirestore.getInstance();
        init(savedInstanceState);
    }

    private void postComment(String commentText){
        Map<String, Object> commentData = new HashMap<>();
        commentData.put("userId", userID);
        commentData.put("userEmail", userEmail);
        commentData.put("comment", commentText);
        commentData.put("postedDate", new Date());

        // Add the comment data to Firestore
        db.collection("comments").add(commentData)
                .addOnSuccessListener(documentReference -> {
                    String commentId = documentReference.getId(); // Get the ID of the newly created comment

                    // Update the corresponding post document with the comment ID
                    db.collection("posts").document(postID)
                            .update("comments", FieldValue.arrayUnion(commentId))
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Comment posted successfully.", Toast.LENGTH_SHORT).show();
                                CommentsItem commentCard = new CommentsItem(userEmail, commentText);

                                commentList.add(commentCard);
                                commentsAdapter.notifyItemInserted(commentList.size() - 1);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to update post with comment ID.", Toast.LENGTH_SHORT).show();
                                Log.e("UpdatePost", "Error updating post with comment ID: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to post comment. Please try again.", Toast.LENGTH_SHORT).show();
                    Log.e("PostComment", "Error posting comment: " + e.getMessage());
                });

    }
    private void init(Bundle savedInstanceState) {


        createRecyclerView();

        if (savedInstanceState != null ) {
            if (commentList == null || commentList.size() == 0) {

                int size = savedInstanceState.getInt(NUMBER_OF_ITEMS);

                // Retrieve keys we stored in the instance
                for (int i = 0; i < size; i++) {

                    String username = savedInstanceState.getString(KEY_OF_INSTANCE + i + "0");
                    String comment = savedInstanceState.getString(KEY_OF_INSTANCE + i + "1");

                    CommentsItem commentCard = new CommentsItem(username, comment);

                    commentList.add(commentCard);

                }
                for(int i=0;i<commentList.size();i++){
                    commentsAdapter.notifyItemInserted(i);
                }
            }


        }else{

            callCommentsList();
        }





    }

    private void createRecyclerView() {


        rLayoutManger = new LinearLayoutManager(this);

        recyclerView = findViewById(R.id.commentsRecyclerView);

        recyclerView.setNestedScrollingEnabled(true);

        commentsAdapter = new CommentsAdapter(commentList);
        CommentsInterface itemClickListener = new CommentsInterface() {

        };
        commentsAdapter.setOnItemClickListener(itemClickListener);
        recyclerView.setLayoutManager(rLayoutManger);
        recyclerView.setAdapter(commentsAdapter);




    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {

        int size = commentsAdapter  == null ? 0 : commentList.size();
        outState.putInt(NUMBER_OF_ITEMS, size);

        // Need to generate unique key for each item
        // This is only a possible way to do, please find your own way to generate the key
        for (int i = 0; i < size; i++) {

            outState.putString(KEY_OF_INSTANCE + i + "0", commentList.get(i).getUserEmail());

            outState.putString(KEY_OF_INSTANCE + i + "1", commentList.get(i).getComment());

        }

        super.onSaveInstanceState(outState);

    }

    public void callCommentsList(){


        List<String> commentIdsL=new ArrayList<>();

        db.collection("posts").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String documentId = document.getId();
                    if (documentId.equals(postID)) {
                        List<String> comm = (List<String>) document.get("comments");
                        if (comm != null) {
                            commentIdsL.addAll(comm);
                        }
                        break;
                    }
                }
                Log.v( "c ids", "comments ids is " + commentIdsL );

                // Query the users collection to get names for each user ID
                for (String commentId : commentIdsL) {
                    db.collection("comments").document(commentId).get().addOnCompleteListener(commentTask -> {
                        if (commentTask.isSuccessful()) {
                            DocumentSnapshot commentDoc = commentTask.getResult();
                            if (commentDoc.exists()) {
                                String commentText = commentDoc.getString("comment");
                                String author = commentDoc.getString("userEmail");
                                Log.v("Comments", author+" : "+commentText);
                                if (commentText != null && !commentText.isEmpty()) {

                                    CommentsItem commentCard = new CommentsItem(author, commentText);
                                    commentList.add(commentCard);
                                    commentsAdapter.notifyItemInserted(commentList.size() - 1);

                                }
                            }

                        } else {
                            // Handle failure to retrieve comment
                            Log.e("populateSpinnerComm", "Error getting comment: " + commentTask.getException());
                        }
                    });
                }



                Log.v("Size of Comments Array", Integer.toString(commentList.size()));


            } else {
                // Handle failure to retrieve users
                Toast.makeText(getApplicationContext(), "Failed to retrieve comments", Toast.LENGTH_SHORT).show();
            }
        });

    }
}