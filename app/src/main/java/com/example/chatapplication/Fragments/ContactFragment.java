package com.example.chatapplication.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import com.example.chatapplication.Adapters.CountryToIsoPrefix;
import com.example.chatapplication.Adapters.UserListAdapter;
import com.example.chatapplication.Adapters.UserObject;
import com.example.chatapplication.R;
import com.example.chatapplication.UserChatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ContactFragment extends Fragment {
    private RecyclerView mUserList;
    private RecyclerView.Adapter mUserListAdapter;
    private UserListAdapter.RecyclerViewClickListener listener;
    private RecyclerView.LayoutManager mUserListLayoutManager;
    ArrayList<UserObject> contactList; //userList;
    View view;


    private void layoutAnimation(RecyclerView recyclerView) {
        Context context = recyclerView.getContext();
        LayoutAnimationController layoutAnimationController =
                AnimationUtils.loadLayoutAnimation(context, R.anim.layout_itemdown);
        recyclerView.setLayoutManager(mUserListLayoutManager);
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }


    private void getContactList() {
        Cursor phones = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (phones.moveToNext()) {
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            phone = phone.replaceAll("\\s", "");
            UserObject mcontact = new UserObject(name, phone);
            contactList.add(mcontact);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setOnClickListener();
        view = inflater.inflate(R.layout.fragment_contact, container, false);
        mUserList = (RecyclerView) view.findViewById(R.id.recycler);
        contactList = new ArrayList<>();
        mUserList.setNestedScrollingEnabled(false);
        mUserList.setHasFixedSize(false);
        mUserListLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        mUserList.setLayoutManager(mUserListLayoutManager);
        mUserListAdapter = new UserListAdapter(contactList, listener);
        mUserList.setAdapter(mUserListAdapter);
        mUserListAdapter.notifyDataSetChanged();
        getContactList();
        getPermissions();
        return view;
    }

    private void setOnClickListener() {
        listener = new UserListAdapter.RecyclerViewClickListener() {
            @Override
            public void onClick(View v, int position) {
                Intent intent = new Intent(getContext().getApplicationContext(), UserChatActivity.class);
                intent.putExtra("name", contactList.get(position).getName());
                intent.putExtra("number",contactList.get(position).getPhone());
                startActivity(intent);
            }
        };
    }

    private void getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS}, 1);
        }
    }
}
