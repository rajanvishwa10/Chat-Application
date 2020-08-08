package com.example.chatapplication.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.R;

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
    public void onBindViewHolder(@NonNull UserListRecyclerViewHolder holder, int position) {
        holder.tname.setText(userList.get(position).getName());
        holder.tphone.setText(userList.get(position).getPhone());
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class UserListRecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView tname, tphone;
        public UserListRecyclerViewHolder(@NonNull View view) {
            super(view);
            tname = view.findViewById(R.id.name);
            tphone = view.findViewById(R.id.contact);
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
