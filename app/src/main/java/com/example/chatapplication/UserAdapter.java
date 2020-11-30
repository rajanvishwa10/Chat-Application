package com.example.chatapplication;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.Adapters.UserObject;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private final Context context;
    private final List<RecentChat> chatList;
    public UserAdapter(Context context, List<RecentChat> chatList) {
        this.context = context;
        this.chatList = chatList;
    }

    @NonNull
    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatlist, null, false);

        return new ViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull final UserAdapter.ViewHolder holder, final int position) {

        final String number = chatList.get(position).getPhoneNumber();

        Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (phones.moveToNext()) {
            final String contactName = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            phone = phone.replaceAll("\\s", "");
            if(phone.equals(number)){
                holder.textView.setText(contactName);
                holder.linearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, UserChatActivity.class);
                        intent.putExtra("name", contactName);
                        intent.putExtra("number", number);
                        context.startActivity(intent);
                    }
                });
            }else{
            }
        }



    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        LinearLayout linearLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.number);
            linearLayout = itemView.findViewById(R.id.linear);
        }
    }
}
