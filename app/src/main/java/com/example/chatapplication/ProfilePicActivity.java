package com.example.chatapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chatapplication.Adapters.Chats;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class ProfilePicActivity extends AppCompatActivity {
    Bitmap bitmap;
    EditText editText;
    ImageView imageView;
    String url, status,phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_pic);
        imageView = findViewById(R.id.circleImageView);
        editText = findViewById(R.id.status);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        getProfilePic();
        getStatus();

    }

    public void selectImage(View view) {
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
                    imageView.setImageURI(uri);
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
                    imageView.setImageBitmap(bitmap);
                    System.out.println(bitmap);
                    postImages(bitmap);
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setStatus(String status) {
        final Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy h:mm a", Locale.getDefault());
        final String formattedDate = df.format(c);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Status")
                .child(phone);
        Map<String, Object> update = new HashMap<>();
        update.put("stats", status);
        update.put("date", formattedDate);
        update.put("phone", phone);

        databaseReference.updateChildren(update).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(ProfilePicActivity.this, "Status saved", Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void postImages(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

        final StorageReference reference = FirebaseStorage.getInstance().getReference().
                child("ProfileImages").
                child(FirebaseAuth.getInstance().getUid() + ".jpeg");

        reference.putBytes(byteArrayOutputStream.toByteArray())
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        reference.getDownloadUrl().addOnSuccessListener(
                                new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri Imguri) {
                                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

                                        Map<String, Object> updates = new HashMap<>();

                                        updates.put("profileImage", String.valueOf(Imguri));

                                        ref.updateChildren(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toasty.success(ProfilePicActivity.this, "Profile Photo Uploaded", Toast.LENGTH_SHORT).show();

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toasty.error(ProfilePicActivity.this, "Not Updated", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                        );

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toasty.success(ProfilePicActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getProfilePic() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Users");
        myRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    url = dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("profileImage").getValue(String.class);
                    phone = dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("phoneNumber").getValue(String.class);

                    try {
                        if (url.isEmpty()) {
                            imageView.setImageResource(R.drawable.icons8_user_60px);
                        } else {
                            Glide.with(getApplicationContext()).load(url).into(imageView);
                            imageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(getApplicationContext(), FullScreenImageActivity.class);
                                    intent.putExtra("url", url);
                                    startActivity(intent);
                                }
                            });
                        }

                    } catch (NullPointerException e) {
                        imageView.setImageResource(R.drawable.icons8_user_60px);
                    }



                } else {
                    Toasty.error(ProfilePicActivity.this, "Nothing to show", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                //Failed to read value
                Toasty.error(ProfilePicActivity.this, "error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getStatus() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Status");
        myRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    status = dataSnapshot.child(phone).child("stats").getValue(String.class);

                    try {
                        if (!status.isEmpty()) {
                            editText.setText(status);
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                } else {
                    Toasty.error(ProfilePicActivity.this, "Nothing to show", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                //Failed to read value
                Toasty.error(ProfilePicActivity.this, "error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setStatus(View view) {
        String status = editText.getText().toString().trim();
        if (status.isEmpty()) {
            editText.setError("Enter status");
        } else {
            setStatus(status);
        }

    }
}