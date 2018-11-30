package com.vr.latlng;

import android.graphics.Bitmap;

/**
 * Created by Admin on 7/15/2018.
 */

public class list_item {
    Bitmap profilePic;

     String user_location,friend_key,friend_name,date;

    public list_item(Bitmap profilePic, String user_location, String friend_key, String friend_name, String date) {
        this.profilePic = profilePic;
        this.user_location = user_location;
        this.friend_key = friend_key;
        this.friend_name = friend_name;
        this.date = date;
    }


}
