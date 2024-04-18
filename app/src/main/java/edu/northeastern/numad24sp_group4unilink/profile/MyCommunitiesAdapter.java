package edu.northeastern.numad24sp_group4unilink.profile;

import static edu.northeastern.numad24sp_group4unilink.Login.loggedInUser;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import edu.northeastern.numad24sp_group4unilink.R;
import edu.northeastern.numad24sp_group4unilink.community.Community;
import edu.northeastern.numad24sp_group4unilink.community.ViewACommunity;

public class MyCommunitiesAdapter extends RecyclerView.Adapter<MyCommunitiesHolder> {
    private ArrayList<Community> communitiesList;
    private Context context;

    public MyCommunitiesAdapter(Context context, ArrayList<Community> communitiesList){
        this.context = context;
        this.communitiesList = communitiesList;
    }

    @NonNull
    @Override
    public MyCommunitiesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View inflator = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_mycommunities, parent, false);
        return new MyCommunitiesHolder(inflator, communitiesList);
    }

    @Override
    public void onBindViewHolder(@NonNull MyCommunitiesHolder holder, int position) {
        Community currentCommunity = communitiesList.get(position);
        holder.communityName.setText(currentCommunity.getTag());

        String imageUrl = currentCommunity.getpicture();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .into(holder.communityImage);
        } else{
            Glide.with((context)).load(R.drawable.community).into(holder.communityImage);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION){
                    Community clickedComm = communitiesList.get(position);
                    Intent intent = new Intent(context, ViewACommunity.class);
                    intent.putExtra("commTag", clickedComm.getTag());
                    intent.putExtra("commId", clickedComm.getId());
                    intent.putExtra("userId", loggedInUser.getUid());
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return communitiesList.size();
    }
}

