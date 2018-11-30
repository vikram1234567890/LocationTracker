package com.vr.latlng;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public  class CustomAdapter
        extends RecyclerView.Adapter<CustomAdapter.ViewHolder>  {

     private    ArrayList<list_item> arrayList;
    private  ArrayList<Thread> arrThreads = new ArrayList<>();
    private final SwipeRefreshLayout swipeRefreshLayout=RecycleViewLayout.swipeRefreshLayout;

    private Context context;
    private String Tag=Variables.Tag;
   private HashMap<Integer , Boolean> friend_extra_keys;
    public CustomAdapter(Context context, ArrayList<list_item> objects, HashMap<Integer ,Boolean> friend_extra_keys) {
        swipeRefreshLayout.setRefreshing(false);

        arrayList=objects;
        this.context=context;
        this. friend_extra_keys=friend_extra_keys;

    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);

        return new ViewHolder(view);
    }

   /* class BackgroundSlowLoadingProcess implements  Runnable{
        private int position;
        ViewHolder holder;

        public BackgroundSlowLoadingProcess(int position, ViewHolder holder) {
            this.position = position;
            this.holder = holder;
        }

        @Override
        public void run() {
            new SetImageFromUrl().setImage(context,holder.progressBar,arrayList,position,holder.image);

        }
    }*/

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        //// TODO: access variables from another calss by handler

        holder.button.setText("Accept");
        holder.button1.setText("Decline");


        if(arrayList.get(position).user_location==null)
         new GetUsersAddress().getUsersAddress(arrayList.get(position).friend_key,holder.address,context,arrayList,position);
       else
           holder.address.setText(arrayList.get(position).user_location);
        if(arrayList.get(position).profilePic!=null){

            holder.image.setImageBitmap( arrayList.get(position).profilePic);
            holder.progressBar.setVisibility(View.GONE);
        }else
        {
           /* Thread T1 = new Thread(new BackgroundSlowLoadingProcess(position,holder));
            T1.start();
            arrThreads.add(position,T1);
            try {
                arrThreads.get(position).join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
            new SetImageFromUrl().setImage(context,holder.progressBar,arrayList,position,holder.image);


        }

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.image.invalidate();
                BitmapDrawable drawable = (BitmapDrawable) holder.image.getDrawable();
                Bitmap bitmap = drawable.getBitmap();
                new CommonMethods().zoomImage(bitmap);
            }
        });

        if (Tag.equals(context.getResources().getString(R.string.notifications))){
            holder.date.setText(context.getResources().getString(R.string.sent_on)+" "+ DateFormat.format("dd-MM-yyyy (HH:mm:ss)",new Date(Long.parseLong(arrayList.get(position).date))));

        }else
        if (Tag.equals(context.getResources().getString(R.string.find_friends))){
            holder.button.setText(context.getResources().getString(R.string.add_friend));
            holder.button.setCompoundDrawablesWithIntrinsicBounds( R.drawable.add_friend, 0, 0, 0);

            holder.button1.setVisibility(View.GONE);
            holder.date.setText(context.getResources().getString(R.string.joined_on)+" "+DateFormat.format("dd-MM-yyyy (HH:mm:ss)",new Date(Long.parseLong(arrayList.get(position).date))));

        }else
        if (Tag.equals(context.getResources().getString(R.string.track_friends))){
            holder.button.setText(context.getResources().getString(R.string.start_tracking));
            holder.button1.setVisibility(View.GONE);
            holder.date.setText(context.getResources().getString(R.string.friends_since)+" "+DateFormat.format("dd-MM-yyyy (HH:mm:ss)",new Date(Long.parseLong(arrayList.get(position).date))));
            holder.button.setCompoundDrawablesWithIntrinsicBounds( R.drawable.start, 0, 0, 0);


        }else
        if (Tag.equals(context.getResources().getString(R.string.tracking_you))){
            holder.description.append(" is tracking you.");
            holder.button.setText(context.getResources().getString(R.string.disallow));
            holder.button.setCompoundDrawablesWithIntrinsicBounds( R.drawable.stop_tracking, 0, 0, 0);
            holder.button1.setVisibility(View.GONE);

            holder.date.setText(context.getResources().getString(R.string.tracking_since)+" "+DateFormat.format("dd-MM-yyyy (HH:mm:ss)",new Date(Long.parseLong(arrayList.get(position).date))));

        }
        else
        if (Tag.equals(context.getResources().getString(R.string.my_fiends))){
            holder.button.setText(context.getResources().getString(R.string.unfriend));
            holder.button.setCompoundDrawablesWithIntrinsicBounds( R.drawable.unfriend, 0, 0, 0);

            holder.button1.setVisibility(View.GONE);
            holder.date.setText(context.getResources().getString(R.string.friends_since)+" "+DateFormat.format("dd-MM-yyyy (HH:mm:ss)",new Date(Long.parseLong(arrayList.get(position).date))));

        }
        if ( Tag.equals(context.getResources().getString(R.string.find_friends))) {

            if (friend_extra_keys.get(position) != null && friend_extra_keys.get(position)) {
                holder.button.setText(R.string.friend_request_sent);
                holder.button.setCompoundDrawablesWithIntrinsicBounds( 0, 0, 0, 0);


            }
        }else    if ( Tag.equals(context.getResources().getString(R.string.track_friends))) {
            if (friend_extra_keys.get(position) != null && friend_extra_keys.get(position)) {
                holder.button.setText(context.getResources().getString(R.string.stop_tracking));

                holder.button.setCompoundDrawablesWithIntrinsicBounds( R.drawable.stop_tracking, 0, 0, 0);

            }
        }
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new CommonMethods().loadAd();

                if (Tag.equals(context.getResources().getString(R.string.find_friends))) {
                    if ( holder.button.getText().toString().toLowerCase().equals(context.getResources().getString(R.string.friend_request_sent).toLowerCase())) {
                        holder.button.setText(context.getResources().getString(R.string.add_friend));
                        holder.button.setCompoundDrawablesWithIntrinsicBounds( R.drawable.add_friend, 0, 0, 0);
                        friend_extra_keys.remove(position);
                       Variables. friend_extra_keys.remove(position);

                        notifyItemChanged(position);

                        String temp= Variables.user_key;//my key
                        Variables.user_key=arrayList.get(position).friend_key;//friend key
                        arrayList.get(position).friend_key=temp;//my key

                        new DeleteFriendRequest().deleteFriendRequest(arrayList.get(position).friend_key,context);//delete my friend request
                        temp=arrayList.get(position).friend_key;//my key
                        arrayList.get(position).friend_key=Variables.user_key;//friend key

                        Variables.user_key=temp;//my key


                    }else {
                        new AddFriend().addFriend(arrayList.get(position).friend_key,context);
                        friend_extra_keys.put(position, true);
                        Variables.friend_extra_keys.put(position, true);

                        notifyItemChanged(position);
                    }

                }else    if (Tag.equals(context.getResources().getString(R.string.notifications))) {
                    Variables.friend_key=arrayList.get(position).friend_key;
                    new AcceptFriendRequest().acceptFriendRequest(arrayList.get(position).friend_key,context);
                    removeListItem(position, arrayList);

                }else     if (Tag.equals(context.getResources().getString(R.string.track_friends))) {
                    Variables.friend_key = arrayList.get(position).friend_key;
                     /*   for (int i=0;i<arrayList.size();i++){

                            new CommonMethods().stopTracking(arrayList.get(i).friend_key);
                            holder.button.setText(context.getResources().getString(R.string.start_tracking));

                            notifyItemChanged(position);

                        }*/

                    if (holder.button.getText().toString().toLowerCase().equals(context.getResources().getString(R.string.stop_tracking).toLowerCase())){
                        Variables.friend_key = arrayList.get(position).friend_key;

                        Variables.start_tracking=false;
                        new CommonMethods().stopTracking(arrayList.get(position).friend_key,Variables.user_key);

                        holder.button.setText(context.getResources().getString(R.string.start_tracking));
                        holder.button.setCompoundDrawablesWithIntrinsicBounds( R.drawable.start, 0, 0, 0);

                        notifyItemChanged(position);
                    }else if (holder.button.getText().toString().toLowerCase().equals(context.getResources().getString(R.string.start_tracking).toLowerCase()))
                    {
                        Variables.friend_key = arrayList.get(position).friend_key;

                        holder.button.setText(context.getResources().getString(R.string.stop_tracking));
                        holder.button.setCompoundDrawablesWithIntrinsicBounds( R.drawable.stop_tracking, 0, 0, 0);

                        Variables.start_tracking = true;
                        notifyItemChanged(position);
                        Intent intent=new Intent(context,MapsActivity.class);

                        context.startActivity(intent);

                    }



                }else     if (Tag.equals(context.getResources().getString(R.string.my_fiends))) {
                    new RemoveFriend().removeFriend(arrayList.get(position).friend_key,context);
                    removeListItem(position, arrayList);

                }else     if (Tag.equals(context.getResources().getString(R.string.tracking_you))) {
                    new CommonMethods().stopTracking(Variables.user_key,arrayList.get(position).friend_key);
                    removeListItem(position, arrayList);

                }


            }
        });
        holder.button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DeleteFriendRequest().deleteFriendRequest(arrayList.get(position).friend_key,context);
                removeListItem(position, arrayList);

            }
        });
        holder.description.setText(arrayList.get(position).friend_name);
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

    }
    private void removeListItem(int position, List<list_item> arrayList){
        arrayList.remove(position);

        notifyItemRemoved(position);
        notifyItemRangeChanged(position, arrayList.size());
    }
    @Override
    public int getItemCount() {
        return arrayList.size();
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