package com.vr.latlng;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Admin on 11/29/2018.
 */

public class SetRecyclerAdapter {
    private  ArrayList<list_item> searchArrayList=new ArrayList<>();


    protected void setAdapter(ArrayList<list_item> arrayList, TextView not_found, Context context, RecyclerView recyclerView, HashMap<Integer, Boolean> friend_extra_keys){
      Variables.arrayList=arrayList;
      Variables.friend_extra_keys=friend_extra_keys;
        set(arrayList,not_found,context,recyclerView,friend_extra_keys);



    }
    protected void searchSetAdapter(ArrayList<list_item> arrayList, TextView not_found, Context context, RecyclerView recyclerView, HashMap<Integer, Boolean> friend_extra_keys){
     set(arrayList,not_found,context,recyclerView,friend_extra_keys);


    }
    private void set(ArrayList<list_item> arrayList, TextView not_found, Context context, RecyclerView recyclerView, HashMap<Integer, Boolean> friend_extra_keys){
        CustomAdapter customAdapter = new CustomAdapter(context, arrayList,friend_extra_keys);
        recyclerView.setAdapter(customAdapter);
        if (arrayList.size()>0){
            not_found.setVisibility(View.GONE);
        }else {
            not_found.setVisibility(View.VISIBLE);

        }
    }

}
