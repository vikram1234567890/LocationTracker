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

/**
 * Created by Admin on 11/29/2018.
 */

public class GetNotification {
    private com.google.firebase.database.Query query;
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance=Variables.mFirebaseInstance;
    private ArrayList<String > keys=new ArrayList<>();
    private ArrayList<list_item> arrayList=new ArrayList<>();

    private void getFriendRequests(final Context context , final TextView not_found, final RecyclerView recyclerView){

        mFirebaseDatabase = mFirebaseInstance.getReference(context.getResources().getString(R.string.friends_requests_received));

        mFirebaseDatabase.child(Variables.user_key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               /*   StackTraceElement stackTraceElement[]= Thread.currentThread().getStackTrace();
                for (StackTraceElement s:stackTraceElement) {
                    Log.i("stack trace", s.toString());
                }*/

                for (DataSnapshot users: dataSnapshot.getChildren()) {
                    keys.add(users.child(context.getResources().getString(R.string.request_from)).getValue(String.class));

                }
                if (keys.size()==0){
                    new SetRecyclerAdapter().setAdapter(arrayList,not_found,context,recyclerView,null);


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

    protected void getNotification(final Context context , final TextView not_found, final RecyclerView recyclerView){

        getFriendRequests(context ,  not_found,  recyclerView);
        }
}
