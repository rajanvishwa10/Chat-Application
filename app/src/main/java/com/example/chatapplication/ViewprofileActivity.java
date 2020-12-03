package com.example.chatapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

public class ViewprofileActivity extends AppCompatActivity {

    TextView textView,textView2,textView3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewprofile);

        textView = findViewById(R.id.userStatus);
        textView2 = findViewById(R.id.statusDate);
        textView3 = findViewById(R.id.name);

        textView3.setText(getIntent().getStringExtra("name"));

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        ImageView imageView = findViewById(R.id.profilepic);
        Glide.with(this).load(getIntent().getStringExtra("url")).into(imageView);

        final String phone = getIntent().getStringExtra("number");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Status");
        databaseReference.orderByChild("phone").equalTo(phone).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String status = snapshot.child(phone).child("stats").getValue(String.class);
                    String date = snapshot.child(phone).child("date").getValue(String.class);
                    textView.setText(status);
                    textView2.setText(date);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}