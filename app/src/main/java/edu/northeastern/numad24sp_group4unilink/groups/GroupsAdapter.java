package edu.northeastern.numad24sp_group4unilink.groups;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import edu.northeastern.numad24sp_group4unilink.R;
import edu.northeastern.numad24sp_group4unilink.events.EventHolder;
import edu.northeastern.numad24sp_group4unilink.events.EventInterface;
import edu.northeastern.numad24sp_group4unilink.events.EventItem;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsHolder>{
    private final ArrayList<GroupsItem> itemList;
    private GroupsInterface listener;
    public GroupsAdapter(ArrayList<GroupsItem> itemList) {
        this.itemList = itemList;
    }

    public void setOnItemClickListener(GroupsInterface listener) {
        this.listener = listener;
    }


    @NonNull
    @Override
    public GroupsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
        return new GroupsHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupsHolder holder, int position) {

        GroupsItem currentItem = itemList.get(position);

        holder.title.setText(currentItem.getTitle());

        holder.communityID.setText(currentItem.getGroupID());



        String imageUrl = currentItem.getImage();

        if (imageUrl != null && !imageUrl.isEmpty() && holder.imageCommunity != null) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.event) // optional, shows while image is loading
                    .error(R.drawable.error) // optional, shows if cannot load image
                    .into(holder.imageCommunity);
            Log.v("Image: ", imageUrl);
        } else {
            // If imageUrl is empty or null, or if ImageView is null, set a default image
            if (holder.imageCommunity != null) {
                holder.imageCommunity.setImageResource(R.drawable.event);
                Log.v("Inside if: ", "r");// Use some default image or a placeholder
            }else{
                Log.v("Inside else: ", "p");
            }
        }


    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
