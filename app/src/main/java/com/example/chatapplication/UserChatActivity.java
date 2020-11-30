package com.example.chatapplication;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapplication.Adapters.Chats;
import com.example.chatapplication.Adapters.MessageAdapter;
import com.example.chatapplication.Adapters.UserListAdapter;
import com.example.chatapplication.Adapters.UserObject;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class UserChatActivity extends AppCompatActivity {
    Toolbar toolbar;
    EmojiconEditText editText;
    ImageView imageView;
    String number, senderNumber;
    DatabaseReference myRef;
    MessageAdapter messageAdapter;
    List<Chats> chats;
    RecyclerView recyclerView;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_chat);

        recyclerView = findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        chats = new ArrayList<>();

        number = getIntent().getStringExtra("number");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        String name = "";
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            name = extras.getString("name");
        }
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(name.equals("")){
            toolbar.setTitle(getIntent().getStringExtra("number"));
        }else{
            toolbar.setTitle(name);
        }

        editText = (EmojiconEditText) findViewById(R.id.sendmess);
        imageView = findViewById(R.id.imagebutton);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        read();

        SharedPreferences sharedPreferences = getSharedPreferences("userNumber", Context.MODE_PRIVATE);
        String num = sharedPreferences.getString("number", "");

        System.out.println("send "+num);
        System.out.println("rec "+number);
        readMessages(num, number);
    }

    private void read() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Users");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    senderNumber = dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child("phoneNumber").getValue(String.class);
                    SharedPreferences sharedPreferences = getSharedPreferences("userNumber", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("number", senderNumber);
                    editor.apply();
                } else {
                    senderNumber = "";
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    private void readMessages(final String phone, final String receiverPhone) {
        myRef = FirebaseDatabase.getInstance().getReference("Messages");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    chats.clear();
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Chats chat = snapshot1.getValue(Chats.class);
                        if (chat.getReceiver().equals(receiverPhone) && chat.getSender().equals(phone)||
                                chat.getReceiver().equals(phone) && chat.getSender().equals(receiverPhone)) {

                            chats.add(chat);

                        }
                        //chats.add(chat);
                        messageAdapter = new MessageAdapter(UserChatActivity.this, chats);
                        recyclerView.setAdapter(messageAdapter);
                    }
                    //System.out.println("snap"+snapshot.getChildren());

                } else {
                    Toasty.error(getApplicationContext(), "Cant get messages", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserChatActivity.this, "error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void sendmess(View view) {
        String message = editText.getText().toString().trim();
        //Toast.makeText(this, "" + message, Toast.LENGTH_SHORT).show();
        number = getIntent().getStringExtra("number");
        if (message.isEmpty()) {
            Toasty.error(this, "Can't send Empty Messages", Toast.LENGTH_SHORT).show();
        }
        else {
            sendMessage(senderNumber, number, message);
        }

    }

    private void sendMessage(String Sender, final String Receiver, String Message) {
        final Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy h:mm a", Locale.getDefault());
        final String formattedDate = df.format(c);
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Messages");
        //.child(Sender + " " + Receiver)
        //.child(Sender + " " + Receiver + " " + formattedDate);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy h:mm:ss:SSS", Locale.getDefault());
        String date = dateFormat.format(c);
        System.out.println(date);

        Map<String, Object> messages = new HashMap<>();
        messages.put("Sender", Sender);
        messages.put("Receiver", Receiver);
        messages.put("Message", Message);
        messages.put("Date", formattedDate);
        databaseReference.child(c.toString()).setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isComplete()) {
                    editText.setText(null);
                }
            }
        });

        final DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(Sender).child(Receiver + date);

        databaseReference1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    databaseReference1.child("id").setValue(Receiver);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    public void emojiview(View view) {
        EmojIconActions emojIconActions = new EmojIconActions(this, view, editText, imageView);
        emojIconActions.ShowEmojIcon();
        emojIconActions.setUseSystemEmoji(true);
        editText.setUseSystemDefault(true);
        //Toast.makeText(this, "Clicked!!", Toast.LENGTH_SHORT).show();
    }
}