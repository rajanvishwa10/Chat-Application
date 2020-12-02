package com.example.chatapplication.Fragments;

import com.example.chatapplication.Notification.MyResponse;
import com.example.chatapplication.Notification.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                 "Content-Type:application/json",
                    "Authorization:key=AAAA_dUmwyo:APA91bHNBkl4NYBYDdgUwdZGXsIjTkWa4N0UNkOgRbiNdSqpIzYGvb3n4o5Rjo4pkNWstbzAePDjgNVomE9qJBP_n0_3vHZbwqaZomnQ89EnNov7FrRmOkqUIXfo-vfFKAzaXbfjd3Qj"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
