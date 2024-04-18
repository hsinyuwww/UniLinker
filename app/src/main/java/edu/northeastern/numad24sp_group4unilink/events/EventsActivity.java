package edu.northeastern.numad24sp_group4unilink.events;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import edu.northeastern.numad24sp_group4unilink.BaseActivity;
import edu.northeastern.numad24sp_group4unilink.R;
import edu.northeastern.numad24sp_group4unilink.databinding.ActivityEventsBinding;


public class EventsActivity extends BaseActivity {

    private ArrayList<EventItem> eventList = new ArrayList<>();
    private RecyclerView recyclerView;

    private EventAdapter eventAdapter;
    private RecyclerView.LayoutManager rLayoutManger;

    private String date, state;

    ActivityEventsBinding activityEventsBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        activityEventsBinding = ActivityEventsBinding.inflate(getLayoutInflater());
        setContentView(activityEventsBinding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



    }

}