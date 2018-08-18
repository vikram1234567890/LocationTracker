package com.vr.locationtracker;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Admin on 7/15/2018.
 */

public class list_item {
     String description,friend_key,friend_name,date;

    public list_item(String description, String friend_key, String friend_name,  String date) {

        this.description = description;
        this.friend_key = friend_key;
        this.friend_name = friend_name;
            this.date = date;
    }
}
