package com.vr.locationtracker;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
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

public class RecycleViewLayout extends AppCompatActivity  {

    private Toolbar toolbar;
    private EditText search;
    private ArrayList<list_item> arrayList=new ArrayList<>();

    private ArrayList<list_item> searchArrayList=new ArrayList<>();
    private CustomAdapter customAdapter;
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private RecyclerView recyclerView;
    private ArrayList<String > keys=new ArrayList<>(),friendExtraKey=new ArrayList<>();
     private SwipeRefreshLayout swipeRefreshLayout;
    private Query query;
    private DatabaseReference tempFirebaseDatabase;
    private Map<Integer,Boolean> friend_extra_keys=new HashMap<>();
    private TextView not_found,status;
    private ArrayList<LatLng> address=new ArrayList<>();
     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Variables.context=this;
        if (Variables.user_key==null){
            Variables.user_key=new CommonMethods().getSharedPref("key",getResources().getString(R.string.system_generated));

        }
        setContentView(R.layout.activity_recycle_view_layout);
        mFirebaseInstance = FirebaseDatabase.getInstance();
       toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        not_found= findViewById(R.id.not_found);
        status= findViewById(R.id.status);

        if (getIntent().getStringExtra(getResources().getString(R.string.tag))!=null) {
            getSupportActionBar().setTitle(getIntent().getStringExtra(getResources().getString(R.string.tag)));
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
                if (search.getText().toString().trim().length()!=0) {
                    for (int j = 0; j < arrayList.size(); j++) {
                        if (arrayList.get(j).friend_name.toLowerCase().contains(search.getText().toString().toLowerCase())) {
                            searchArrayList.add(arrayList.get(j));
                        }
                    }
                    setAdapter(searchArrayList);

                }else {
                    setAdapter(arrayList);

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
            getUsers();
        }else if (getIntent().getStringExtra(getResources().getString(R.string.tag)).equals(getResources().getString(R.string.track_friends))){
            getOnlineFriendsForTracking();
        }else if (getIntent().getStringExtra(getResources().getString(R.string.tag)).equals(getResources().getString(R.string.notifications))){
            getNotification();
        }else if (getIntent().getStringExtra(getResources().getString(R.string.tag)).equals(getResources().getString(R.string.my_fiends))){
            getFriends();
        }else if (getIntent().getStringExtra(getResources().getString(R.string.tag)).equals(getResources().getString(R.string.tracking_you))){
            trackingYou();
        }

    }
    private void setAdapter(ArrayList<list_item> arrayList){

        customAdapter=new CustomAdapter(RecycleViewLayout.this, arrayList);
        recyclerView.setAdapter(customAdapter);
        if (arrayList.size()>0){
         not_found.setVisibility(View.GONE);
        }else {
            not_found.setVisibility(View.VISIBLE);

        }

    }

