package edu.northeastern.numad24sp_group4unilink.messages;

import static edu.northeastern.numad24sp_group4unilink.Login.mAuth;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.numad24sp_group4unilink.BaseActivity;
import edu.northeastern.numad24sp_group4unilink.R;
import edu.northeastern.numad24sp_group4unilink.databinding.ActivityMessagesBinding;
import edu.northeastern.numad24sp_group4unilink.messages.adapter.MyChatRecyclerAdapter;
import edu.northeastern.numad24sp_group4unilink.messages.adapter.MyUserRecyclerAdapter;
import edu.northeastern.numad24sp_group4unilink.messages.bean.ChatBean;

public class MessagesActivity extends BaseActivity {

    ActivityMessagesBinding messagesBinding;
    private String formUserEmail;
    private MyUserRecyclerAdapter adapter;
    private List<ChatBean> mList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        messagesBinding = ActivityMessagesBinding.inflate(getLayoutInflater());
        setContentView(messagesBinding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        formUserEmail = mAuth.getCurrentUser().getEmail();


        messagesBinding.btSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = messagesBinding.etSearch.getText().toString();
                String myUser = mAuth.getCurrentUser().getEmail();
                if (email.equals(myUser)) {
                    Toast.makeText(MessagesActivity.this, "This user is themselves", Toast.LENGTH_SHORT).show();
                    return;
                }
                queryUser(email);
            }
        });

        messagesBinding.recyclerview.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyUserRecyclerAdapter(mList,formUserEmail,this);
        messagesBinding.recyclerview.setAdapter(adapter);
        adapter.setOnItemClickListener(new MyUserRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                String email = mList.get(position).getFromUser().equals(formUserEmail) ? mList.get(position).getToUser() : mList.get(position).getFromUser();
                queryUser(email);
            }
        });
        getChatUserList();
        addSnapshotListener();
    }
    private void queryUser(String email){
        FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String email = (String) document.getData().get("email");
                                String firstName = (String) document.getData().get("firstName");
                                String lastName = (String) document.getData().get("lastName");

                                Intent intent = new Intent(MessagesActivity.this, ChatActivity.class);
                                intent.putExtra("email", email);
                                intent.putExtra("firstName", firstName);
                                intent.putExtra("lastName", lastName);
                                startActivity(intent);
                                return;
                            }

                        } else {
                            Toast.makeText(MessagesActivity.this, "No such user", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void getChatUserList() {
        FirebaseFirestore.getInstance().collection("lastChat")
                .where(Filter.or(
                        Filter.equalTo("fromUser", formUserEmail),
                        Filter.equalTo("toUser", formUserEmail)
                ))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            mList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                ChatBean chatBean = new ChatBean();
                                chatBean.setFromUser((String) document.getData().get("fromUser"));
                                chatBean.setToUser((String) document.getData().get("toUser"));
                                chatBean.setContent((String) document.getData().get("content"));
                                chatBean.setTime((Long) document.getData().get("time"));

                                if (chatBean != null) {
                                    mList.add(chatBean);
                                }

                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                });

    }

    private void addSnapshotListener() {
        FirebaseFirestore.getInstance().collection("lastChat")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e("TAG", "listen:error", e);
                            return;
                        }

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                case REMOVED:
                                case MODIFIED:
                                    getChatUserList();
                                    break;
                            }
                        }

                    }
                });
    }
}