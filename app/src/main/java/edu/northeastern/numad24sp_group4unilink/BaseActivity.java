package edu.northeastern.numad24sp_group4unilink;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
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

public abstract class BaseActivity extends AppCompatActivity{

    CoordinatorLayout coordinatorLayout;
    BottomNavigationView navigationView;
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
                openHome();
            } else if (itemId == R.id.events) {
                // Handle events action
                openEvents();
            } else if (itemId == R.id.fab) {
                View menuItemView = findViewById(R.id.fab);
                openAdd(menuItemView);
            } else if (itemId == R.id.groups) {
                // Handle groups action
                openGroups();
            } else if (itemId == R.id.profile) {
                // Handle profile action
                openProfile();
            }
            return true; // return true to indicate you've handled the selection
        });


    }

    public void openAdd(View view){
        PopupMenu popup = new PopupMenu(BaseActivity.this, view);
        popup.getMenuInflater().inflate(R.menu.add_menu, popup.getMenu());

        // Setting the onClick Listener for the menu items
        popup.setOnMenuItemClickListener(item -> {

            int itemId = item.getItemId();
            if (itemId == R.id.addPost) {
                Intent intent = new Intent(this, CreatePost.class);
                startActivity(intent);

            } else if (itemId == R.id.addEvent) {
                // Handle events action
                Intent intent = new Intent(this, CreateEvent.class);
                startActivity(intent);

            } else if (itemId == R.id.addGroup) {
                // Handle add action
                Intent intent = new Intent(this, CreateGroup.class);
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
            intentHelper(MessagesActivity.class);

        } else if (itemId == R.id.logout) {

            FirebaseAuth.getInstance().signOut();
            finish();
            intentHelper(Login.class);
            Toast.makeText(BaseActivity.this, "Logged out successfully!", Toast.LENGTH_SHORT).show();

        }
        return true;

    }
    public void openHome(){
        intentHelper(MainActivity.class);
    }

    public void openEvents(){
        intentHelper(EventsActivity.class);
    }

    public void openGroups(){
        intentHelper(GroupsActivity.class);
    }

    public void openProfile(){
        intentHelper(ProfileActivity.class);
    }

    private void intentHelper(Class<?> activityClass){
        Intent intent = new Intent(getApplicationContext(), activityClass);
        startActivity(intent); // starts new activity
    }
}