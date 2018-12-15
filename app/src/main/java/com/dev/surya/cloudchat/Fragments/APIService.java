package com.dev.surya.cloudchat.Fragments;

import com.dev.surya.cloudchat.Notifications.MyResponse;
import com.dev.surya.cloudchat.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAZWeMLqU:APA91bGd_0QB3Tsn_A-O0Yo9A-ekkEejFvruCcZbyRCuSTg01spelp1TgecEnaDb5TV7p_Bh4EgxYHknGJFHXJ-IwSfG-cBtv5vlIaOimb0JPbp09ULoiTn-E3w5wXLqSN60m7XXfb4e"
            }
    )

    @POST("fcm/send")
     Call<MyResponse> sendNotification(@Body Sender body);


}
