package com.example.chatapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void back(View view) {
        Intent intent = new Intent(getApplicationContext(), StartActivity.class);
        startActivity(intent);
        finish();
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
