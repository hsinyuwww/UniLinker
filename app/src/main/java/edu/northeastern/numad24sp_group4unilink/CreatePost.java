package edu.northeastern.numad24sp_group4unilink;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import edu.northeastern.numad24sp_group4unilink.databinding.ActivityCreatePostBinding;

public class CreatePost extends BaseActivity {

    ActivityCreatePostBinding activityCreatePostBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        activityCreatePostBinding = ActivityCreatePostBinding.inflate(getLayoutInflater());
        setContentView(activityCreatePostBinding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}