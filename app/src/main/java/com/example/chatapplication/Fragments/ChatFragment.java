package com.example.chatapplication.Fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chatapplication.Adapters.UserObject;
import com.example.chatapplication.Chatlist;
import com.example.chatapplication.Notification.Token;
import com.example.chatapplication.R;
import com.example.chatapplication.RecentChat;
import com.example.chatapplication.UserAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.List;


public class ChatFragment extends Fragment {

    RecyclerView recyclerView;
    UserAdapter userAdapter;

    List<RecentChat> chatList;
    List<Chatlist> userList;

    DatabaseReference databaseReference;
    ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userList = new ArrayList<>();

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Getting Data");
        progressDialog.setMessage("Loading....");
        progressDialog.show();

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("userNumber", Context.MODE_PRIVATE);
        final String sender = sharedPreferences.getString("number", "");

        databaseReference = FirebaseDatabase.getInstance().getReference("Chatlist").
                child(sender);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Chatlist chatlist = dataSnapshot.getValue(Chatlist.class);
                    userList.add(chatlist);
                }
                chatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        updateToken(FirebaseInstanceId.getInstance().getToken());

        return view;
    }

    private void updateToken(String token){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        databaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token1);
    }

    private void chatList(){
        chatList = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    RecentChat userObject = dataSnapshot.getValue(RecentChat.class);
                    for(Chatlist chatlist : userList){
                        if(userObject.getPhoneNumber().equals(chatlist.getId())){
                            chatList.add(userObject);
                        }
                    }
                }
                userAdapter = new UserAdapter(getContext(), chatList);
                progressDialog.dismiss();
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

//    pr
}
