package com.vr.latlng;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class RecycleViewLayout extends AppCompatActivity  {

    private Toolbar toolbar;
    private EditText search;



    private RecyclerView recyclerView;
    protected static SwipeRefreshLayout swipeRefreshLayout;
    private TextView not_found;
    private TextView status;
    private ArrayList searchArrayList=new ArrayList();
    private HashMap<Integer,Boolean> friend_extra_keys=new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Variables.context=this;
        if (Variables.user_key==null){
            Variables.user_key=new CommonMethods().getSharedPref("key",getResources().getString(R.string.system_generated));

        }
        setContentView(R.layout.activity_recycle_view_layout);

       toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        not_found= findViewById(R.id.not_found);
        status= findViewById(R.id.status);
        if (getIntent().getStringExtra(getResources().getString(R.string.tag))!=null) {
            Variables.Tag=getIntent().getStringExtra(getResources().getString(R.string.tag));
            getSupportActionBar().setTitle(Variables.Tag);
        }
         recyclerView=findViewById(R.id.recycler_view);

        swipeRefreshLayout=findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                     load();
            }
        });
        search=findViewById(R.id.search);
         search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {



                searchArrayList.clear();
                friend_extra_keys.clear();
                int k=0;

                if (search.getText().toString().trim().length()!=0) {
                    for (int j = 0; j < Variables.arrayList.size(); j++) {
                        if (Variables.arrayList.get(j).friend_name.toLowerCase().contains(search.getText().toString().toLowerCase())) {
                            searchArrayList.add(Variables.arrayList.get(j));
                            friend_extra_keys.put(k,Variables.friend_extra_keys.get(j));
                            k++;
                        }
                    }
                    new SetRecyclerAdapter().searchSetAdapter(searchArrayList,not_found,RecycleViewLayout.this,recyclerView,friend_extra_keys);

                }else {
                    new SetRecyclerAdapter().searchSetAdapter(Variables.arrayList,not_found,RecycleViewLayout.this,recyclerView,Variables.friend_extra_keys);


                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

        });
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(mLayoutManager);

        load();
        if (!Variables.NO_SHARE &&  new CommonMethods().getSharedPref(getResources().getString(R.string.share),getResources().getString(R.string.app_name)).equals("")) {
            new CommonMethods().shareMessage();
        }
    }
    private void load(){
        swipeRefreshLayout.setRefreshing(true);

        if (getIntent().getStringExtra(getResources().getString(R.string.tag)).equals(getResources().getString(R.string.find_friends))) {
            new FindFriends().getAllUsers(this,not_found,recyclerView);
        }else if (getIntent().getStringExtra(getResources().getString(R.string.tag)).equals(getResources().getString(R.string.track_friends))){
            status.setVisibility(View.VISIBLE);
            new GetOnlineFriendsForTracking().getOnlineFriendsForTracking(this,not_found,recyclerView);
        }else if (getIntent().getStringExtra(getResources().getString(R.string.tag)).equals(getResources().getString(R.string.notifications))){
            new GetNotification().getNotification(this,not_found,recyclerView);
        }else if (getIntent().getStringExtra(getResources().getString(R.string.tag)).equals(getResources().getString(R.string.my_fiends))){
            new MyFriends().getFriends(this,not_found,recyclerView);

        }else if (getIntent().getStringExtra(getResources().getString(R.string.tag)).equals(getResources().getString(R.string.tracking_you))){

            new FriendsTrackingYou().trackingYou(this,not_found,recyclerView);
        }

    }









    @Override
    public boolean onSupportNavigateUp() {

        if (!new CommonMethods().isMyServiceRunning(LocationService.class) ) {

            startActivity(new Intent(this,LoginActivity.class));
        }else {
            back();
        }
        return super.onSupportNavigateUp();
          }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!new CommonMethods().isMyServiceRunning(LocationService.class)) {

           startActivity(new Intent(this,LoginActivity.class));
        }else {
            back();
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        new StartService(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        new StopService(this);
    }



    private void back(){
              startActivity(new Intent(this,MapsActivity.class));
                finish();
          }
}
