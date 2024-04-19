package edu.northeastern.numad24sp_group4unilink.messages;


import static edu.northeastern.numad24sp_group4unilink.Login.mAuth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.core.OrderBy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.northeastern.numad24sp_group4unilink.R;
import edu.northeastern.numad24sp_group4unilink.databinding.ActivityChatBinding;
import edu.northeastern.numad24sp_group4unilink.messages.adapter.MyChatRecyclerAdapter;
import edu.northeastern.numad24sp_group4unilink.messages.bean.ChatBean;

public class ChatActivity extends AppCompatActivity {

    protected ActionBar actionBar;
    private MyChatRecyclerAdapter adapter;
    private List<ChatBean> mList = new ArrayList<>();
    private ActivityChatBinding binding;
    private String sendUserEmail = "";
    private String formUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sendUserEmail = getIntent().getStringExtra("email");
        String firstName = getIntent().getStringExtra("firstName");
        String lastName = getIntent().getStringExtra("lastName");
        actionBar =  getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(firstName + " " + lastName);
            actionBar.setSubtitle(sendUserEmail);
        }

        formUserEmail = mAuth.getCurrentUser().getEmail();

        binding.recyclerview.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyChatRecyclerAdapter(mList,formUserEmail,sendUserEmail,this);
        binding.recyclerview.setAdapter(adapter);
        try {
            binding.recyclerview.smoothScrollToPosition(adapter.getItemCount() - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        getChatHistory();
        addSnapshotListener();

        binding.btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txt = binding.etInput.getText().toString();
                if (TextUtils.isEmpty(txt)) {
                    Toast.makeText(ChatActivity.this, "Please enter the content", Toast.LENGTH_SHORT).show();
                    return;
                }
                binding.etInput.setText("");
                int isBig = sendUserEmail.compareTo(formUserEmail);
                String documentId = formUserEmail+"+"+ sendUserEmail;
                if (isBig > 0) {
                    documentId = sendUserEmail + "+" + formUserEmail;
                }

                ChatBean bean = new ChatBean();
                bean.setToUser(sendUserEmail);
                bean.setFromUser(formUserEmail);
                bean.setContent(txt);
                bean.setMark(documentId);
                bean.setTime(System.currentTimeMillis());
                mList.add(bean);
                adapter.notifyDataSetChanged();
                try {
                    binding.recyclerview.smoothScrollToPosition(adapter.getItemCount() - 1);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String finalDocumentId = documentId;
                FirebaseFirestore.getInstance().collection("chat").add(bean).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("Firebase", "userAddedInDB:success" + documentReference.getId());

                        FirebaseFirestore.getInstance().collection("lastChat").document(finalDocumentId)
                                .set(bean)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Firebase", "userAddedInDB:failure", e);
                        Toast.makeText(ChatActivity.this, "Send Failed!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void addSnapshotListener() {
        FirebaseFirestore.getInstance().collection("chat")
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
                                    if (dc.getDocument().getData().get("fromUser").equals(sendUserEmail)&&dc.getDocument().getData().get("toUser").equals(formUserEmail)) {
                                        ChatBean chatBean = new ChatBean();
                                        QueryDocumentSnapshot document = dc.getDocument();
                                        chatBean.setFromUser((String) document.getData().get("fromUser"));
                                        chatBean.setToUser((String) document.getData().get("toUser"));
                                        chatBean.setContent((String) document.getData().get("content"));
                                        chatBean.setTime((Long) document.getData().get("time"));

                                        if (chatBean != null) {
                                            mList.add(chatBean);
                                        }
                                        adapter.notifyDataSetChanged();
                                        try {
                                            binding.recyclerview.smoothScrollToPosition(adapter.getItemCount() - 1);
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }

                                    }
                                    break;
                            }
                        }

                    }
                });
    }

    /**
     * 查询历史聊天数据
     */
    private void getChatHistory() {
        int isBig = sendUserEmail.compareTo(formUserEmail);
        String documentId = formUserEmail+"+"+ sendUserEmail;
        if (isBig > 0) {
            documentId = sendUserEmail + "+" + formUserEmail;
        }

        FirebaseFirestore.getInstance().collection("chat")
                .where(Filter.equalTo("mark", documentId))
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
                            Collections.sort(mList, new Comparator<ChatBean>() {
                                @Override
                                public int compare(ChatBean chatBean1, ChatBean chatBean2) {
                                    return (int) (chatBean1.getTime() - chatBean2.getTime());
                                }
                            });
                            adapter.notifyDataSetChanged();
                            try {
                                binding.recyclerview.smoothScrollToPosition(adapter.getItemCount() - 1);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    }
                });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}