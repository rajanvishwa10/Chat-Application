package com.example.chatapplication.Adapters;

import android.app.ProgressDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserListRecyclerViewHolder> {
    ArrayList<UserObject> userList;
    private RecyclerViewClickListener listener;
    public UserListAdapter(ArrayList<UserObject> userList, RecyclerViewClickListener listener){
        this.userList =  userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserListRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.userlist,null,false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);

        UserListRecyclerViewHolder rcv = new UserListRecyclerViewHolder(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(@NonNull final UserListRecyclerViewHolder holder, final int position) {

        final String phone = userList.get(position).getPhone();
//
//        final DatabaseReference userDb = FirebaseDatabase.getInstance().getReference().child("Users");
//        userDb.orderByChild("phoneNumber").equalTo(phone)
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        if (snapshot.exists()){
                            holder.tname.setText(userList.get(position).getName());
                            String ph = phone.substring(3);
                            holder.tphone.setText(ph);
                            holder.progressBar.setVisibility(View.GONE);

//                        }else{
//                            holder.linearLayout.setVisibility(View.GONE);
//                            holder.tname.setVisibility(View.GONE);
//                            holder.tphone.setVisibility(View.GONE);
//                            holder.progressBar.setVisibility(View.GONE);
//                        }
//
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class UserListRecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView tname, tphone;
        LinearLayout linearLayout;
        ProgressBar progressBar;
        public UserListRecyclerViewHolder(@NonNull View view) {
            super(view);
            tname = view.findViewById(R.id.name);
            tphone = view.findViewById(R.id.contact);
            linearLayout = view.findViewById(R.id.linear);
            progressBar = view.findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onClick(v, getAdapterPosition());
        }
    }

    public interface RecyclerViewClickListener{
        void onClick(View v, int position);
    }
}
