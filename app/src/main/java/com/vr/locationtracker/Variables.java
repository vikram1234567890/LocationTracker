package com.vr.locationtracker;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by Admin on 7/14/2018.
 */

public class Variables {

    static protected GoogleMap mMap;

    static protected String  user_key;

    static protected String  friend_key;

    static protected String  friend_name;

    static protected LatLng myLatLng;

    static protected LatLng friendLatLng;
    static protected Bitmap  my_photo;
    static protected Bitmap  friend_photo;
    static protected boolean  start_tracking;
    static protected Context context;
    static protected String currentLatitude,currentLongitude;

    static protected int TAKE_PICTURE=1;
    static protected int FROM_GALARY=2;
    static protected int STORAGE_PERMISSION=3;
    static protected int CAMERA_PERMISSION=4;
    static protected boolean NO_SHARE;
    static protected boolean NO_RATE;
    static protected String  interstitial_ad_unit_id="ca-app-pub-1211635675454735/3014593037";
    static protected int REQUEST_PERMISSION=101;
    public Variables() {
         mMap=null;

          user_key=null;

        friend_key=null;

           friend_name=null;

           my_photo=null;
           friend_photo=null;
          start_tracking=false;
        NO_SHARE=false;
        NO_RATE=false;
        myLatLng=null;
        friendLatLng=null;
      new CommonMethods().clearSharedPref(context.getResources().getString(R.string.app_name));
        new CommonMethods().clearSharedPref(context.getResources().getString(R.string.system_generated));

        context=null;
    }
}
