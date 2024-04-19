package edu.northeastern.numad24sp_group4unilink;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.coordinatorlayout.widget.CoordinatorLayout;


import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import edu.northeastern.numad24sp_group4unilink.events.CreateEvent;
import edu.northeastern.numad24sp_group4unilink.events.EventsActivity;
import edu.northeastern.numad24sp_group4unilink.groups.CreateGroup;
import edu.northeastern.numad24sp_group4unilink.groups.GroupsActivity;
import edu.northeastern.numad24sp_group4unilink.messages.MessagesActivity;
import edu.northeastern.numad24sp_group4unilink.profile.ProfileActivity;
import edu.northeastern.numad24sp_group4unilink.profile.UpdateProfileActivity;

public abstract class BaseActivity extends AppCompatActivity{

    CoordinatorLayout coordinatorLayout;
    BottomNavigationView navigationView;
    public static boolean isLoggedInUser;
    @Override
    public void setContentView(View view){
        coordinatorLayout = (CoordinatorLayout) getLayoutInflater().inflate(R.layout.activity_base, null);
        FrameLayout container = coordinatorLayout.findViewById(R.id.activityContainer);
        container.addView(view);
        super.setContentView(coordinatorLayout);

        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.primaryDarkColor)));

        navigationView = coordinatorLayout.findViewById(R.id.bottomNavigationView);
        navigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.home) {
                // Handle home action
                openHome(itemId);

            } else if (itemId == R.id.events) {
                // Handle events action
                openEvents(itemId);

            } else if (itemId == R.id.fab) {
                View menuItemView = findViewById(R.id.fab);
                openAdd(menuItemView);

            } else if (itemId == R.id.groups) {
                // Handle groups action
                openGroups(itemId);

            } else if (itemId == R.id.profile) {
                // Handle profile action
                openProfile(itemId);

            }
            item.setChecked(true); // Ensure the item is visually marked as selected.
            return true; // return true to indicate you've handled the selection
        });


    }

    public void openAdd(View view){
        PopupMenu popup = new PopupMenu(BaseActivity.this, view);
        popup.getMenuInflater().inflate(R.menu.add_menu, popup.getMenu());

        // Setting the onClick Listener for the menu items
        popup.setOnMenuItemClickListener(item -> {

            int itemId = item.getItemId();
            if (itemId == R.id.addEvent) {
                Intent intent = new Intent(this, CreateEvent.class);
                String userEmail = getIntent().getStringExtra("userEmail");
                String userId= getIntent().getStringExtra("userID");
                intent.putExtra("NAV_ITEM_ID", itemId);
                intent.putExtra("userEmail", userEmail); // Pass the user's email address to CreatePost activity
                intent.putExtra("userID", userId);
                intent.putExtra("EVENTS_TYPE","ALL_EVENTS" );
                startActivity(intent);
            } else if (itemId == R.id.addGroup) {
                intentHelper(CreateGroup.class);
                Intent intent = new Intent(this, CreateGroup.class);
                String userEmail = getIntent().getStringExtra("userEmail");
                String userId= getIntent().getStringExtra("userID");
                intent.putExtra("userEmail", userEmail); // Pass the user's email address to CreatePost activity
                intent.putExtra("userID", userId);
                intent.putExtra("NAV_ITEM_ID", itemId);
                startActivity(intent);

            }
            return true;

        });

        // Showing the popup menu
        popup.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int itemId = item.getItemId();
        if (itemId == R.id.messages) {
            Intent intent = new Intent(this, MessagesActivity.class);
            String userEmail = getIntent().getStringExtra("userEmail");
            String userId= getIntent().getStringExtra("userID");
            intent.putExtra("userEmail", userEmail);
            intent.putExtra("userID", userId);
            intent.putExtra("NAV_ITEM_ID", itemId);
            startActivity(intent);

        } else if (itemId == R.id.logout) {

            AlertDialog.Builder builder = new AlertDialog.Builder(BaseActivity.this);
            builder.setTitle("Logout").setMessage("Are you sure you want to logout?").setCancelable(false);
            builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                FirebaseAuth.getInstance().signOut();
                finish();
                intentHelper(Login.class);
                Toast.makeText(BaseActivity.this, "Logged out successfully!", Toast.LENGTH_SHORT).show();
            });
            builder.setNegativeButton(android.R.string.no, null);
            builder.show();

        }
        return true;

    }
    public void openHome(int itemId){

        Intent intent = new Intent(this, MainActivity.class);
        String userEmail = getIntent().getStringExtra("userEmail");
        String userId= getIntent().getStringExtra("userID");
        intent.putExtra("userEmail", userEmail); // Pass the user's email address to CreatePost activity
        intent.putExtra("userID", userId);
        intent.putExtra("NAV_ITEM_ID", itemId);
        intent.putExtra("EVENTS_TYPE","ALL_EVENTS" );
        startActivity(intent);
    }



    public void openEvents(int itemId){
        Intent intent = new Intent(this, EventsActivity.class);
        String userEmail = getIntent().getStringExtra("userEmail");
        String userId= getIntent().getStringExtra("userID");
        intent.putExtra("userEmail", userEmail); // Pass the user's email address to CreatePost activity
        intent.putExtra("userID", userId);
        intent.putExtra("NAV_ITEM_ID", itemId);
        intent.putExtra("EVENTS_TYPE","MY_EVENTS" );
        startActivity(intent);
    }

    public void openGroups(int itemId){

        Intent intent = new Intent(this, GroupsActivity.class);
        String userEmail = getIntent().getStringExtra("userEmail");
        String userId= getIntent().getStringExtra("userID");
        intent.putExtra("userEmail", userEmail);
        intent.putExtra("userID", userId);
        intent.putExtra("NAV_ITEM_ID", itemId);
        startActivity(intent);
    }

    public void openProfile(int itemId){
        isLoggedInUser = true;
        Intent intent = new Intent(this, ProfileActivity.class);
        String userEmail = getIntent().getStringExtra("userEmail");
        String userId= getIntent().getStringExtra("userID");
        intent.putExtra("userEmail", userEmail);
        intent.putExtra("email", userEmail);
        intent.putExtra("userID", userId);
        intent.putExtra("NAV_ITEM_ID", itemId);
        startActivity(intent);
    }

    private void intentHelper(Class<?> activityClass){
        Intent intent = new Intent(getApplicationContext(), activityClass);
        startActivity(intent); // starts new activity
    }

}