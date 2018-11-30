package com.vr.latlng;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Admin on 11/29/2018.
 */

public class AcceptFriendRequest {

    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance=Variables.mFirebaseInstance;

    protected void acceptFriendRequest(final String friend_Key, final Context context){
        mFirebaseDatabase = mFirebaseInstance.getReference(context.getResources().getString(R.string.friends));

        final Query query = mFirebaseDatabase.child(Variables.user_key).orderByChild(context.getResources().getString(R.string.friend)).equalTo(friend_Key);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    Map<String, String> data = new HashMap<>();
                    data.put(context.getResources().getString(R.string.friend), friend_Key);
                    data.put(context.getResources().getString(R.string.date), String.valueOf(new Date().getTime()));

                    mFirebaseDatabase.child(Variables.user_key).push().setValue(data);
                    data.clear();
                    data.put(context.getResources().getString(R.string.friend), Variables.user_key);
                    data.put(context.getResources().getString(R.string.date), String.valueOf(new Date().getTime()));


                    mFirebaseDatabase.child(Variables.friend_key).push().setValue(data);

                    Variables.friend_key=null;
                    new DeleteFriendRequest().deleteFriendRequest(friend_Key,context);
                }


                query.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("", "onCancelled", databaseError.toException());
            }
        });
    }

}
