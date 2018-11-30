package com.vr.latlng;

import android.content.Context;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Admin on 11/29/2018.
 */

public class AddFriend {
    private com.google.firebase.database.Query query;
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance=Variables.mFirebaseInstance;
    protected void addFriend(final String friend_Key, Context context){
        mFirebaseDatabase = mFirebaseInstance.getReference(context.getResources().getString(R.string.friends_requests_received));


        Map<String, String> data = new HashMap<>();
        data.put(context.getResources().getString(R.string.request_from), Variables.user_key);
        data.put(context.getResources().getString(R.string.date), String.valueOf(new Date().getTime()));

        mFirebaseDatabase.child(friend_Key).push().setValue(data);
        mFirebaseDatabase = mFirebaseInstance.getReference(context.getResources().getString(R.string.friends_requests_sent));
        data.clear();
        data = new HashMap<>();
        data.put(context.getResources().getString(R.string.request_to), friend_Key);
        data.put(context.getResources().getString(R.string.date), String.valueOf(new Date().getTime()));

        mFirebaseDatabase.child(Variables.user_key).push().setValue(data);


    }
}
