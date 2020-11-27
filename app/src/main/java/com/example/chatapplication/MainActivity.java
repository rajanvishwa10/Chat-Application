package com.example.chatapplication;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            getWindow().setNavigationBarColor(getResources().getColor(R.color.colorWhite));
        }
        request();

    }

    public void back(View view) {
        Intent intent = new Intent(getApplicationContext(), StartActivity.class);
        startActivity(intent);
        finish();
    }

    private void request() {
        if (ContextCompat.checkSelfPermission
                (this,
                        Manifest.permission.READ_CONTACTS
                ) == PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Granted", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    1
            );

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void generate(View view) {
        EditText editText = (EditText) findViewById(R.id.phonenumber);
        EditText editText1 = (EditText) findViewById(R.id.countrycode);
        String phone = editText.getText().toString().trim();
        String ccode = editText1.getText().toString().trim();
        if (phone.isEmpty()) {
            editText.setError("Enter phone number");
            editText.clearFocus();
        } else if (ccode.isEmpty()) {
            editText1.setError("Enter country code");
            editText1.clearFocus();
        } else if (phone.length() < 10) {
            editText.setError("Invalid phone number");
            editText.clearFocus();
        } else if (ccode.length() < 2) {
            editText1.setError("Invalid phone number");
            editText1.clearFocus();
        } else {
            String phoneNumber = "+" + ccode + phone;
            Intent intent = new Intent(getApplicationContext(), OtpActivity.class);
            intent.putExtra("phonenumber", phoneNumber);
            startActivity(intent);
        }

    }
}
