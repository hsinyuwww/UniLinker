package edu.northeastern.numad24sp_group4unilink.Attendees;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import edu.northeastern.numad24sp_group4unilink.R;



public class AttendeesAdapter extends RecyclerView.Adapter<AttendeesHolder>{

    private final ArrayList<AttendeesItem> itemList;
    private AttendeesInterface listener;

    public AttendeesAdapter(ArrayList<AttendeesItem> itemList) {
        this.itemList = itemList;

    }

    public void setOnItemClickListener(AttendeesInterface listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public AttendeesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendees, parent, false);
        return new AttendeesHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendeesHolder holder, int position) {
        AttendeesItem currentItem = itemList.get(position);


        holder.userEmail.setText(currentItem.getUserEmail());

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
