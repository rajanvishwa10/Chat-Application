package com.example.chatapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class StartActivity extends AppCompatActivity {
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        mAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            Bundle extras = intent.getExtras();
            if(extras.getString("profile") == null){
                if (mAuth.getCurrentUser() != null) {
                    Intent intent2 = new Intent(StartActivity.this, MainActivity2.class);
                    startActivity(intent2);
                    finish();
                }
            }
        }else{
            if (mAuth.getCurrentUser() != null) {
                Intent intent2 = new Intent(StartActivity.this, MainActivity2.class);
                startActivity(intent2);
                finish();
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorBlack));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorWhite));
        }
    }

    public void lets(View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }
}