    private void getFriends(){
        arrayListClear();
        mFirebaseDatabase = mFirebaseInstance.getReference(getResources().getString(R.string.friends));

        mFirebaseDatabase.child(Variables.user_key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot users: dataSnapshot.getChildren()) {
                         keys.add(users.child(getResources().getString(R.string.friend)).getValue(String.class));

                }
                if (keys.size()==0){
                    setAdapter(arrayList);

                }
                for (int i=0;i<keys.size();i++) {
                    addUsersInArrayList(getResources().getString(R.string.users),i);

                }


                mFirebaseDatabase.removeEventListener(this);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("Error list data retrive", "loadPost:onCancelled", databaseError.toException());

            }
        });

    }
    private void getOnlineFriendsForTracking(){
        arrayListClear();
        status.setVisibility(View.VISIBLE);
        mFirebaseDatabase = mFirebaseInstance.getReference(getResources().getString(R.string.friends));

        mFirebaseDatabase.child(Variables.user_key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot users: dataSnapshot.getChildren()) {
                    keys.add(users.child(getResources().getString(R.string.friend)).getValue(String.class));

                }
                tempFirebaseDatabase = mFirebaseInstance.getReference(getResources().getString(R.string.online));

                    tempFirebaseDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            ArrayList<String> temp=new ArrayList<>();

                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                //keep only online users

                                for (int j=0;j<keys.size();j++) {
                                    for (int i = 0; i < keys.size(); i++) {
                                        if (keys.get(i).equals(data.getKey())) {
                                                 temp.add(keys.get(i));
                                                 keys.remove(i);
                                        }
                                    }
                                }

                            }


                            keys.clear();
                            keys.addAll(temp);

                            mFirebaseDatabase = mFirebaseInstance.getReference(getResources().getString(R.string.you_tracking_user));

                            mFirebaseDatabase.child(Variables.user_key).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    for (DataSnapshot data : dataSnapshot.getChildren()) {


                                           friendExtraKey.add(data.child(getResources().getString(R.string.track_to)).getValue(String .class));

                                    }
                                    if (keys.size()==0){
                                        setAdapter(arrayList);

                                    }
                                    for (int i=0;i<keys.size();i++) {
                                            friend_extra_keys.clear();
                                       addUsersInArrayList(getResources().getString(R.string.users),i);
                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                    // Getting Post failed, log a message
                                    Log.w("Error list data retrive", "loadPost:onCancelled", databaseError.toException());

                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e("", "onCancelled", databaseError.toException());
                        }
                    });



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("Error list data retrive", "loadPost:onCancelled", databaseError.toException());

            }
        });

    }

    private void arrayListClear(){
        keys.clear();
        arrayList.clear();
        friend_extra_keys.clear();
        friendExtraKey.clear();
        }
    private void getFriendRequests() {
        mFirebaseDatabase = mFirebaseInstance.getReference(getResources().getString(R.string.friends_requests_received));

        mFirebaseDatabase.child(Variables.user_key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               /*   StackTraceElement stackTraceElement[]= Thread.currentThread().getStackTrace();
                for (StackTraceElement s:stackTraceElement) {
                    Log.i("stack trace", s.toString());
                }*/

                for (DataSnapshot users: dataSnapshot.getChildren()) {
                    keys.add(users.child(getResources().getString(R.string.request_from)).getValue(String.class));

                }
                if (keys.size()==0){
                    setAdapter(arrayList);

                }
                for (int i=0;i<keys.size();i++) {
                    addUsersInArrayList(getResources().getString(R.string.users),i);

                }


                mFirebaseDatabase.removeEventListener(this);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                // Getting Post failed, log a message
                Log.w("Error list data retrive", "loadPost:onCancelled", databaseError.toException());

            }
        });
    }

        private void getNotification(){

            arrayListClear();
     getFriendRequests();

    }
    private void trackingYou(){

        arrayListClear();
        mFirebaseDatabase = mFirebaseInstance.getReference(getResources().getString(R.string.user_tracking_me));

        mFirebaseDatabase.child(Variables.user_key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot users: dataSnapshot.getChildren()) {
                    keys.add(users.child(getResources().getString(R.string.tracked_by)).getValue(String.class));

                }
                if (keys.size()==0){
                    setAdapter(arrayList);

                }
                              for (int i=0;i<keys.size();i++) {
                    addUsersInArrayList(getResources().getString(R.string.users),i);
                }

                mFirebaseDatabase.removeEventListener(this);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                // Getting Post failed, log a message
                Log.w("Error list data retrive", "loadPost:onCancelled", databaseError.toException());

            }
        });
    }
    private void addUsersInArrayList(String tag,final int id){
        mFirebaseDatabase = mFirebaseInstance.getReference(tag).child(keys.get(id));

        mFirebaseDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                     arrayList.add(new list_item("", keys.get(id)
                            , dataSnapshot.child(getResources().getString(R.string.name)).getValue(String.class) ,  dataSnapshot.child(getResources().getString(R.string.date)).getValue(String.class)));
              if (getIntent().getStringExtra(getResources().getString(R.string.tag)).equals(getResources().getString(R.string.tracking_you))) {

                    for (int i = 0; i < friendExtraKey.size(); i++) {
                        if (keys.get(id).equals(friendExtraKey.get(i))) {
                            friend_extra_keys.put(arrayList.size() - 1, true);
                            break;
                        }
                    }
                }
                if (id==keys.size()-1  || !dataSnapshot.exists()){
                    setAdapter(arrayList);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("Error list data retrive", "loadPost:onCancelled", databaseError.toException());

            }
        });
    }
