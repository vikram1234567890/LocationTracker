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

public class AddUsersInArrayList {
    private HashMap<Integer,Boolean> friend_extra_keys=new HashMap<>();

    private ArrayList arrayList=new ArrayList();
    private String Tag=Variables.Tag;
    private ArrayList friendExtraKey=new ArrayList();
    private FirebaseDatabase mFirebaseInstance=Variables.mFirebaseInstance;
    private DatabaseReference tempFirebaseDatabase;

    protected void addUsersInArrayList(String tag, final int id, final Context context, final TextView not_found, final RecyclerView recyclerView, final ArrayList<String > keys){
        DatabaseReference mFirebaseDatabase = mFirebaseInstance.getReference(tag).child(keys.get(id));

        mFirebaseDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name,date;
                    name=dataSnapshot.child(context.getResources().getString(R.string.name)).getValue(String.class);
                    date= dataSnapshot.child(context.getResources().getString(R.string.date)).getValue(String.class);
                    if (name==null){
                        name="";
                    }
                    if (date==null) {
                        date="";
                    }
                    arrayList.add(new list_item( null,null,keys.get(id)
                            , name,date));
                    if (Tag.equals(context.getResources().getString(R.string.tracking_you))) {

                        for (int i = 0; i < friendExtraKey.size(); i++) {
                            if (keys.get(id).equals(friendExtraKey.get(i))) {
                                friend_extra_keys.put(arrayList.size() - 1, true);
                                break;
                            }
                        }
                    }

                }
                int i = id + 1;
                if(i < keys.size()) {

                    addUsersInArrayList(context.getResources().getString(R.string.users), i, context, not_found, recyclerView, keys);
                }
                if (id == keys.size() - 1 || !dataSnapshot.exists()) {
                    new SetRecyclerAdapter().setAdapter(arrayList,not_found,context,recyclerView,friend_extra_keys);


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("Error list data retrive", "loadPost:onCancelled", databaseError.toException());

            }
        });
    }
}
