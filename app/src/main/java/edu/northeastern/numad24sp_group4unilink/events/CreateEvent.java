package edu.northeastern.numad24sp_group4unilink.events;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import edu.northeastern.numad24sp_group4unilink.BaseActivity;
import edu.northeastern.numad24sp_group4unilink.R;
import edu.northeastern.numad24sp_group4unilink.databinding.ActivityCreateEventBinding;


public class CreateEvent extends BaseActivity {

    ActivityCreateEventBinding activityCreateEventBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        activityCreateEventBinding = ActivityCreateEventBinding.inflate(getLayoutInflater());
        setContentView(activityCreateEventBinding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}