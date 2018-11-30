package com.vr.latlng;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicMarkableReference;

/**
 * Created by Admin on 11/29/2018.
 */

public class SetImageFromUrl {
   private FirebaseDatabase mFirebaseInstance = Variables.mFirebaseInstance;
    protected void setImage(Context context, final ProgressBar progressBar, final ArrayList<list_item> arrayList, final int position, final ImageView image) {

            mFirebaseInstance.getReference(context.getResources().getString(R.string.users)).child(arrayList.get(position).friend_key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    try{
                    new UploadDownloadImages(progressBar).download(arrayList.get(position).friend_key + ".png", "recycleview", image, arrayList, position);
                }catch (Exception e){

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
