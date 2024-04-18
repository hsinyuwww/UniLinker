package edu.northeastern.numad24sp_group4unilink.events;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import edu.northeastern.numad24sp_group4unilink.R;

public class EventAdapter extends RecyclerView.Adapter<EventHolder>{

    private final ArrayList<EventItem> itemList;
    private final String EVENTS_TYPE;
    private EventInterface listener;
    public EventAdapter(ArrayList<EventItem> itemList, String EVENTS_TYPE) {
        this.itemList = itemList;
        this.EVENTS_TYPE = EVENTS_TYPE;
    }

    public void setOnItemClickListener(EventInterface listener) {
        this.listener = listener;
    }
    @NonNull
    @Override
    public EventHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventHolder(view, listener, EVENTS_TYPE);
    }

    @Override
    public void onBindViewHolder(EventHolder holder, int position) {

        EventItem currentItem = itemList.get(position);

        String date = currentItem.getDate();
        String time = currentItem.getTime();

        holder.title.setText(currentItem.getTitle());
        holder.location.setText(currentItem.getLocation());
        holder.description.setText(currentItem.getDescription());
        holder.community.setText(currentItem.getCommunity());
        holder.dateTime.setText(date +", "+time);
        holder.eventID.setText(currentItem.getEventID());

        if(EVENTS_TYPE.equals("MY_EVENTS")){

            holder.edit.setVisibility(View.VISIBLE);
            holder.delete.setVisibility(View.VISIBLE);
        }else if(EVENTS_TYPE.equals("ATTENDING_EVENTS")){

            holder.edit.setVisibility(View.GONE);
            holder.delete.setVisibility(View.GONE);
        }else if(EVENTS_TYPE.equals("ALL_EVENTS")){
            holder.edit.setVisibility(View.GONE);
            holder.delete.setVisibility(View.GONE);
        }

        String imageUrl = currentItem.getImage();

        if (imageUrl != null && !imageUrl.isEmpty() && holder.imageEvent != null) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.event) // optional, shows while image is loading
                    .error(R.drawable.error) // optional, shows if cannot load image
                    .into(holder.imageEvent);
            Log.v("Image: ", imageUrl);
        } else {
            // If imageUrl is empty or null, or if ImageView is null, set a default image
            if (holder.imageEvent != null) {
                holder.imageEvent.setImageResource(R.drawable.event);
                Log.v("Inside if: ", "r");// Use some default image or a placeholder
            }else{
                Log.v("Inside else: ", "p");
            }
        }
        /*if (imageUrl != null && imageUrl!="") {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .into(holder.image);
        } else {
            // Handle null case or set a default image
        }*/

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
