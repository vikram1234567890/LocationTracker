package com.vr.latlng;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Admin on 11/29/2018.
 */

public class MyFriends {
    private com.google.firebase.database.Query query;
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance=Variables.mFirebaseInstance;
    private ArrayList keys=new ArrayList();
    private ArrayList<list_item> arrayList=new ArrayList<>();
    private HashMap<Integer,Boolean> friend_extra_keys=new HashMap<>();

    protected void getFriends(final Context context , final TextView not_found, final RecyclerView recyclerView){

        mFirebaseDatabase = mFirebaseInstance.getReference(context.getResources().getString(R.string.friends));

        mFirebaseDatabase.child(Variables.user_key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot users: dataSnapshot.getChildren()) {
                    keys.add(users.child(context.getResources().getString(R.string.friend)).getValue(String.class));

                }
                if (keys.size()==0){
                    new SetRecyclerAdapter().setAdapter(arrayList,not_found,context,recyclerView,friend_extra_keys);

                }else
                    new AddUsersInArrayList().addUsersInArrayList(context.getResources().getString(R.string.users),0,context, not_found, recyclerView, keys);




                mFirebaseDatabase.removeEventListener(this);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("Error list data retrive", "loadPost:onCancelled", databaseError.toException());

            }
        });

    }
}
