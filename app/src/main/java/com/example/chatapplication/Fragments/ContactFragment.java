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
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private RecyclerView mUserList;
    private RecyclerView.Adapter mUserListAdapter;
    private UserListAdapter.RecyclerViewClickListener listener;
    private RecyclerView.LayoutManager mUserListLayoutManager;
    ArrayList<UserObject> contactList; //userList;
    View view;

    public ContactFragment() {

    }

    private void layoutAnimation(RecyclerView recyclerView) {
        Context context = recyclerView.getContext();
        LayoutAnimationController layoutAnimationController =
                AnimationUtils.loadLayoutAnimation(context, R.anim.layout_itemdown);
        recyclerView.setLayoutManager(mUserListLayoutManager);
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }

    public static ContactFragment newInstance(String param1, String param2) {
        ContactFragment fragment = new ContactFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        contactList = new ArrayList<>();
       //userList = new ArrayList<>();
        getPermissions();
    }

   // private String getCountryISO(){
    //    String iso = null;
       // TelephonyManager telephonyManager = (TelephonyManager) getActivity().getApplicationContext().getSystemService(getActivity().getApplicationContext().TELEPHONY_SERVICE);
        ////if(telephonyManager.getNetworkCountryIso()!= null ){
            //if(!telephonyManager.getNetworkCountryIso().toString().equals("")){
            //    iso = telephonyManager.getNetworkCountryIso().toString();
         //   }

   //     }
       // return CountryToIsoPrefix.getPhone(iso);
  //  }

    private void getContactList() {
     //   String ISOpre = getCountryISO();
        Cursor phones = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (phones.moveToNext()) {
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//
           // phone = phone.replace(" ", "");
       //     phone = phone.replace("-", "");
       //     phone = phone.replace("(", "");
        //    phone = phone.replace(")", "");

        //    if(!String.valueOf(phone.charAt(0)).equals("+")){
        //        phone = ISOpre+phone;
         ///   }

            UserObject mcontact = new UserObject(name, phone);
            contactList.add(mcontact);
            //getUserDetails(mcontact);
        }
    }

   // private void getUserDetails(UserObject mcontact) {
      //  final DatabaseReference userDb = FirebaseDatabase.getInstance().getReference().child("user");
      //  Query query = userDb.orderByChild("phone").equalTo(mcontact.getPhone());
      //  query.addListenerForSingleValueEvent(new ValueEventListener() {
       //     @Override
          //  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
          //      if(dataSnapshot.exists()){
          //          String phone = "",
               //             name = "";
               //     for(DataSnapshot childSnapshot : dataSnapshot.getChildren()){
              //          if(childSnapshot.child("phone").getValue()!=null){
               //             phone= childSnapshot.child("phone").getValue().toString();
                //        }
                //        if(childSnapshot.child("name").getValue()!=null){
                 //           phone= childSnapshot.child("name").getValue().toString();
                 //       }

              //          UserObject userObject = new UserObject(name,phone);
              ///          userList.add(userObject);
              //          mUserListAdapter.notifyDataSetChanged();
               //         return;
                //    }

              //  }
        //    }

        //    @Override
          //  public void onCancelled(@NonNull DatabaseError databaseError) {
//
        //    }
        //});
   // }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setOnClickListener();
        view = inflater.inflate(R.layout.fragment_contact, container, false);
        mUserList = (RecyclerView) view.findViewById(R.id.recycler);
        mUserList.setNestedScrollingEnabled(false);
        mUserList.setHasFixedSize(false);
        mUserListLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        mUserList.setLayoutManager(mUserListLayoutManager);
        mUserListAdapter = new UserListAdapter(contactList, listener);
        mUserList.setAdapter(mUserListAdapter);
        mUserListAdapter.notifyDataSetChanged();
        getContactList();
        return view;
    }

    private void setOnClickListener() {
        listener =  new UserListAdapter.RecyclerViewClickListener() {
            @Override
            public void onClick(View v, int position) {
                Intent intent = new Intent(getContext().getApplicationContext(), UserChatActivity.class);
                intent.putExtra("name", contactList.get(position).getName());
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
