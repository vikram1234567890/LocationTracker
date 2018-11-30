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

public class RemoveFriend {
    private com.google.firebase.database.Query query;
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance=Variables.mFirebaseInstance;
    protected void removeFriend(String friend_Key, Context context){
        mFirebaseDatabase = mFirebaseInstance.getReference(context.getResources().getString(R.string.friends));

        query = mFirebaseDatabase.child(Variables.user_key).orderByChild(context.getResources().getString(R.string.friend)).equalTo(friend_Key);

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
        query = mFirebaseDatabase.child(friend_Key).orderByChild(context.getResources().getString(R.string.friend)).equalTo(Variables.user_key);

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
