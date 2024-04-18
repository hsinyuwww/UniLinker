package edu.northeastern.numad24sp_group4unilink.events;


import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import edu.northeastern.numad24sp_group4unilink.R;
import edu.northeastern.numad24sp_group4unilink.comments.CommentsActivity;


public class EventHolder extends RecyclerView.ViewHolder {
    public ImageView imageEvent;
    public TextView title;
    public TextView location;
    public TextView dateTime;

    public TextView description;
    public TextView eventID;

    public TextView community;

    public Button attend;

    public ImageView comment, delete, edit, attendees;

    public EventHolder(View itemView, final EventInterface listener) {

        super(itemView);
        imageEvent = itemView.findViewById(R.id.eventImage);
        title = itemView.findViewById(R.id.textTitle);
        location = itemView.findViewById(R.id.textLocation);
        dateTime = itemView.findViewById(R.id.textDateTime);
        description = itemView.findViewById(R.id.textDescription);
        community = itemView.findViewById(R.id.textCommunity);
        attend = itemView.findViewById(R.id.buttonAttend);
        edit = itemView.findViewById(R.id.iconEdit);
        delete = itemView.findViewById(R.id.iconDelete);
        comment = itemView.findViewById(R.id.iconComment);
        eventID = itemView.findViewById(R.id.textEventID);
        attendees = itemView.findViewById(R.id.iconAttendees);
        if (imageEvent == null) {
            Log.e("ViewHolder", "ImageView is null");
            // Log more details about the itemView
            Log.d("ViewHolder", "itemView: " + itemView.toString());
            int resId = itemView.getContext().getResources().getIdentifier("eventImage", "id", itemView.getContext().getPackageName());
            Log.d("ViewHolder", "Resource ID found: " + resId);
        } else {
            Log.d("ViewHolder", "ImageView found successfully");
        }

        comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    int position = getLayoutPosition();


                    if (position != RecyclerView.NO_POSITION) {
                        listener.onCommentClick(position);
                        // Start the CommentsActivity

                    }
                }
            }
        });

        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    int position = getLayoutPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onLocationClick(position);
                    }
                }
            }
        });

        attendees.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    int position = getLayoutPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onAttendeesClick(position);
                    }
                }
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    int position = getLayoutPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onEditClick(position);
                    }
                }
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    int position = getLayoutPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onDeleteClick(position);
                    }
                }
            }
        });

        attend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    int position = getLayoutPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onAttendClick(position);
                    }
                }
            }
        });


    }
}
