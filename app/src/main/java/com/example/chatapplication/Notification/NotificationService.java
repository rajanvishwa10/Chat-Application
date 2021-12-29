package com.example.chatapplication.Notification;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.Person;
import androidx.core.app.RemoteInput;
import androidx.core.content.ContextCompat;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.chatapplication.Cypher;
import com.example.chatapplication.MainActivity2;
import com.example.chatapplication.R;
import com.example.chatapplication.UserChatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class NotificationService extends FirebaseMessagingService {

    public static final String GENERAL_CHANNEL = "General Channel";

    public static final String GENERAL_CHANNEL_ID = "general_channel_id";

    public static final String CHAT_CHANNEL = "Chats";

    public static final String CHAT_CHANNEL_ID = "chat_channel_id";

    public static final int ID = 111;

    //todo setting up local notification as well


    @Override
    public void onNewToken(@NonNull final String s) {
        super.onNewToken(s);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Users");
        myRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if(FirebaseAuth.getInstance().getCurrentUser() != null) {
                        String username = dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("phoneNumber").getValue(String.class);
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Tokens");
                        databaseReference.child(username).child("token").setValue(s);
                    }
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
            sendNotification(title2, contact, Cypher.decrypt(body), channel_id);
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

                                final Person user = new Person.Builder()
                                        .setIcon(IconCompat.createWithBitmap(resource))
                                        .setName(title).build();

                                NotificationCompat.MessagingStyle.Message notificationMessage;
                                if (body.contains("http")) {
                                    notificationMessage = new
                                            NotificationCompat.MessagingStyle.Message(
                                            "sent you an image",
                                            System.currentTimeMillis(),
                                            user
                                    );

                                } else {
                                    notificationMessage = new
                                            NotificationCompat.MessagingStyle.Message(
                                            body,
                                            System.currentTimeMillis(),
                                            user
                                    );
                                }


                                NotificationCompat.MessagingStyle messagingStyle = new
                                        NotificationCompat.MessagingStyle(user);
                                messagingStyle.addMessage(notificationMessage).build();

                                Intent broadIntent = new Intent(getApplicationContext(), ReplyBroadcastReceiver.class);
                                broadIntent.setAction("REPLY_ACTION");
                                broadIntent.putExtra("number", contactName);

                                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 18, broadIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                                RemoteInput remoteInput = new RemoteInput.Builder("key_text_reply").setLabel("Reply").build();

                                NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.icons8_chat_500px_3, "Reply", pendingIntent)
                                        .addRemoteInput(remoteInput).build();


                                Intent broadIntent2 = new Intent(getApplicationContext(), ReplyBroadcastReceiver.class);
                                broadIntent2.setAction("MARK_AS_ACTION");
                                broadIntent2.putExtra("number", contactName);

                                PendingIntent pendingIntent2 = PendingIntent.getBroadcast(getApplicationContext(), 19, broadIntent2, PendingIntent.FLAG_UPDATE_CURRENT);

                                NotificationCompat.Action action2 = new NotificationCompat.Action.Builder(R.drawable.icons8_chat_500px_3, "Mark as read", pendingIntent2)
                                        .build();


                                final NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channel_id)
                                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                                        .setContentIntent(notifyPendingIntent)
                                        .setSmallIcon(R.drawable.icons8_chat_500px_3)
                                        .setStyle(messagingStyle)
                                        .setAutoCancel(true)
                                        .setOnlyAlertOnce(true)
                                        .addAction(action)
                                        .addAction(action2)
                                        .setColor(ContextCompat.getColor(getApplicationContext(),R.color.colorAccent))
//                                        .setBubbleMetadata(bubbleData)
//                                        .setGroup("com.android.example.WORK_EMAIL")
                                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                        .setShortcutId(String.valueOf(user.getName()));
//
//                                NotificationCompat.Builder builder3 = new NotificationCompat.Builder(getApplicationContext(), channel_id)
//                                        .setPriority(NotificationCompat.PRIORITY_HIGH)
////                                        .setContentIntent(notifyPendingIntent)
//                                        .setSmallIcon(R.drawable.icons8_chat_500px_3)
//                                        .setStyle(new NotificationCompat.InboxStyle()
////                                                .addLine(user.getName() + "  " + notificationMessage.getText())
////                                                .addLine(user2.getName() + "  " + notificationMessage2.getText())
//                                                .setSummaryText("2 new messages from 2 contacts"))
//                                        .setGroup("com.android.example.WORK_EMAIL")
//                                        .setAutoCancel(true)
//                                        .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
//                                        .setGroupSummary(true);

//
                                NotificationManagerCompat managerCompat = NotificationManagerCompat.from(getApplicationContext());
                                managerCompat.notify(ID, builder.build());
//                                managerCompat.notify(14, builder3.build());
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
                        managerCompat.notify(ID, builder.build());

                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                //Failed to read value
            }
        });


    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private Uri getUrl(String body) {
        try {
            URL url = new URL(body);
            return Uri.parse(url.toURI().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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

    Bitmap res;

    public Bitmap getBitmap(String imgUrl) {

        Glide.with(getApplicationContext()).asBitmap().load(imgUrl)
                .apply(RequestOptions.circleCropTransform()).listener(new RequestListener<Bitmap>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                res = resource;
                return false;
            }
        }).submit();
        return res;
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
