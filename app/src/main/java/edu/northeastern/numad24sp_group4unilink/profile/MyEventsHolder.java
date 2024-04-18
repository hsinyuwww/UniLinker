package edu.northeastern.numad24sp_group4unilink.profile;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import edu.northeastern.numad24sp_group4unilink.R;
import edu.northeastern.numad24sp_group4unilink.events.Event;

public class MyEventsHolder extends RecyclerView.ViewHolder {

    TextView eventName, eventDate, eventLocation;
    ImageView eventImage;
    public MyEventsHolder(@NonNull View itemView, ArrayList<Event> myEventsList) {
        super(itemView);
        eventName = itemView.findViewById(R.id.eventName);
        eventDate = itemView.findViewById(R.id.eventDate);
        eventLocation = itemView.findViewById(R.id.eventLocation);
        eventImage = itemView.findViewById(R.id.eventImage);

    }
}
