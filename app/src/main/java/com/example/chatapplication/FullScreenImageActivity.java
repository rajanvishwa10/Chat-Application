package com.example.chatapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class FullScreenImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        getWindow().setNavigationBarColor(getResources().getColor(R.color.colorBlack));
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorBlack));

        Toolbar toolbar = findViewById(R.id.toolbar);
        String name = getIntent().getStringExtra("name");
        try {
            if (!name.contains("-")) {
                toolbar.setTitle(name);
            } else {
                toolbar.setTitle(getIntent().getStringExtra("date"));
            }

        } catch (NullPointerException e) {
            toolbar.setTitle(getIntent().getStringExtra("date"));
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });

        ImageView imageView = findViewById(R.id.imageView2);
        Glide.with(this).load(getIntent().getStringExtra("url")).into(imageView);
    }
}