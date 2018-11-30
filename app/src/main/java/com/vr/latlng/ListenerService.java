package com.vr.latlng;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ListenerService extends Service {
    private FirebaseDatabase mFirebaseInstance;
    private static String user_key;
    
    private DatabaseReference mFirebaseDatabase;
    public ListenerService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {



            mFirebaseInstance = FirebaseDatabase.getInstance();
            user_key = getSharedPref("key", getResources().getString(R.string.system_generated));

            listenFriendsRequests();
        return START_STICKY;
    }
    protected String getSharedPref(String key, String shared_pref_name)
    {
        SharedPreferences sharedPreferences = getSharedPreferences(shared_pref_name, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key,"");
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    private void listenFriendsRequests(){
        mFirebaseDatabase = mFirebaseInstance.getReference(getResources().getString(R.string.friends_requests_received));
    if (user_key.trim().length()!=0)
        mFirebaseDatabase.child(user_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    new Notifications(getApplicationContext()).shownotification(RecycleViewLayout.class, getResources().getString(R.string.notifications), "New Friend Request", "Click to accept");
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
