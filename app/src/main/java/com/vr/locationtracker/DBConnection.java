package com.vr.locationtracker;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.media.Image;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by Admin on 7/12/2018.
 */

public class DBConnection   implements Serializable {
    private DatabaseReference mFirebaseDatabase, tempFirebaseDatabase, friendFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;

    protected static Marker marker;
    protected static Marker friend_marker;
    private GoogleMap mMap;
    private FirebaseAuth mAuth;
    private LatLng my_latLng;
    private FirebaseUser currentUser;
    private Query query;
    private Context context;
    private AlertDialog alertDialog;
    PopupWindow popupWindow;
    View popupView;
    int mCurrentX, mCurrentY;
    private LinearLayout maps_layout;
    protected static ImageView popupProfile;
    private static TextView popupUserName, popupUserDistance;
    private Bitmap bitmap;
    private boolean popupWindowShown;
    private boolean my_zoom_once,friend_zoom_once;
     static Bitmap myBitmap,friendBitmap;
    protected void startDb() {


        // get reference to 'users' node
        mFirebaseDatabase = mFirebaseInstance.getReference(context.getResources().getString(R.string.users_locations));
        createUser(Variables.currentLatitude, Variables.currentLongitude);

    }

    public DBConnection() {
        context = Variables.context;
        friend_marker = null;
        popupProfile=null;
        mAuth = FirebaseAuth.getInstance();
        maps_layout = MapsActivity.maps_layout;
        currentUser = mAuth.getCurrentUser();

        mMap = Variables.mMap;

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (marker.equals(friend_marker)) {
                    friend_marker.showInfoWindow();
                    showDistance();
                }else if (marker.equals(DBConnection.this.marker)){
                    DBConnection.this.marker.showInfoWindow();
                }
                return true;
            }
        });
        mFirebaseInstance = FirebaseDatabase.getInstance();
    }

    private void showDistance() {

        if (!popupWindowShown) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
            popupView = layoutInflater.inflate(R.layout.custom_friend_details, null);
            popupUserName = popupView.findViewById(R.id.friend_name);

            popupUserDistance = popupView.findViewById(R.id.distance);
            popupProfile = popupView.findViewById(R.id.friend_photo);
            ImageView close = popupView.findViewById(R.id.close);

            popupProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try{
                    popupProfile.invalidate();
                    BitmapDrawable drawable = (BitmapDrawable) popupProfile.getDrawable();
                    Bitmap bitmap = drawable.getBitmap();
                    new CommonMethods().zoomImage(bitmap);
                    }catch (Exception e){

                    }
                }
            });
            Button stop_tracking = popupView.findViewById(R.id.stop_tracking);

            if (Variables.friend_name != null) {
                popupUserName.setText(Variables.friend_name);
            }
            distanceoOnPopup();
             if (Variables.friend_photo != null) {
                Bitmap bitmap = Bitmap.createScaledBitmap(Variables.friend_photo, 150, 150, false);

                popupProfile.setImageBitmap(bitmap);
            }
            popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            stop_tracking.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Variables.start_tracking = false;
                    new CommonMethods().stopTracking(Variables.friend_key, Variables.user_key);
                    stopTracking();
                    popupWindow.dismiss();
                    popupWindowShown = false;
                }
            });
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    popupWindow.dismiss();

                    popupWindowShown = false;
                }
            });


            mCurrentX = 20;
            mCurrentY = 100;
            popupWindow.showAtLocation(maps_layout, Gravity.NO_GRAVITY, mCurrentX, mCurrentY);


            popupView.setOnTouchListener(new View.OnTouchListener() {
                private float mDx;
                private float mDy;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int action = event.getAction();
                    if (action == MotionEvent.ACTION_DOWN) {
                        mDx = mCurrentX - event.getRawX();
                        mDy = mCurrentY - event.getRawY();
                    } else if (action == MotionEvent.ACTION_MOVE) {
                        mCurrentX = (int) (event.getRawX() + mDx);
                        mCurrentY = (int) (event.getRawY() + mDy);
                        popupWindow.update(mCurrentX, mCurrentY, -1, -1);
                    }
                    return true;
                }
            });
            popupWindowShown = true;
        }
    }
    void distanceoOnPopup(){

        if (popupUserDistance!=null) {
            StackTraceElement stackTraceElement[]= Thread.currentThread().getStackTrace();
            for (StackTraceElement s:stackTraceElement) {
                Log.i("stack trace", s.toString());
            }
               popupUserDistance.setText("Distance from you " + new DecimalFormat("#.#").format(new CommonMethods().CalculationByDistance(Variables.myLatLng, Variables.friendLatLng)) + " Km");
        }
    }
    private void createUser(String lat, String lng) {


        Map<String, String> data = new HashMap<>();
        data.put(context.getResources().getString(R.string.lat), lat);
        data.put(context.getResources().getString(R.string.lng), lng);
        data.put(context.getResources().getString(R.string.date), String.valueOf(new Date().getTime()));

        mFirebaseDatabase.child(Variables.user_key).setValue(data);
        tempFirebaseDatabase = mFirebaseInstance.getReference(context.getResources().getString(R.string.users)).child(Variables.user_key).child(context.getResources().getString(R.string.user_email));

        tempFirebaseDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    insertUserData();
                }
