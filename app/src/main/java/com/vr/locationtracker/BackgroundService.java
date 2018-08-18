package com.vr.locationtracker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class BackgroundService extends Service {
    private  FirebaseUser currentUser;
    private  FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseInstance;

    private DatabaseReference mFirebaseDatabase;
    private Query query;

    public BackgroundService() {





    }
    protected void changeSharedPref(String key, String value, String shared_pref_name)
    {

        SharedPreferences sharedPreferences = getSharedPreferences(shared_pref_name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, "");
        editor.putString(key, value);
        editor.commit();
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mFirebaseInstance = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();

        currentUser = mAuth.getCurrentUser();

        Variables.user_key = currentUser.getUid();
        changeSharedPref("key",Variables.user_key,getResources().getString(R.string.system_generated));

        if (!new CommonMethods().isMyServiceRunning(ListenerService.class)) {

            startService(new Intent(this, ListenerService.class));
        }

        online();

        return START_NOT_STICKY;
    }

    void online(){
        FirebaseDatabase mFirebaseInstance;

        final DatabaseReference mFirebaseDatabase;

        mFirebaseInstance = FirebaseDatabase.getInstance();

        mFirebaseDatabase = mFirebaseInstance.getReference(getResources().getString(R.string.online)).child(Variables.user_key);
           mFirebaseDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    Map<String, String> data = new HashMap<>();
                     data.put(getResources().getString(R.string.date), String.valueOf(new Date().getTime()));
                    mFirebaseDatabase.setValue(data);

                }

                updateChecker();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("", "onCancelled", databaseError.toException());
            }
        });
    }
    void updateChecker(){
        mFirebaseDatabase = mFirebaseInstance.getReference(getResources().getString(R.string.app_name));
        mFirebaseDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){

                    addVersionToDb();
                }else {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        if (Long.parseLong(data.getValue(String.class))>Long.parseLong(new CommonMethods().currentVersionCode()))
                        {
                            new CommonMethods().updateMessage();


                            break;
                        }else  if (Long.parseLong(data.getValue(String.class))<Long.parseLong(new CommonMethods().currentVersionCode()))
                        {

                            addVersionToDb();
                            break;
                        }else {
                            listenUserTracking();
                            break;
                        }

                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("Error list data retrive", "loadPost:onCancelled", databaseError.toException());

            }
        });

    }
    private void addVersionToDb(){
        mFirebaseDatabase = mFirebaseInstance.getReference(getResources().getString(R.string.app_name));
        query = mFirebaseDatabase.orderByChild(getResources().getString(R.string.version_code)).equalTo(new CommonMethods().currentVersion());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    Map<String, String> data = new HashMap<>();
                    data.put(getResources().getString(R.string.version_code), new CommonMethods().currentVersionCode());
                    mFirebaseDatabase.setValue(data);

                }

                listenUserTracking();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("", "onCancelled", databaseError.toException());
            }
        });
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        mFirebaseDatabase = mFirebaseInstance.getReference(getResources().getString(R.string.online)).child(Variables.user_key);
        mFirebaseDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataSnapshot.getRef().removeValue();


                          }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("", "onCancelled", databaseError.toException());
            }
        });
    }
    void listenUserTracking(){
       String  user_key = Variables.user_key;

        mFirebaseDatabase = mFirebaseInstance.getReference(getResources().getString(R.string.user_tracking_me));
        mFirebaseDatabase = mFirebaseDatabase.child(user_key);
        mFirebaseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    new Notifications(BackgroundService.this).shownotification(RecycleViewLayout.class, Variables.context.getResources().getString(R.string.tracking_you), "Tracking You", "Your friends are tracking you.Click to see");
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e("", "Failed to read user", error.toException());
            }
        });
    }

}
