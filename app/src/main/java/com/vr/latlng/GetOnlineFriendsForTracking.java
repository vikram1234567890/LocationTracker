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

public class GetOnlineFriendsForTracking {
    private HashMap<Integer,Boolean> friend_extra_keys=new HashMap<>();
    private ArrayList<String > keys=new ArrayList<>();
    private ArrayList arrayList=new ArrayList();

    private ArrayList friendExtraKey=new ArrayList();
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance=Variables.mFirebaseInstance;
    private DatabaseReference tempFirebaseDatabase;

    protected void getOnlineFriendsForTracking(final Context context, final TextView not_found, final RecyclerView recyclerView){

        mFirebaseDatabase = mFirebaseInstance.getReference(context.getResources().getString(R.string.friends));

        mFirebaseDatabase.child(Variables.user_key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot users: dataSnapshot.getChildren()) {
                    keys.add(users.child(context.getResources().getString(R.string.friend)).getValue(String.class));

                }
                tempFirebaseDatabase = mFirebaseInstance.getReference(context.getResources().getString(R.string.online));

                tempFirebaseDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ArrayList<String> temp=new ArrayList<>();

                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            //keep only online users

                            for (int j=0;j<keys.size();j++) {
                                for (int i = 0; i < keys.size(); i++) {
                                    if (keys.get(i).equals(data.getKey())) {
                                        temp.add(keys.get(i));
                                        keys.remove(i);
                                    }
                                }
                            }

                        }


                        keys.clear();
                        keys.addAll(temp);

                        mFirebaseDatabase = mFirebaseInstance.getReference(context.getResources().getString(R.string.you_tracking_user));

                        mFirebaseDatabase.child(Variables.user_key).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                for (DataSnapshot data : dataSnapshot.getChildren()) {


                                    friendExtraKey.add(data.child(context.getResources().getString(R.string.track_to)).getValue(String .class));

                                }
                                if (keys.size()==0){
                                    new SetRecyclerAdapter().setAdapter(arrayList,not_found,context,recyclerView,friend_extra_keys);


                                }else
                                    new AddUsersInArrayList().addUsersInArrayList(context.getResources().getString(R.string.users),0,context, not_found, recyclerView, keys);


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
                        Log.e("", "onCancelled", databaseError.toException());
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




}