private void getUsers(){
    arrayListClear();
    //get keys of users registered in friends
    tempFirebaseDatabase = mFirebaseInstance.getReference(getResources().getString(R.string.friends)).child(Variables.user_key);
    tempFirebaseDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(final DataSnapshot dataSnapshot) {

            for (DataSnapshot users: dataSnapshot.getChildren()) {
                keys.add(users.child(getResources().getString(R.string.friend)).getValue(String.class));

            }
            //get friend request recieved list and dont show in our list
            tempFirebaseDatabase = mFirebaseInstance.getReference(getResources().getString(R.string.friends_requests_received)).child(Variables.user_key);
            tempFirebaseDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for (DataSnapshot users: dataSnapshot.getChildren()) {
                        keys.add(users.child(getResources().getString(R.string.request_from)).getValue(String.class));

                    }
                //get friend reuest received list
            tempFirebaseDatabase = mFirebaseInstance.getReference(getResources().getString(R.string.friends_requests_sent)).child(Variables.user_key);
            tempFirebaseDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {

                            for (DataSnapshot users : dataSnapshot.getChildren()) {
                                friendExtraKey.add(users.child(getResources().getString(R.string.request_to)).getValue(String .class));

                            }

                            //get all users list
                            mFirebaseDatabase = mFirebaseInstance.getReference(getResources().getString(R.string.users));

                            mFirebaseDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(final DataSnapshot dataSnapshot) {

                                    for (DataSnapshot users : dataSnapshot.getChildren()) {
                                        if (!users.getKey().equals(Variables.user_key)) {
                                            boolean keyfound = false;
                                            for (int i = 0; i < keys.size(); i++) {
                                                if (users.getKey().equals(keys.get(i))) {
                                                    keyfound = true;
                                                    break;
                                                }
                                            }
                                            if (!keyfound) {//friend not added
                                                for (int i = 0; i < friendExtraKey.size(); i++) {
                                                    if (users.getKey().equals(friendExtraKey.get(i))) {
                                                        keyfound = true;
                                                        break;
                                                    }
                                                }
                                                if (keyfound) {//friend request has been sent
                                                    arrayList.add(new list_item( "", users.getKey()
                                                            , users.child(getResources().getString(R.string.name)).getValue(String.class),  users.child(getResources().getString(R.string.date)).getValue(String.class)));
                                                    friend_extra_keys.put(arrayList.size() - 1, true);

                                                }else {//friend request not  sent

                                                    arrayList.add(new list_item( "", users.getKey()
                                                            , users.child(getResources().getString(R.string.name)).getValue(String.class),  users.child(getResources().getString(R.string.date)).getValue(String.class)));
                                                }

                                            }
                                        }
                                    }

                                    setAdapter(arrayList);


                                }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });



                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Getting Post failed, log a message
                            Log.w("Error list data retrive", "loadPost:onCancelled", databaseError.toException());

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

        }
    });



}


    private void removeFriend(String friend_Key){
        mFirebaseDatabase = mFirebaseInstance.getReference(getResources().getString(R.string.friends));

         query = mFirebaseDatabase.child(Variables.user_key).orderByChild(getResources().getString(R.string.friend)).equalTo(friend_Key);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data: dataSnapshot.getChildren()) {
                    data.getRef().removeValue();
                }
                query.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("", "onCancelled", databaseError.toException());
            }
        });
       query = mFirebaseDatabase.child(friend_Key).orderByChild(getResources().getString(R.string.friend)).equalTo(Variables.user_key);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data: dataSnapshot.getChildren()) {
                    data.getRef().removeValue();
                }
                query.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("", "onCancelled", databaseError.toException());
            }
        });
    }

    private void acceptFriendRequest(final String friend_Key){
        mFirebaseDatabase = mFirebaseInstance.getReference(getResources().getString(R.string.friends));

        final Query query = mFirebaseDatabase.child(Variables.user_key).orderByChild(getResources().getString(R.string.friend)).equalTo(friend_Key);
         query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                       Map<String, String> data = new HashMap<>();
                    data.put(getResources().getString(R.string.friend), friend_Key);
                    data.put(getResources().getString(R.string.date), String.valueOf(new Date().getTime()));

                    mFirebaseDatabase.child(Variables.user_key).push().setValue(data);
                    data.clear();
                    data.put(getResources().getString(R.string.friend), Variables.user_key);
                    data.put(getResources().getString(R.string.date), String.valueOf(new Date().getTime()));


                    mFirebaseDatabase.child(Variables.friend_key).push().setValue(data);

                    Variables.friend_key=null;
                    deleteFriendRequest(friend_Key);
                }


                query.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("", "onCancelled", databaseError.toException());
            }
        });
              }


    private void deleteFriendRequest(String friend_Key){
        mFirebaseDatabase = mFirebaseInstance.getReference(getResources().getString(R.string.friends_requests_received));
        query = mFirebaseDatabase.child(Variables.user_key).orderByChild(getResources().getString(R.string.request_from)).equalTo(friend_Key);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data: dataSnapshot.getChildren()) {
                    data.getRef().removeValue();

                }

                query.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("", "onCancelled", databaseError.toException());
            }
        });
        mFirebaseDatabase = mFirebaseInstance.getReference(getResources().getString(R.string.friends_requests_sent));

        query = mFirebaseDatabase.child(friend_Key).orderByChild(getResources().getString(R.string.request_to)).equalTo(Variables.user_key);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data: dataSnapshot.getChildren()) {
                    data.getRef().removeValue();

                }

                query.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("", "onCancelled", databaseError.toException());
            }
        });
    }
    private void addFriend(final String friend_Key){
        mFirebaseDatabase = mFirebaseInstance.getReference(getResources().getString(R.string.friends_requests_received));


        Map<String, String> data = new HashMap<>();
        data.put(getResources().getString(R.string.request_from), Variables.user_key);
        data.put(getResources().getString(R.string.date), String.valueOf(new Date().getTime()));

        mFirebaseDatabase.child(friend_Key).push().setValue(data);
        mFirebaseDatabase = mFirebaseInstance.getReference(getResources().getString(R.string.friends_requests_sent));
        data.clear();
         data = new HashMap<>();
        data.put(getResources().getString(R.string.request_to), friend_Key);
        data.put(getResources().getString(R.string.date), String.valueOf(new Date().getTime()));

        mFirebaseDatabase.child(Variables.user_key).push().setValue(data);


    }
    private void getUsersAddress(String key, final TextView textView){
        mFirebaseDatabase = mFirebaseInstance.getReference(getResources().getString(R.string.users_locations)).child(key);

        mFirebaseDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot data) {
                String address;
                if (!data.exists()){
                     address="Unknown";
                 }else {
                    address = new CommonMethods().getAddress(Double.parseDouble(data.child(getResources().getString(R.string.lat)).getValue(String.class)), Double.parseDouble(data.child(getResources().getString(R.string.lng)).getValue(String.class)));
                }
                textView.setText(address);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("Error list data retrive", "loadPost:onCancelled", databaseError.toException());

            }
        });
    }
    public class CustomAdapter
            extends RecyclerView.Adapter<CustomAdapter.ViewHolder>  {

        private final List<list_item> mValues;

        private Context context;



        public CustomAdapter(Context context, List<list_item> objects) {
            swipeRefreshLayout.setRefreshing(false);

            mValues=objects;
            this.context=context;
        }



        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item, parent, false);

            return new ViewHolder(view);
        }



        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            //// TODO: access variables from another calss by handler
            holder.description.setText(mValues.get(position).description);
            holder.button.setText("Accept");
            holder.button1.setText("Decline");
            getUsersAddress(mValues.get(position).friend_key,holder.address);
              holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.image.invalidate();
                    BitmapDrawable drawable = (BitmapDrawable) holder.image.getDrawable();
                    Bitmap bitmap = drawable.getBitmap();
                    new CommonMethods().zoomImage(bitmap);
                }
            });
            mFirebaseInstance.getReference(context.getResources().getString(R.string.users)).child(mValues.get(position).friend_key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {

                    try {
                        new UploadDownloadImages(holder.progressBar).download(mValues.get(position).friend_key+".png","recycleview",holder.image);

                    }catch (Exception e){

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a message
                    Log.w("Error list data retrive", "loadPost:onCancelled", databaseError.toException());

                }
            });
             if (getIntent().getStringExtra(getResources().getString(R.string.tag)).equals(getResources().getString(R.string.notifications))){
                 holder.date.setText(getResources().getString(R.string.sent_on)+" "+DateFormat.format("dd-MM-yyyy (HH:mm:ss)",new Date(Long.parseLong(mValues.get(position).date))));

             }else
            if (getIntent().getStringExtra(getResources().getString(R.string.tag)).equals(getResources().getString(R.string.find_friends))){
                holder.button.setText(getResources().getString(R.string.add_friend));
                holder.button.setCompoundDrawablesWithIntrinsicBounds( R.drawable.add_friend, 0, 0, 0);

                holder.button1.setVisibility(View.GONE);
                holder.date.setText(getResources().getString(R.string.joined_on)+" "+DateFormat.format("dd-MM-yyyy (HH:mm:ss)",new Date(Long.parseLong(mValues.get(position).date))));

            }else
            if (getIntent().getStringExtra(getResources().getString(R.string.tag)).equals(getResources().getString(R.string.track_friends))){
                holder.button.setText(getResources().getString(R.string.start_tracking));
                holder.button1.setVisibility(View.GONE);
                holder.date.setText(getResources().getString(R.string.friends_since)+" "+DateFormat.format("dd-MM-yyyy (HH:mm:ss)",new Date(Long.parseLong(mValues.get(position).date))));
                holder.button.setCompoundDrawablesWithIntrinsicBounds( R.drawable.start, 0, 0, 0);


            }else
            if (getIntent().getStringExtra(getResources().getString(R.string.tag)).equals(getResources().getString(R.string.tracking_you))){
                holder.description.append(" is tracking you.");
                holder.button.setText(getResources().getString(R.string.disallow));
                holder.button.setCompoundDrawablesWithIntrinsicBounds( R.drawable.stop_tracking, 0, 0, 0);
                holder.button1.setVisibility(View.GONE);

                holder.date.setText(getResources().getString(R.string.tracking_since)+" "+DateFormat.format("dd-MM-yyyy (HH:mm:ss)",new Date(Long.parseLong(mValues.get(position).date))));

            }
            else
            if (getIntent().getStringExtra(getResources().getString(R.string.tag)).equals(getResources().getString(R.string.my_fiends))){
                holder.button.setText(getResources().getString(R.string.unfriend));
                holder.button.setCompoundDrawablesWithIntrinsicBounds( R.drawable.unfriend, 0, 0, 0);

                holder.button1.setVisibility(View.GONE);
                holder.date.setText(getResources().getString(R.string.friends_since)+" "+DateFormat.format("dd-MM-yyyy (HH:mm:ss)",new Date(Long.parseLong(mValues.get(position).date))));

            }
            if ( getIntent().getStringExtra(getResources().getString(R.string.tag)).equals(getResources().getString(R.string.find_friends))) {

                if (friend_extra_keys.get(position) != null && friend_extra_keys.get(position)) {
                    holder.button.setText(R.string.friend_request_sent);
                                    holder.button.setCompoundDrawablesWithIntrinsicBounds( 0, 0, 0, 0);


                }
            }else    if ( getIntent().getStringExtra(getResources().getString(R.string.tag)).equals(getResources().getString(R.string.track_friends))) {
                if (friend_extra_keys.get(position) != null && friend_extra_keys.get(position)) {
                    holder.button.setText(getResources().getString(R.string.stop_tracking));

                    holder.button.setCompoundDrawablesWithIntrinsicBounds( R.drawable.stop_tracking, 0, 0, 0);

                }
            }
                holder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new CommonMethods().loadAd();

                    if (getIntent().getStringExtra(getResources().getString(R.string.tag)).equals(getResources().getString(R.string.find_friends))) {
                        if ( holder.button.getText().toString().toLowerCase().equals(getResources().getString(R.string.friend_request_sent).toLowerCase())) {
                            holder.button.setText(getResources().getString(R.string.add_friend));
                            holder.button.setCompoundDrawablesWithIntrinsicBounds( R.drawable.add_friend, 0, 0, 0);
                            friend_extra_keys.remove(position);

                            notifyItemChanged(position);

                            String temp=Variables.user_key;//my key
                            Variables.user_key=mValues.get(position).friend_key;//friend key
                            mValues.get(position).friend_key=temp;//my key

                            deleteFriendRequest(mValues.get(position).friend_key);//delete my friend request
                            temp=mValues.get(position).friend_key;//my key
                            mValues.get(position).friend_key=Variables.user_key;//friend key

                            Variables.user_key=temp;//my key


                        }else {
                            addFriend(mValues.get(position).friend_key);
                            friend_extra_keys.put(position, true);
                            notifyItemChanged(position);
                        }

                    }else    if (getIntent().getStringExtra(getResources().getString(R.string.tag)).equals(getResources().getString(R.string.notifications))) {
                        Variables.friend_key=mValues.get(position).friend_key;
                        acceptFriendRequest(mValues.get(position).friend_key);
                        removeListItem(position);

                    }else     if (getIntent().getStringExtra(getResources().getString(R.string.tag)).equals(getResources().getString(R.string.track_friends))) {
                        Variables.friend_key = mValues.get(position).friend_key;
                     /*   for (int i=0;i<mValues.size();i++){

                            new CommonMethods().stopTracking(mValues.get(i).friend_key);
                            holder.button.setText(getResources().getString(R.string.start_tracking));

                            notifyItemChanged(position);

                        }*/

                        if (holder.button.getText().toString().toLowerCase().equals(getResources().getString(R.string.stop_tracking).toLowerCase())){
                            Variables.friend_key = mValues.get(position).friend_key;

                            Variables.start_tracking=false;
                            friend_extra_keys.remove(position);
                            new CommonMethods().stopTracking(mValues.get(position).friend_key,Variables.user_key);

                            holder.button.setText(getResources().getString(R.string.start_tracking));
                            holder.button.setCompoundDrawablesWithIntrinsicBounds( R.drawable.start, 0, 0, 0);

                            notifyItemChanged(position);
                        }else if (holder.button.getText().toString().toLowerCase().equals(getResources().getString(R.string.start_tracking).toLowerCase()))
                            {
                                Variables.friend_key = mValues.get(position).friend_key;

                                  holder.button.setText(getResources().getString(R.string.stop_tracking));
                                holder.button.setCompoundDrawablesWithIntrinsicBounds( R.drawable.stop_tracking, 0, 0, 0);

                                Variables.start_tracking = true;
                            notifyItemChanged(position);
                            Intent intent=new Intent(RecycleViewLayout.this,MapsActivity.class);

                            startActivity(intent);
                            finish();
                        }



                    }else     if (getIntent().getStringExtra(getResources().getString(R.string.tag)).equals(getResources().getString(R.string.my_fiends))) {
                          removeFriend(mValues.get(position).friend_key);
                        removeListItem(position);

                    }else     if (getIntent().getStringExtra(getResources().getString(R.string.tag)).equals(getResources().getString(R.string.tracking_you))) {
                        new CommonMethods().stopTracking(Variables.user_key,mValues.get(position).friend_key);
                        removeListItem(position);

                    }


                    }
            });
            holder.button1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteFriendRequest(mValues.get(position).friend_key);
                    removeListItem(position);

                }
            });
            holder.description.setText(mValues.get(position).friend_name);
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                            }
            });

        }
        private void removeListItem(int position){
            arrayList.remove(position);

            notifyItemRemoved(position);
            notifyItemRangeChanged(position, arrayList.size());
                  }
        @Override
        public int getItemCount() {
            return mValues.size();
        }

        protected class ViewHolder extends RecyclerView.ViewHolder {
            private final View mView;
            private TextView description,date,address;
            private ImageView image;
            private Button button,button1;
            private ProgressBar progressBar;

            private ViewHolder(View v) {
                super(v);
                mView = v;
                description= v.findViewById(R.id.description);
                date= v.findViewById(R.id.date);
                address= v.findViewById(R.id.address);

                image=v.findViewById(R.id.profile_image);
                button=v.findViewById(R.id.button);

                button1=v.findViewById(R.id.button1);
                progressBar=v.findViewById(R.id.progressBar);
            }


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
