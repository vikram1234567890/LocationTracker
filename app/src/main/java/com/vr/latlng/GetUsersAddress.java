package com.vr.latlng;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by Admin on 11/29/2018.
 */

public class GetUsersAddress {
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance=Variables.mFirebaseInstance;
    protected void getUsersAddress(String key, final TextView textView, final Context context, final ArrayList<list_item> arrayList, final int position){
        mFirebaseDatabase = mFirebaseInstance.getReference(context.getResources().getString(R.string.users_locations)).child(key);

        mFirebaseDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot data) {
                try {
                    String address, lat, lng;
                    lat = data.child(context.getResources().getString(R.string.lat)).getValue(String.class);
                    lng = data.child(context.getResources().getString(R.string.lng)).getValue(String.class);
                    if (!data.exists() || lat == null || lng == null) {
                        address = "Unknown";
                    } else {
                        address = new CommonMethods().getAddress(Double.parseDouble(lat), Double.parseDouble(lng));
                    }
                    arrayList.get(position).user_location = address;
                    textView.setText(address);
                }catch (Exception e){

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
