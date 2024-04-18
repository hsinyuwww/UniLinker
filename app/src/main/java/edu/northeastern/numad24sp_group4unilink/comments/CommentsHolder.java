package edu.northeastern.numad24sp_group4unilink.comments;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import edu.northeastern.numad24sp_group4unilink.R;

public class CommentsHolder extends RecyclerView.ViewHolder{

    public TextView userEmail;
    public TextView comment;
    public CommentsHolder(@NonNull View itemView, CommentsInterface listener) {
        super(itemView);
        userEmail = itemView.findViewById(R.id.usernameTextView);
        comment = itemView.findViewById(R.id.commentTextView);
    }
}
