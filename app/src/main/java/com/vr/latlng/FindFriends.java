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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by Admin on 11/29/2018.
 */

public class FindFriends {
    DatabaseReference tempFirebaseDatabase;

     ArrayList arrayList=new ArrayList();
     ArrayList<String> keys=new ArrayList<>();
     HashMap<Integer, Boolean> friend_extra_keys=new HashMap<>();
      ArrayList friendExtraKey=new ArrayList();
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance=Variables.mFirebaseInstance;
    protected void getAllUsers(final Context context , final TextView not_found, final RecyclerView recyclerView){

        //get keys of users registered in friends

        tempFirebaseDatabase = mFirebaseInstance.getReference(context.getResources().getString(R.string.friends)).child(Variables.user_key);
        tempFirebaseDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                
                
                

                for (DataSnapshot users: dataSnapshot.getChildren()) {
                  keys.add(users.child(context.getResources().getString(R.string.friend)).getValue(String.class));

                }
                //get friend request recieved list and dont show in our list
                tempFirebaseDatabase = mFirebaseInstance.getReference(context.getResources().getString(R.string.friends_requests_received)).child(Variables.user_key);
                tempFirebaseDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot users: dataSnapshot.getChildren()) {
                            keys.add(users.child(context.getResources().getString(R.string.request_from)).getValue(String.class));

                        }
                        //get friend reuest received list
                        tempFirebaseDatabase = mFirebaseInstance.getReference(context.getResources().getString(R.string.friends_requests_sent)).child(Variables.user_key);
                        tempFirebaseDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(final DataSnapshot dataSnapshot) {

                                for (DataSnapshot users : dataSnapshot.getChildren()) {
                                    friendExtraKey.add(users.child(context.getResources().getString(R.string.request_to)).getValue(String .class));

                                }

                                //get all users list
                                mFirebaseDatabase = mFirebaseInstance.getReference(context.getResources().getString(R.string.users));

                                mFirebaseDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(final DataSnapshot dataSnapshot) {

                                        for (DataSnapshot users : dataSnapshot.getChildren()) {
                                            if (!users.getKey().equals(Variables.user_key)) {
                                                boolean keyfound = false;
                                                for (int i = 0; i < keys.size(); i++) {
                                                    if (users.getKey().equals(keys.get(i))) {
                                                        keyfound = true;
                                                        break;
                                                    }
                                                }
                                                if (!keyfound) {//friend not added
                                                    for (int i = 0; i < friendExtraKey.size(); i++) {
                                                        if (users.getKey().equals(friendExtraKey.get(i))) {
                                                            keyfound = true;
                                                            break;
                                                        }
                                                    }
                                                    if (keyfound) {//friend request has been sent
                                                        arrayList.add(new list_item( null,null, users.getKey()
                                                                , users.child(context.getResources().getString(R.string.name)).getValue(String.class),  users.child(context.getResources().getString(R.string.date)).getValue(String.class)));
                                                        friend_extra_keys.put(arrayList.size() - 1, true);

                                                    }else {//friend request not  sent

                                                        arrayList.add(new list_item(null,null,  users.getKey()
                                                                , users.child(context.getResources().getString(R.string.name)).getValue(String.class),  users.child(context.getResources().getString(R.string.date)).getValue(String.class)));
                                                    }

                                                }
                                            }
                                        }

                                        new SetRecyclerAdapter().setAdapter(arrayList,not_found,context,recyclerView,friend_extra_keys);


                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });



                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                // Getting Post failed, log a message
                                Log.w("Error list data retrive", "loadPost:onCancelled", databaseError.toException());

                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }
}
