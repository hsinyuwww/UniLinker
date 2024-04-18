package edu.northeastern.numad24sp_group4unilink.Attendees;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import edu.northeastern.numad24sp_group4unilink.R;

public class AttendeesHolder extends RecyclerView.ViewHolder{
    public TextView userEmail;
    public AttendeesHolder(@NonNull View itemView, AttendeesInterface listener) {
        super(itemView);
        userEmail = itemView.findViewById(R.id.userEmailTextView);
    }
}
