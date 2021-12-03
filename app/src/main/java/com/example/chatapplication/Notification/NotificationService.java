package com.example.chatapplication.Notification;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.Person;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.chatapplication.Adapters.UserObject;
import com.example.chatapplication.FullScreenImageActivity;
import com.example.chatapplication.MainActivity;
import com.example.chatapplication.MainActivity2;
import com.example.chatapplication.R;
import com.example.chatapplication.UserChatActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class NotificationService extends FirebaseMessagingService {

    private static final String GENERAL_CHANNEL = "General Channel";

    private static final String GENERAL_CHANNEL_ID = "general_channel_id";

    private static final String CHAT_CHANNEL = "Chats";

    private static final String CHAT_CHANNEL_ID = "chat_channel_id";

    //todo setting up local notification as well


    @Override
    public void onNewToken(@NonNull final String s) {
        super.onNewToken(s);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Users");
        myRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String username = dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("phoneNumber").getValue(String.class);
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Tokens");
                    databaseReference.child(username).child("token").setValue(s);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        generalChannel();
        chatChannel();

        String contact, title2 = "";
        String body;

        if (!isRunning(getApplicationContext())) {
            contact = remoteMessage.getData().get("title2");
            body = remoteMessage.getData().get("body2");
            String channel_id = remoteMessage.getData().get("channel_id");

            Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
            while (phones.moveToNext()) {
                String contactName = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                number = number.replaceAll("\\s", "");
                if (contact != null && contact.contains(number)) {
                    title2 = contactName;
                    break;
                }
            }
            sendNotification(title2, contact, body, channel_id);
            phones.close();
        }


    }

    private void generalChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    GENERAL_CHANNEL_ID, GENERAL_CHANNEL,
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel);
        }
    }

    private void chatChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    CHAT_CHANNEL_ID, CHAT_CHANNEL,
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel);
        }
    }


    private void sendNotification(final String title, final String contactName, final String body, final String channel_id) {

        Intent notifyIntent = new Intent(getApplicationContext(), UserChatActivity.class);
        notifyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notifyIntent.putExtra("name", title);
        notifyIntent.putExtra("number", contactName);
        final PendingIntent notifyPendingIntent = PendingIntent.getActivity(
                this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Users");
        myRef.orderByChild("phoneNumber").equalTo(contactName).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    final String url = snapshot.child("profileImage").getValue(String.class);
                    if (url != null) {
                        Glide.with(getApplicationContext()).asBitmap().load(url)
                                .apply(RequestOptions.circleCropTransform()).listener(new RequestListener<Bitmap>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                Person user = new Person.Builder()
                                        .setIcon(IconCompat.createWithBitmap(resource))
                                        .setName(title).build();
                                Intent intent = new Intent(getApplicationContext(), UserChatActivity.class);
                                intent.putExtra("name", title);
                                intent.putExtra("number", contactName);
                                PendingIntent bubbleIntent =
                                        PendingIntent.getActivity(getApplicationContext(), 0, intent, 0 /* flags */);
                                //createShortCut(user);


                                NotificationCompat.BubbleMetadata bubbleData =
                                        new NotificationCompat.BubbleMetadata.Builder()
                                                .setIntent(bubbleIntent)
                                                .setIcon(IconCompat.createWithResource(getApplicationContext(), R.drawable.ic_baseline_chat_24))
//                                                .setIcon(IconCompat.createWithBitmap(resource))
                                                .setDesiredHeight(600)
                                                .build();

                                NotificationCompat.MessagingStyle messagingStyle = new
                                        NotificationCompat.MessagingStyle(user);
                                messagingStyle.setConversationTitle("New Message");
                                NotificationCompat.MessagingStyle.Message notificationMessage = new
                                        NotificationCompat.MessagingStyle.Message(
                                        body,
                                        System.currentTimeMillis(),
                                        user
                                );
                                messagingStyle.addMessage(notificationMessage);


                                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channel_id)
                                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                                        .setContentIntent(notifyPendingIntent)
                                        .setSmallIcon(R.drawable.icons8_chat_500px_3)
                                        .setStyle(messagingStyle)
                                        .setAutoCancel(true)
                                        .setBubbleMetadata(bubbleData)
                                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                        .setShortcutId("Chats");

                                NotificationManagerCompat managerCompat = NotificationManagerCompat.from(getApplicationContext());
                                managerCompat.notify(12, builder.build());
                                return false;
                            }
                        }).submit();
                    } else {
                        Person user = new Person.Builder()
                                .setName(title).build();
                        Intent intent = new Intent(getApplicationContext(), UserChatActivity.class);
                        intent.putExtra("name", title);
                        intent.putExtra("number", contactName);
                        PendingIntent bubbleIntent =
                                PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT /* flags */);
                        //createShortCut(user);
                        NotificationCompat.BubbleMetadata bubbleData =
                                new NotificationCompat.BubbleMetadata.Builder()
                                        .setIntent(bubbleIntent)
                                        .setIcon(IconCompat.createWithResource(getApplicationContext(), R.drawable.ic_baseline_chat_24))
                                        .setDesiredHeight(600)
                                        .build();
                        NotificationCompat.MessagingStyle messagingStyle = new
                                NotificationCompat.MessagingStyle(user);
                        messagingStyle.setConversationTitle("New Message");
                        NotificationCompat.MessagingStyle.Message notificationMessage = new
                                NotificationCompat.MessagingStyle.Message(
                                body,
                                System.currentTimeMillis(),
                                user
                        );
                        messagingStyle.addMessage(notificationMessage);


                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channel_id)
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setContentIntent(notifyPendingIntent)
                                .setSmallIcon(R.drawable.icons8_chat_500px_3)
                                .setStyle(messagingStyle)
                                .setAutoCancel(true)
                                .setBubbleMetadata(bubbleData)
                                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                .setShortcutId("Chats");

                        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(getApplicationContext());
                        managerCompat.notify(12, builder.build());

                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                //Failed to read value
            }
        });


    }

    private void createShortCut(Person user) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Intent messageIntent = new Intent(getApplicationContext(), MainActivity2.class);
            messageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            messageIntent.setAction(Intent.ACTION_VIEW);

            ShortcutInfoCompat shortcutInfoCompat = new ShortcutInfoCompat.Builder(getApplicationContext(), "Chats").
                    setPerson(new Person.Builder().setName(user.getName()).setImportant(true).setIcon(user.getIcon()).build()).
                    setShortLabel(user.getName()).
                    setLongLived(true).
                    setIntent(messageIntent).
                    setIcon(user.getIcon()).build();
            ShortcutManagerCompat.addDynamicShortcuts(this, Arrays.asList(shortcutInfoCompat));

        }

    }

    public boolean isRunning(Context ctx) {
        ActivityManager activityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (ctx.getPackageName().equalsIgnoreCase(task.baseActivity.getPackageName()))
                return true;
        }
        return false;
    }
}
