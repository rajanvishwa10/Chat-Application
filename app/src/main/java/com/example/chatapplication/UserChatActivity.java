package com.example.chatapplication;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.NotificationManager;
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
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.chatapplication.Adapters.Chats;
import com.example.chatapplication.Adapters.MessageAdapter;
import com.example.chatapplication.Notification.NotificationService;
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

import org.json.JSONException;
import org.json.JSONObject;

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

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class UserChatActivity extends AppCompatActivity {
    Toolbar toolbar;
    EmojiconEditText editText;
    //    ImageView imageView;
    CircleImageView circleImageView;
    String number, senderNumber, url;
    DatabaseReference myRef;
    MessageAdapter messageAdapter;
    List<Chats> chats;
    RecyclerView recyclerView;
    Bitmap bitmap;
    String fcm_url = "https://fcm.googleapis.com/fcm/send";
    RequestQueue requestQueue;
    ValueEventListener valueEventListener;
    DatabaseReference databaseReference;
    boolean notify = false;
    String name;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_chat);

        requestQueue = Volley.newRequestQueue(this);
        editText = (EmojiconEditText) findViewById(R.id.sendmess);
        recyclerView = findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        chats = new ArrayList<>();
        circleImageView = findViewById(R.id.circleImageView);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        getWindow().setNavigationBarColor(Color.parseColor("#EDE9E9"));

        number = getIntent().getStringExtra("number");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            name = extras.getString("name");
        }
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        final TextView textView = findViewById(R.id.name);
        final TextView textView2 = findViewById(R.id.status);
        if (name.equals("")) {
            textView.setText(getIntent().getStringExtra("number"));
        } else {
            textView.setText(name);
        }



//        imageView = findViewById(R.id.imagebutton);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        read();

        SharedPreferences sharedPreferences = getSharedPreferences("userNumber", Context.MODE_PRIVATE);
        String num = sharedPreferences.getString("number", "");