try {

    addUserChangeListener();
}catch (Exception e){

}
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void insertUserData() {


        DatabaseReference userNameRef = mFirebaseDatabase.child(context.getResources().getString(R.string.users)).child(Variables.user_key);
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    //create new user
                    tempFirebaseDatabase = mFirebaseInstance.getReference(context.getResources().getString(R.string.users));
                    Map<String, String> data = new HashMap<>();
                    data.put(context.getResources().getString(R.string.name), currentUser.getDisplayName());

                    data.put(context.getResources().getString(R.string.description), "");
                    data.put(context.getResources().getString(R.string.user_email), currentUser.getEmail());
                    data.put(context.getResources().getString(R.string.phone_no), "");
                    data.put(context.getResources().getString(R.string.date), String.valueOf(new Date().getTime()));

                    tempFirebaseDatabase.child(Variables.user_key).setValue(data);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        userNameRef.addListenerForSingleValueEvent(eventListener);
    }
    private void getMarkerIcon(){
        // User data change listener
        mFirebaseInstance.getReference(context.getResources().getString(R.string.users)).child(Variables.user_key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {


                new UploadDownloadImages().download(Variables.user_key + ".png", "user", null);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("Error list data retrive", "loadPost:onCancelled", databaseError.toException());

            }
        });
    }
    private void addUserChangeListener() {
        getMarkerIcon();
        mFirebaseDatabase = mFirebaseDatabase.child(Variables.user_key);
        mFirebaseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                        Variables.currentLatitude = dataSnapshot.child(context.getResources().getString(R.string.lat)).getValue(String.class);
                        Variables.currentLongitude = dataSnapshot.child(context.getResources().getString(R.string.lng)).getValue(String.class);


            if (marker != null) {
                marker.remove();

            }
                my_latLng = new LatLng(Double.parseDouble(Variables.currentLatitude), Double.parseDouble(Variables.currentLongitude));

            Variables.myLatLng = my_latLng;
            if (!my_zoom_once) {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(my_latLng));
                my_zoom_once = true;
            }

            marker = mMap.addMarker(new MarkerOptions().position(my_latLng));


            // Showing the current location in Google Map
            // mMap.moveCamera(CameraUpdateFactory.newLatLng(my_latLng));
            // mMap.animateCamarkerra(CamarkerraUpdateFactory.newLatLngZoom(my_latLng, 15));
            marker.setPosition(my_latLng);
            marker.setTitle("YOU");
            if (myBitmap!=null){
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(new CommonMethods().getCustomMapMarker(myBitmap)));
            }
            if (!marker.isInfoWindowShown()) {
                marker.showInfoWindow();
            }

            distanceoOnPopup();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read user", error.toException());
            }
        });


    }

    protected void friendTrack() {
        if (!Variables.NO_RATE &&  new CommonMethods().getSharedPref(context.getResources().getString(R.string.rate),context.getResources().getString(R.string.app_name)).equals("") ) {
            new CommonMethods().rateMessage();
        }
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        Variables.friend_name = "Loading name...";
        mFirebaseInstance = FirebaseDatabase.getInstance();

        tempFirebaseDatabase = mFirebaseInstance.getReference(context.getResources().getString(R.string.user_tracking_me)).child(Variables.friend_key);

        query = tempFirebaseDatabase.orderByChild(context.getResources().getString(R.string.tracked_by)).equalTo(Variables.user_key);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Map<String, String> data = new HashMap<>();
                    data.put(context.getResources().getString(R.string.tracked_by), Variables.user_key);
                    data.put(context.getResources().getString(R.string.date), String.valueOf(new Date().getTime()));

                    tempFirebaseDatabase.push().setValue(data);

                }
                tempFirebaseDatabase = mFirebaseInstance.getReference(context.getResources().getString(R.string.you_tracking_user)).child(Variables.user_key);
                query = tempFirebaseDatabase.orderByChild(context.getResources().getString(R.string.track_to)).equalTo(Variables.friend_key);

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            Map<String, String> data = new HashMap<>();
                            data.put(context.getResources().getString(R.string.track_to), Variables.friend_key);
                            data.put(context.getResources().getString(R.string.date), String.valueOf(new Date().getTime()));

                            tempFirebaseDatabase.push().setValue(data);
                        }
                        friendFirebaseDatabase = mFirebaseInstance.getReference(context.getResources().getString(R.string.users_locations));

                        friendFirebaseDatabase = friendFirebaseDatabase.child(Variables.friend_key);
                        friendFirebaseDatabase.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {


                                tempFirebaseDatabase = mFirebaseInstance.getReference(context.getResources().getString(R.string.you_tracking_user)).child(Variables.user_key);
                                query = tempFirebaseDatabase.orderByChild(context.getResources().getString(R.string.track_to)).equalTo(Variables.friend_key);

                                query.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (!dataSnapshot.exists()) {
                                            Variables.start_tracking = false;

                                            query.removeEventListener(this);
                                            query.removeEventListener(this);

                                            query.removeEventListener(this);
                                              stopTracking();
                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });


                                if (friend_marker != null) {
                                    friend_marker.remove();
                                       }
                                my_latLng = new LatLng(Double.parseDouble(dataSnapshot.child(context.getResources().getString(R.string.lat)).getValue(String.class)), Double.parseDouble(dataSnapshot.child(context.getResources().getString(R.string.lng)).getValue(String.class)));
                                Variables.friendLatLng = my_latLng;
                                friend_marker = mMap.addMarker(new MarkerOptions().position(my_latLng));
                               if (!friend_zoom_once){
                                   mMap.moveCamera(CameraUpdateFactory.newLatLng(my_latLng));
                                   friend_zoom_once=true;
                               }
                                mFirebaseDatabase = mFirebaseInstance.getReference(context.getResources().getString(R.string.users)).child(Variables.friend_key);

                                mFirebaseDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(final DataSnapshot dataSnapshot) {

                                        Variables.friend_name = dataSnapshot.child(context.getResources().getString(R.string.name)).getValue(String.class);

                                        new UploadDownloadImages().download(Variables.friend_key + ".png", "friend", null);
                                        popupUserName.setText(Variables.friend_name);
                                        friend_marker.setTitle(Variables.friend_name);
                                        friend_marker.setTitle(Variables.friend_name);

                                        if (!friend_marker.isInfoWindowShown()) {
                                            friend_marker.showInfoWindow();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        // Getting Post failed, log a message
                                        Log.w("Error list data retrive", "loadPost:onCancelled", databaseError.toException());

                                    }
                                });


                                // Showing the current location in Google Map
                                // mMap.moveCamera(CameraUpdateFactory.newLatLng(my_latLng));
                                // mMap.animateCamarkerra(CamarkerraUpdateFactory.newLatLngZoom(my_latLng, 15));
                                friend_marker.setPosition(my_latLng);

                                progressDialog.dismiss();

                          /*      if (!Variables.start_tracking) {
                                    friendFirebaseDatabase.removeEventListener(this);

                                }*/
                                showDistance();
                               distanceoOnPopup();
        if (!Variables.start_tracking){
            friendFirebaseDatabase.removeEventListener(this);

        }
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                // Failed to read value
                                Log.e(TAG, "Failed to read user", error.toException());
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
                Log.e("", "onCancelled", databaseError.toException());
            }
        });


    }

    private void stopTracking() {
        Toast toast;
        toast=    Toast.makeText(context, "Disconnected", Toast.LENGTH_SHORT);

        friend_marker.remove();
        popupWindow.dismiss();
        toast.show();
    }

    protected void updateUser(String lat, String lng) {
        mFirebaseDatabase = mFirebaseInstance.getReference(context.getResources().getString(R.string.users_locations));

        Map<String, String> data = new HashMap<>();

            data.put(context.getResources().getString(R.string.lat), lat);
            data.put(context.getResources().getString(R.string.lng), lng);
            data.put(context.getResources().getString(R.string.date), String.valueOf(new Date().getTime()));

            mFirebaseDatabase.child(Variables.user_key).setValue(data);

            //  mFirebaseDatabase.child("lng").setValue(data);



    }


}
