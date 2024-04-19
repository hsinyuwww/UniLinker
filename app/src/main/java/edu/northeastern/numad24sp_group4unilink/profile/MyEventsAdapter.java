package edu.northeastern.numad24sp_group4unilink.profile;

import static edu.northeastern.numad24sp_group4unilink.profile.ProfileActivity.email;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import edu.northeastern.numad24sp_group4unilink.Login;
import edu.northeastern.numad24sp_group4unilink.R;
import edu.northeastern.numad24sp_group4unilink.events.Event;
import edu.northeastern.numad24sp_group4unilink.events.ViewEventActivity;

public class MyEventsAdapter extends RecyclerView.Adapter<MyEventsHolder> {
    private final ArrayList<Event> eventsList;
    private Context context;

    public MyEventsAdapter(Context context, ArrayList<Event> eventsList){
        this.context = context;
        this.eventsList = eventsList;
    }

    @NonNull
    @Override
    public MyEventsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View inflator = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_myevents, parent, false);
        return new MyEventsHolder(inflator, eventsList);
    }

    @Override
    public void onBindViewHolder(@NonNull MyEventsHolder holder, int position) {
        Event currentEvent = eventsList.get(position);
        holder.eventName.setText(currentEvent.getTitle());
        holder.eventLocation.setText(currentEvent.getLocation());
        holder.eventDate.setText(currentEvent.getEventDate().toString());

        String imageUrl = currentEvent.getPicture();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .into(holder.eventImage);
        }else{
            Glide.with((context)).load(R.drawable.event).into(holder.eventImage);
        }

        holder.itemView.setOnClickListener(v -> {
            int position1 = holder.getAdapterPosition();
            if (position1 != RecyclerView.NO_POSITION){
                Event clickedEvent = eventsList.get(position1);
                Intent intent = new Intent(context, ViewEventActivity.class);
                intent.putExtra("postId", clickedEvent.getDocumentId());
                intent.putExtra("userEmail", email);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventsList.size();
    }
}

