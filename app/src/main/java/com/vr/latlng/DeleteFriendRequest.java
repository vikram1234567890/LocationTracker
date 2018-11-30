package com.vr.latlng;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * Created by Admin on 11/29/2018.
 */

public class DeleteFriendRequest {
    private com.google.firebase.database.Query query;
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance=Variables.mFirebaseInstance;

    protected void deleteFriendRequest(String friend_Key, Context context){
        mFirebaseDatabase = mFirebaseInstance.getReference(context.getResources().getString(R.string.friends_requests_received));
        query = mFirebaseDatabase.child(Variables.user_key).orderByChild(context.getResources().getString(R.string.request_from)).equalTo(friend_Key);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data: dataSnapshot.getChildren()) {
                    data.getRef().removeValue();

                }

                query.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("", "onCancelled", databaseError.toException());
            }
        });
        mFirebaseDatabase = mFirebaseInstance.getReference(context.getResources().getString(R.string.friends_requests_sent));

        query = mFirebaseDatabase.child(friend_Key).orderByChild(context.getResources().getString(R.string.request_to)).equalTo(Variables.user_key);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data: dataSnapshot.getChildren()) {
                    data.getRef().removeValue();

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
