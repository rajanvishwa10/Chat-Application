package com.example.chatapplication;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
    Bitmap bitmap;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_chat);

        recyclerView = findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        chats = new ArrayList<>();

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        getWindow().setNavigationBarColor(Color.parseColor("#EDE9E9"));

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
                .child(Sender).child(Receiver);

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
    }

    public void image(View view) {
        request();
    }
    private void request() {
        if (ContextCompat.checkSelfPermission
                (this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
        ) {
            SelectImage();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    1
            );

        }
    }
    private void SelectImage() {
        final CharSequence[] items = {"Gallery", "Camera", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Image");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (items[i].equals("Camera")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, 0);
                } else if (items[i].equals("Gallery")) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent.createChooser(intent, "Select file"), 1);
                } else if (items[i].equals("Cancel")) {
                    dialogInterface.dismiss();
                }
            }
        });
        builder.create().show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println(bitmap);
                    postImages(bitmap);
                }
                break;

            case 0:
                if (resultCode == RESULT_OK) {
                    bitmap = (Bitmap) data.getExtras().get("data");
                    System.out.println(bitmap);
                    postImages(bitmap);
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void postImages(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

        final Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.getDefault());
        final String formattedDate = df.format(c);


        final StorageReference reference = FirebaseStorage.getInstance().getReference().
                child("Images").
                child(formattedDate + ".jpeg");

        reference.putBytes(byteArrayOutputStream.toByteArray())
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        reference.getDownloadUrl().addOnSuccessListener(
                                new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri Imguri) {
                                        sendMessage(senderNumber,number,Imguri.toString());
                                    }
                                }
                        );

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
//
                    }
                });
    }
}