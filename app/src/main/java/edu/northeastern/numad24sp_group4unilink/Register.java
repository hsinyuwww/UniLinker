package edu.northeastern.numad24sp_group4unilink;

import static edu.northeastern.numad24sp_group4unilink.Login.mAuth;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Register extends AppCompatActivity {

    private String email, password, fName, lName;
    private EditText emailText, passwordText, firstNameText, lastNameText;
    private ProgressBar progressBar;
    private FirebaseFirestore userDB;
    CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.primaryDarkColor)));

        userDB = FirebaseFirestore.getInstance();

        emailText = findViewById(R.id.editEmail);
        passwordText = findViewById(R.id.editPassword);
        firstNameText = findViewById(R.id.editFirstName);
        lastNameText = findViewById(R.id.editLastName);
        progressBar = findViewById(R.id.registerProgressBarId);

        final Button registerButton = findViewById(R.id.buttonRegister);
        final TextView loginNow = findViewById(R.id.loginNow);

        loginNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = emailText.getText().toString().trim();
                password = passwordText.getText().toString().trim();
                fName = firstNameText.getText().toString().trim();
                lName = lastNameText.getText().toString().trim();

                if(TextUtils.isEmpty(email)){
                    emailText.setError("Email is required");
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    passwordText.setError("Password is required");
                    return;
                }
                if(TextUtils.isEmpty(fName)){
                    firstNameText.setError("First name is required");
                    return;
                }
                if(TextUtils.isEmpty(lName)){
                    lastNameText.setError("Last name is required");
                    return;
                }
                if(!TextUtils.isEmpty(email) && !email.endsWith("@northeastern.edu")){
                    Toast.makeText(Register.this, "Register with your Northeastern email id.", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressBar.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {
                                        User user = new User(email, password);
                                        storeUserData();
                                        Log.d("FireAuth", "createUserWithEmail:success");
                                        Toast.makeText(Register.this, "Registration is successful.", Toast.LENGTH_SHORT).show();
                                        login();
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w("FireAuth", "createUserWithEmail:failure", task.getException());
                                        if(task.getException() instanceof FirebaseAuthWeakPasswordException){
                                            // Handle weak password exception
                                            Toast.makeText(Register.this, "Invalid password!! Password should be at least 6 characters.",
                                                    Toast.LENGTH_SHORT).show();
                                        } else if(task.getException() instanceof FirebaseAuthUserCollisionException){
                                            // Handle email already exists exception
                                            Toast.makeText(Register.this, "The email address is already in use by another account.",
                                                    Toast.LENGTH_SHORT).show();
                                        } else{
                                            Toast.makeText(Register.this, "Authentication failed.",
                                                    Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                }
                            });

            }
        });
    }

    private void storeUserData() {
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("password", password);
        user.put("firstName", fName);
        user.put("lastName", lName);
        user.put("userID", mAuth.getCurrentUser().getUid());

        userDB.collection("users").add(user).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.d("Firebase", "userAddedInDB:success" + documentReference.getId());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("Firebase", "userAddedInDB:failure", e);
            }
        });
    }

    public void login() {
        Intent intent = new Intent(Register.this, Login.class);
        startActivity(intent);
    }
}

