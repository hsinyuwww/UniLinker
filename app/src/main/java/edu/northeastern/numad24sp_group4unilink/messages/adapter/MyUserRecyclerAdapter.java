package edu.northeastern.numad24sp_group4unilink.messages.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import edu.northeastern.numad24sp_group4unilink.databinding.ItemUserListBinding;
import edu.northeastern.numad24sp_group4unilink.messages.bean.ChatBean;

public class MyUserRecyclerAdapter extends RecyclerView.Adapter<MyUserRecyclerAdapter.ItemView>{
    private List<ChatBean> list;
    Context context;
    private String myEmail;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);

    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public MyUserRecyclerAdapter(List<ChatBean> list,String myEmail, Context context) {
        this.list=list;
        this.context=context;
        this.myEmail = myEmail;
    }

    public void updateData(List<ChatBean> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public ItemView onCreateViewHolder(ViewGroup viewGroup, int i) {
        ItemUserListBinding binding = ItemUserListBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
        return new ItemView(binding);
    }

    @Override
    public void onBindViewHolder(final ItemView itemView, int position) {
        ChatBean info = list.get(position);
        itemView.itemListBinding.tvNickname.setText(myEmail.equals(info.getFromUser())?info.getToUser():info.getFromUser());
        itemView.itemListBinding.tvTime.setText(getStringDateShort(info.getTime()));
        itemView.itemListBinding.tvContent.setText(info.getContent());

        itemView.itemListBinding.llBg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onItemClickListener != null) {
                    int pos = itemView.getLayoutPosition();
                    onItemClickListener.onItemClick(itemView.itemView, pos);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    //viewholder
    public static class ItemView extends  RecyclerView.ViewHolder{

        ItemUserListBinding itemListBinding;
        public ItemView(ItemUserListBinding itemListBinding){
            super(itemListBinding.getRoot());
            this.itemListBinding = itemListBinding;
        }


    }
    public static String getStringDateShort(long time) {
        Date currentTime = new Date(time);
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd HH:mm");
        String dateString = formatter.format(currentTime);
        return dateString;
    }
}