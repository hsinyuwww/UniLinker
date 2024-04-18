package edu.northeastern.numad24sp_group4unilink.comments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import edu.northeastern.numad24sp_group4unilink.R;
import edu.northeastern.numad24sp_group4unilink.events.EventHolder;
import edu.northeastern.numad24sp_group4unilink.events.EventInterface;
import edu.northeastern.numad24sp_group4unilink.events.EventItem;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsHolder>{

    private final ArrayList<CommentsItem> itemList;
    private CommentsInterface listener;
    public CommentsAdapter(ArrayList<CommentsItem> itemList) {
        this.itemList = itemList;
    }

    public void setOnItemClickListener(CommentsInterface listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CommentsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentsHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentsHolder holder, int position) {
        CommentsItem currentItem = itemList.get(position);


        holder.userEmail.setText(currentItem.getUserEmail());
        holder.comment.setText(currentItem.getComment());

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
