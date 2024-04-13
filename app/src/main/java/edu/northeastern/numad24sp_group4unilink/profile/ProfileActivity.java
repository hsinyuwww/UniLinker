package edu.northeastern.numad24sp_group4unilink.profile;

import static edu.northeastern.numad24sp_group4unilink.Login.loggedInUser;
import static edu.northeastern.numad24sp_group4unilink.Login.mAuth;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import edu.northeastern.numad24sp_group4unilink.BaseActivity;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import edu.northeastern.numad24sp_group4unilink.Login;
import edu.northeastern.numad24sp_group4unilink.R;
import edu.northeastern.numad24sp_group4unilink.databinding.ActivityProfileBinding;

public class ProfileActivity extends BaseActivity {

    ActivityProfileBinding activityProfileBinding;
    private TextView firstName, lastName;
    private TextView aboutContent, genderContent, levelContent, emailContent;
    private FirebaseFirestore userDB;
    private FirebaseUser currentUser;
    private boolean isDialogOpen;
    static public String currentUserDocId;

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

            userDB = FirebaseFirestore.getInstance();
            currentUser = mAuth.getCurrentUser();

            setProfileDataUI();
            setupExpandableInfoListeners();

        //ImageButton updateProfile = findViewById(R.id.editProfileId);

        if(loggedInUser == currentUser){
            hamburgerMenuFunctionality();
        }

        if(savedInstanceState != null){
            isDialogOpen = savedInstanceState.getBoolean("isDialogOpen");
            if(isDialogOpen){
                showDeleteDialog();
            }
        }

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isDialogOpen", isDialogOpen);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setProfileDataUI();
    }

    private void setupExpandableInfoListeners() {
        TextView aboutHeader = findViewById(R.id.aboutHeaderID);
        aboutContent = findViewById(R.id.aboutContentID);
        TextView genderHeader = findViewById(R.id.genderHeaderID);
        genderContent = findViewById(R.id.genderContentID);
        TextView levelHeader = findViewById(R.id.levelHeaderID);
        levelContent = findViewById(R.id.levelContentID);
        TextView emailHeader = findViewById(R.id.emailHeaderID);
        emailContent = findViewById(R.id.emailContentID);

        aboutHeader.setOnClickListener(v -> contentVisibilityHelper(aboutContent));
        genderHeader.setOnClickListener(v -> contentVisibilityHelper(genderContent));
        levelHeader.setOnClickListener(v -> contentVisibilityHelper(levelContent));
        emailHeader.setOnClickListener(v -> contentVisibilityHelper(emailContent));
    }
    private void contentVisibilityHelper(TextView content){
        if(content.getVisibility() == View.VISIBLE){
            content.setVisibility(View.GONE);
        } else{
            content.setVisibility(View.VISIBLE);
        }
    }

    private void setProfileDataUI() {
        firstName = findViewById(R.id.firstNameId);
        lastName = findViewById(R.id.lastNameId);

        if(currentUser!= null){

            CollectionReference usersCollection = userDB.collection("users");
            Query query = usersCollection.whereEqualTo("userID", currentUser.getUid());

            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                            String fName = document.getString("firstName");
                            String lName = document.getString("lastName");
                            String about = document.getString("about");
                            String gender = document.getString("gender");
                            String level = document.getString("level");
                            String email = document.getString("email");

                            firstName.setText(fName);
                            lastName.setText(lName);
                            aboutContent.setText(about);
                            genderContent.setText(gender);
                            levelContent.setText(level);
                            emailContent.setText(email);

                            // stores the current user's document ID for future use
                            currentUserDocId = document.getId();
                        } else {
                            Log.e("Firebase", "No such document exists.");
                        }

                    } else {
                        Log.w("Firebase", "Error getting user document", task.getException());
                    }
                }

    });
        }
    }

    private void hamburgerMenuFunctionality() {
        ImageView hamBurgerBtn = findViewById(R.id.hamBurgerId);
        hamBurgerBtn.setVisibility(View.VISIBLE);

        PopupMenu popupMenu = new PopupMenu(this, hamBurgerBtn);
        popupMenu.getMenuInflater().inflate(R.menu.hamburger_profile_menu, popupMenu.getMenu());
        
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.editProfile){
                    editProfile();
                    return true;
                    
                } else if (item.getItemId() == R.id.deleteProfile) {
                    showDeleteDialog();
                    return true;
                } else{
                    return false;
                }
            }
        });
        hamBurgerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.show();
            }
        });

    }

    private void editProfile() {
                Intent intent = new Intent(ProfileActivity.this, UpdateProfileActivity.class);
                Log.d("Profile", "First name:" + firstName.getText().toString());
                intent.putExtra("firstName", firstName.getText().toString());
                intent.putExtra("lastName", lastName.getText().toString());
                intent.putExtra("about", aboutContent.getText().toString());
                intent.putExtra("gender", genderContent.getText().toString());
                intent.putExtra("level", levelContent.getText().toString());
                startActivity(intent); // starts new activity
    }

    private void showDeleteDialog(){
        isDialogOpen = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setTitle("Delete Profile");
        builder.setMessage("Are you sure you want to delete your UniLink account??");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isDialogOpen = false;
                deleteProfile();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isDialogOpen = false;
                dialog.dismiss();
            }
        });
        builder.create().show();

    }

    private void deleteProfile() {

        DocumentReference userDoc = userDB.collection("users").document(currentUserDocId);
        userDoc.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                if (currentUser != null){
                    currentUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Log.d("FireAuth", "Account deletion:Success");
                                mAuth.signOut();
                                finish();
                                Intent intent = new Intent(getApplicationContext(), Login.class);
                                startActivity(intent);
                                Toast.makeText(ProfileActivity.this, "User account deleted.", Toast.LENGTH_SHORT).show();
                            } else{
                                Log.w("FireAuth", "Account deletion:Failure" + task.getException());
                                Toast.makeText(ProfileActivity.this, "Failed to delete account.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("Firebase", "Profile deletion:Failure" + e);
            }
        });
    }


}
