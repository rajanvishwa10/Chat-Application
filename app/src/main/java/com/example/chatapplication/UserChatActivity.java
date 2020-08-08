package com.example.chatapplication;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapplication.Adapters.UserListAdapter;
import com.example.chatapplication.Adapters.UserObject;

import java.util.ArrayList;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class UserChatActivity extends AppCompatActivity {
    Toolbar toolbar;
    EmojiconEditText editText;
    ImageView imageView;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_chat);
        String name = "";
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            name = extras.getString("name");
        }
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(name);
        editText = (EmojiconEditText) findViewById(R.id.sendmess);
        imageView = findViewById(R.id.imagebutton);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);



    }


    public void sendmess(View view) {
        String message = editText.getText().toString().trim();
        Toast.makeText(this, ""+message, Toast.LENGTH_SHORT).show();
        TextView textView = findViewById(R.id.chats);
        textView.setText(message);
        editText.setText(null);
    }

    public void emojiview(View view) {
        EmojIconActions emojIconActions = new EmojIconActions(this,view, editText,imageView);
        emojIconActions.ShowEmojIcon();
        emojIconActions.setUseSystemEmoji(true);
        editText.setUseSystemDefault(true);
        //Toast.makeText(this, "Clicked!!", Toast.LENGTH_SHORT).show();
    }
}