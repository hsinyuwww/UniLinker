package edu.northeastern.numad24sp_group4unilink.profile;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import edu.northeastern.numad24sp_group4unilink.R;
import edu.northeastern.numad24sp_group4unilink.community.Community;

public class MyCommunitiesHolder extends RecyclerView.ViewHolder {

    TextView communityName;
    ImageView communityImage;
    public MyCommunitiesHolder(@NonNull View itemView, ArrayList<Community> myCommunitiesList) {
        super(itemView);
        communityName = itemView.findViewById(R.id.communityName);
        communityImage = itemView.findViewById(R.id.communityImage);

    }
}
