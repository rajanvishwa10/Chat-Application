package com.example.chatapplication.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapplication.FullScreenImageActivity;
import com.example.chatapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    public static final int MSG_TYPE_LEFT_IMAGE = 2;
    public static final int MSG_TYPE_RIGHT_IMAGE = 3;
    String number;
    private final Context context;
    private final List<Chats> chats;

    public MessageAdapter(Context context, List<Chats> chats) {
        this.chats = chats;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_right_message, parent, false);
            return new ViewHolder(view);
        }
        else if(viewType == MSG_TYPE_RIGHT_IMAGE) {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_right_image_message, parent, false);
            return new ViewHolder(view);
        }
        else if(viewType == MSG_TYPE_LEFT_IMAGE) {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_left_image_message, parent, false);
            return new ViewHolder(view);
        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_left_message, parent, false);
            return new ViewHolder(view);
        }


    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        Chats chat = chats.get(position);
        final String chatMessage = chat.getMessage();

        final String time = chat.getDate();
        String[] newTime = time.split("\\s");

        if (chatMessage.contains("https://")) {
//            holder.imageView.setVisibility(View.VISIBLE);
//            holder.cardView.setVisibility(View.VISIBLE);
//            holder.linearLayout.setVisibility(View.GONE);
//            holder.imageTime.setVisibility(View.VISIBLE);
            holder.imageTime.setText(newTime[1] + " " + newTime[2]);
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, FullScreenImageActivity.class);
                    intent.putExtra("url", chatMessage);
                    intent.putExtra("date", time);
                    context.startActivity(intent);
                }
            });
            Glide.with(holder.imageView.getContext()).load(chatMessage).into(holder.imageView);
        } else {
            holder.show_message.setText(chatMessage);
            holder.time.setText(newTime[1] + " " + newTime[2]);
        }


        if (position == chats.size() - 1) {
            if (chat.isIsseen()) {
                holder.isSeen.setText("Seen");
            } else {
                holder.isSeen.setText("Delivered");
            }
        } else {
            holder.isSeen.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView show_message, time, imageTime, isSeen;
        ImageView imageView;
        LinearLayout linearLayout;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            show_message = itemView.findViewById(R.id.chats);
            time = itemView.findViewById(R.id.time);
            imageView = itemView.findViewById(R.id.image);
            linearLayout = itemView.findViewById(R.id.linearLayout);
            cardView = itemView.findViewById(R.id.cardView);
            imageTime = itemView.findViewById(R.id.imageTime);
            isSeen = itemView.findViewById(R.id.seen);
        }
    }

    @Override
    public int getItemViewType(int position) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("userNumber", Context.MODE_PRIVATE);
        number = sharedPreferences.getString("number", "");
        //System.out.println("number" + number);
        if (chats.get(position).getSender().equals(number)) {
            if (chats.get(position).getMessage().contains("http")) {
                return MSG_TYPE_RIGHT_IMAGE;
            } else {
                return MSG_TYPE_RIGHT;
            }
        } else {
            if (chats.get(position).getMessage().contains("http")) {
                return MSG_TYPE_LEFT_IMAGE;
            } else {
                return MSG_TYPE_LEFT;
            }
        }
    }

}
