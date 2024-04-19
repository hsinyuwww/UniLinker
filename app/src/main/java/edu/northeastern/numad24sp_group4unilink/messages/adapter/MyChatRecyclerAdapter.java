package edu.northeastern.numad24sp_group4unilink.messages.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import edu.northeastern.numad24sp_group4unilink.databinding.ItemChatListBinding;
import edu.northeastern.numad24sp_group4unilink.messages.bean.ChatBean;

public class MyChatRecyclerAdapter extends RecyclerView.Adapter<MyChatRecyclerAdapter.ItemView>{
    private List<ChatBean> list;
    private Context context;
    private String fromUser;
    private String toUser;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);

    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public MyChatRecyclerAdapter(List<ChatBean> list, String fromUser, String toUser, Context context) {
        this.list=list;
        this.context=context;
        this.fromUser = fromUser;
        this.toUser = toUser;
    }

    public void updateData(List<ChatBean> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public ItemView onCreateViewHolder(ViewGroup viewGroup, int i) {
        ItemChatListBinding binding = ItemChatListBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
        return new ItemView(binding);
    }

    @Override
    public void onBindViewHolder(final ItemView itemView, int position) {
        ChatBean info = list.get(position);
        itemView.itemListBinding.tvTime.setText(getStringDateShort(info.getTime()));

        if (info.getFromUser().equals(fromUser)) {
            itemView.itemListBinding.receive.setVisibility(View.GONE);
            itemView.itemListBinding.send.setVisibility(View.VISIBLE);
            itemView.itemListBinding.tvSend.setText(info.getContent());
        }else {
            itemView.itemListBinding.receive.setVisibility(View.VISIBLE);
            itemView.itemListBinding.send.setVisibility(View.GONE);
            itemView.itemListBinding.tvReceive.setText(info.getContent());
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ItemView extends  RecyclerView.ViewHolder{

        ItemChatListBinding itemListBinding;
        public ItemView(ItemChatListBinding itemListBinding){
            super(itemListBinding.getRoot());
            this.itemListBinding = itemListBinding;
        }


    }

    public static String getStringDateShort(long time) {
        Date currentTime = new Date(time);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(currentTime);
        return dateString;
    }
}