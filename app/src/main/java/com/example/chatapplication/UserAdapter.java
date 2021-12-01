package com.example.chatapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapplication.Adapters.Chats;
import com.example.chatapplication.Adapters.UserObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;


public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private final Context context;
    private final List<Chatlist> chatList;
    String url1;
    String lastmess;
    String senderNumber;

    public UserAdapter(Context context, List<Chatlist> chatList) {
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

        final String number = chatList.get(position).getId();
        lastMessage(number, holder.textView2, holder.countTv);
        Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (phones.moveToNext()) {
            final String contactName = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            phone = phone.replaceAll("\\s", "");
            if (phone.equals(number)) {
                holder.textView.setText(contactName);
                holder.linearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, UserChatActivity.class);
                        intent.putExtra("name", contactName);
                        intent.putExtra("number", number);
                        intent.putExtra("url", url1);
                        context.startActivity(intent);
                    }
                });

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("Users");
                myRef.orderByChild("phoneNumber").equalTo(number).addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            final String url = snapshot.child("profileImage").getValue(String.class);
                            url1 = snapshot.child("profileImage").getValue(String.class);
                            try {
                                if (url != null) {
                                    Glide.with(context).load(url).into(holder.imageView);
                                } else {
                                    holder.imageView.setImageResource(R.drawable.user);
                                }
                            } catch (NullPointerException e) {
                                holder.imageView.setImageResource(R.drawable.user);
                            }

                            holder.imageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(context, FullScreenImageActivity.class);
                                    ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                            (Activity) context, holder.imageView, holder.imageView.getTransitionName());
                                    intent.putExtra("url", url);
                                    intent.putExtra("name", contactName);
                                    context.startActivity(intent, activityOptionsCompat.toBundle());
                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        //Failed to read value
                        Toasty.error(context, "error", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }


    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView, textView2, countTv;
        LinearLayout linearLayout;
        CircleImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.number);
            textView2 = itemView.findViewById(R.id.lastMessage);
            linearLayout = itemView.findViewById(R.id.linear);
            imageView = itemView.findViewById(R.id.circleImageView2);
            countTv = itemView.findViewById(R.id.unseenCount);
        }
    }
    int count = 0;
    private void lastMessage(final String number, final TextView lastmsg, final TextView countTv) {
        lastmess = "default";
        SharedPreferences sharedPreferences = context.getSharedPreferences("userNumber", Context.MODE_PRIVATE);
        senderNumber = sharedPreferences.getString("number", "");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Messages");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chats chat = dataSnapshot.getValue(Chats.class);
                    if (chat.getReceiver().equals(number) && chat.getSender().equals(senderNumber) ||
                            chat.getReceiver().equals(senderNumber) && chat.getSender().equals(number)) {

                        lastmess = chat.getMessage();
                        if (lastmess.contains("https://")) {
                            lastmess = "Image";
                        }

                        if (!chat.getSender().equals(senderNumber)) {
                            if (!chat.isIsseen()) {
                                count++;
                            }
                        }
                    }
                }
                if ("default".equals(lastmess)) {
                    lastmsg.setText("No Message");
                } else {
                    lastmsg.setText(lastmess);
                }

                if (count > 0) {
                    lastmsg.setTypeface(lastmsg.getTypeface(), Typeface.BOLD);
                    countTv.setVisibility(View.VISIBLE);
                    countTv.setText("" + count);
                    count = 0;
                } else {
                    lastmsg.setTypeface(lastmsg.getTypeface(), Typeface.NORMAL);
                    countTv.setVisibility(View.GONE);
                }

                lastmess = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
