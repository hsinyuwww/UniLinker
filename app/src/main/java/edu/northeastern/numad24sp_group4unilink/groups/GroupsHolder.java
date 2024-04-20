package edu.northeastern.numad24sp_group4unilink.groups;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import edu.northeastern.numad24sp_group4unilink.R;
import edu.northeastern.numad24sp_group4unilink.events.EventInterface;

public class GroupsHolder extends RecyclerView.ViewHolder {
    public ImageView imageCommunity;
    public TextView title, communityID;
    public Button join;


    public GroupsHolder(@NonNull View itemView, final GroupsInterface listener) {
        super(itemView);

        imageCommunity = itemView.findViewById(R.id.imageCommunity);
        title = itemView.findViewById(R.id.communityName);
        communityID = itemView.findViewById(R.id.communityID);
        join = itemView.findViewById(R.id.buttonJoin);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    int position = getLayoutPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onCommunityClick(position);
                    }
                }
            }
        });
        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    int position = getLayoutPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onJoinClick(position);
                    }
                }
            }
        });
    }
}