//        readMessages(Cypher.decrypt(num).trim(), number);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Users");
        myRef.orderByChild("phoneNumber").equalTo(number).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    url = snapshot.child("profileImage").getValue(String.class);
                    try {
                        if (!url.isEmpty()) {
                            Glide.with(getApplicationContext()).load(url).into(circleImageView);
                        } else {
                            circleImageView.setImageResource(R.mipmap.ic_launcher);
                        }
                    } catch (NullPointerException e) {
                        circleImageView.setImageResource(R.mipmap.ic_launcher);
                    }
                    circleImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(UserChatActivity.this, FullScreenImageActivity.class);
                            ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(UserChatActivity.this,
                                    circleImageView, circleImageView.getTransitionName());
                            intent.putExtra("url", url);
                            intent.putExtra("name", name);
                            startActivity(intent, activityOptionsCompat.toBundle());
                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                //Failed to read value
                Toasty.error(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
            }
        });
        myRef.orderByChild("phoneNumber").equalTo(number).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String status = dataSnapshot.child("status").getValue(String.class);
                    System.out.println(status);
                    textView2.setText(status);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        seenMessage(number);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ViewprofileActivity.class);
                Pair<View, String> pair = Pair.create(findViewById(R.id.circleImageView), circleImageView.getTransitionName());
                Pair<View, String> pair2 = Pair.create(findViewById(R.id.name), findViewById(R.id.name).getTransitionName());

                ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(UserChatActivity.this, pair, pair2);
                intent.putExtra("number", number);
                intent.putExtra("url", url);
                intent.putExtra("name", name);
                startActivity(intent, activityOptionsCompat.toBundle());
            }
        });

        getDataFromIntent(num, number);
    }

    private void getDataFromIntent(String num, String number) {
        Bundle remoteReply = RemoteInput.getResultsFromIntent(getIntent());
        if(remoteReply != null){
            String message = remoteReply.getCharSequence("key_text_reply").toString();
            sendMessage(num, number, Cypher.encrypt(message).trim());
        }
        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(getApplicationContext());
        mNotificationManager.cancelAll();
    }

    private void seenMessage(final String number) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Messages");
        valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chats chat = dataSnapshot.getValue(Chats.class);
                    if (chat.getReceiver().equals(senderNumber) && chat.getSender().equals(number)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        dataSnapshot.getRef().updateChildren(hashMap);
                        System.out.println("isseen");
                    } else {
                        System.out.println("error");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
                    senderNumber = Cypher.encrypt(senderNumber).trim();
                    readMessages(senderNumber, number);
                    SharedPreferences sharedPreferences = getSharedPreferences("userNumber", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("number", Cypher.encrypt(senderNumber).trim());
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
                        if (Cypher.decrypt(chat.getReceiver()).trim().equals(receiverPhone) && chat.getSender().equals(phone) ||
                                chat.getReceiver().equals(phone) &&  Cypher.decrypt(chat.getSender()).trim().equals(receiverPhone)) {

                            chats.add(chat);


                        }
                        //chats.add(chat);
                        messageAdapter = new MessageAdapter(UserChatActivity.this, chats);
                        recyclerView.setAdapter(messageAdapter);
                    }
                    //System.out.println("snap"+snapshot.getChildren());

                } else {
                    Toasty.error(getApplicationContext(), "No messages", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserChatActivity.this, "error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void sendmess(View view) {
        notify = true;
        String message = editText.getText().toString().trim();
        //Toast.makeText(this, "" + message, Toast.LENGTH_SHORT).show();
        number = getIntent().getStringExtra("number");
        if (message.isEmpty()) {
            Toasty.error(this, "Can't send Empty Messages", Toast.LENGTH_SHORT).show();
        } else {
            sendMessage(senderNumber, Cypher.encrypt(number).trim(), Cypher.encrypt(message).trim());
        }

    }

    private void sendMessage(final String Sender, final String Receiver, final String Message) {
        final Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy h:mm a", Locale.getDefault());
        final String formattedDate = df.format(c);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Messages");
        //.child(Sender + " " + Receiver)
        //.child(Sender + " " + Receiver + " " + formattedDate);

        final long timeInMillIs = System.currentTimeMillis();

        Map<String, Object> messages = new HashMap<>();
        messages.put("Sender", Sender);
        messages.put("Receiver", Receiver);
        messages.put("Message", Message);
        messages.put("Date", formattedDate);
        messages.put("isseen", false);
        databaseReference.child(timeInMillIs + "").setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isComplete()) {
                    editText.setText(null);
                    final DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("Chatlist")
                            .child(Sender).child(Receiver);
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("date", timeInMillIs);
                    databaseReference1.updateChildren(hashMap);

                    final DatabaseReference databaseReference2 = FirebaseDatabase.getInstance().getReference("Chatlist")
                            .child(Receiver).child(Sender);

                    databaseReference2.updateChildren(hashMap);
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("Tokens");
                    myRef.addValueEventListener(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String username = dataSnapshot.child(Receiver).child("token").getValue(String.class);
                                sendNotification(username, Cypher.decrypt(Sender).trim(), Cypher.decrypt(Message).trim());
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                } else {
                    System.out.println(task);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });

        final DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(Sender).child(Receiver);

        databaseReference1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    databaseReference1.child("id").setValue(Receiver);
                    databaseReference1.child("date").setValue(timeInMillIs);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        final DatabaseReference databaseReference2 = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(Receiver).child(Sender);

        databaseReference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    databaseReference2.child("id").setValue(Sender);
                    databaseReference2.child("date").setValue(timeInMillIs);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


//    public void emojiview(View view) {
//        EmojIconActions emojIconActions = new EmojIconActions(this, view, editText, imageView);
//        emojIconActions.ShowEmojIcon();
//        emojIconActions.setUseSystemEmoji(true);
//        editText.setUseSystemDefault(true);
//    }

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
                                        sendMessage(senderNumber, number, Cypher.encrypt(Imguri.toString()).trim());
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

    private void status(String Status) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", Status);
        databaseReference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("Online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        databaseReference.removeEventListener(valueEventListener);
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("h:mm a", Locale.getDefault());
        String formattedDate = df.format(c);
        status("Last Seen " + formattedDate);

    }

    private void sendNotification(String receiver, String sender, String message) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("to", receiver);

//            JSONObject jsonObject1 = new JSONObject();
//            jsonObject1.put("title", sender);
//            jsonObject1.put("body", "sent you a message");
//
//            jsonObject.put("notification", jsonObject1);

            JSONObject jsonObject2 = new JSONObject();
            jsonObject2.put("title2", sender);
            jsonObject2.put("body2", message);
            jsonObject2.put("channel_id", "chat_channel_id");

            jsonObject.put("data", jsonObject2);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, fcm_url, jsonObject, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    System.out.println(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> map = new HashMap<>();
                    map.put("content-type", "application/json");
                    map.put("authorization", "key = AAAA_dUmwyo:APA91bHNBkl4NYBYDdgUwdZGXsIjTkWa4N0UNkOgRbiNdSqpIzYGvb3n4o5Rjo4pkNWstbzAePDjgNVomE9qJBP_n0_3vHZbwqaZomnQ89EnNov7FrRmOkqUIXfo-vfFKAzaXbfjd3Qj");

                    return map;
                }
            };
            requestQueue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}