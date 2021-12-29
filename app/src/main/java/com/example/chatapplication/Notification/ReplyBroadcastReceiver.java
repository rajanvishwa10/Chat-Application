package com.example.chatapplication.Notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatapplication.Adapters.Chats;
import com.example.chatapplication.Cypher;
import com.example.chatapplication.MyApp;
import com.example.chatapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class ReplyBroadcastReceiver extends BroadcastReceiver {

    String fcm_url = "https://fcm.googleapis.com/fcm/send";
    RequestQueue requestQueue;

    @Override
    public void onReceive(final Context context, Intent intent) {

        SharedPreferences sharedPreferences = context.getSharedPreferences("userNumber", Context.MODE_PRIVATE);
        final String num = sharedPreferences.getString("number", "");

        if (intent.getAction().equals("REPLY_ACTION")) {
            requestQueue = Volley.newRequestQueue(context);
            if (intent.getStringExtra("number") != null) {
                String number = intent.getStringExtra("number");
                seenMessage(number, num, context);
                sendMessage(context, num, number, Cypher.encrypt(String.valueOf(getReplyMessage(intent, context))).trim());
            }
        } else {
            if (intent.getStringExtra("number") != null) {
                final String number = intent.getStringExtra("number");
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Messages");
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Chats chat = dataSnapshot.getValue(Chats.class);
                            if (chat.getReceiver().equals(num) && chat.getSender().equals(number)) {
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("isseen", true);
                                dataSnapshot.getRef().updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isComplete()){
                                            NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(context.getApplicationContext());
                                            mNotificationManager.cancel(NotificationService.ID);
//                                            MyApp.getInstance().unregisterReceiver(ReplyBroadcastReceiver.this);
                                        }
                                    }
                                });

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }

    }

    private void removeNoti(Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationService.CHAT_CHANNEL_ID)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentText("Message Sent")
                .setContentTitle("Message Sent")
                .setTimeoutAfter(2000)
                .setSmallIcon(R.drawable.icons8_chat_500px_3);
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
        managerCompat.notify(NotificationService.ID, builder.build());

    }


    private CharSequence getReplyMessage(Intent intent, Context context) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            return remoteInput.getCharSequence("key_text_reply");
        }
        return null;
    }

    private void seenMessage(final String number, final String senderNumber, final Context context) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Messages");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chats chat = dataSnapshot.getValue(Chats.class);
                    if (chat.getReceiver().equals(senderNumber) && chat.getSender().equals(number)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        dataSnapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(final Context context, final String Sender, final String Receiver, final String Message) {
        final Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy h:mm a", Locale.getDefault());
        final String formattedDate = df.format(c);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Messages");

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
                                sendNotification(username, Sender, Message);
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
                Toast.makeText(context, "Message Sent", Toast.LENGTH_SHORT).show();
//                    NotificationManagerCompat.from(context).cancel(12);
                removeNoti(context);
                MyApp.getInstance().unregisterReceiver(ReplyBroadcastReceiver.this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendNotification(String receiver, String sender, String message) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("to", receiver);

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

