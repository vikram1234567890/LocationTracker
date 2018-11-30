package com.vr.latlng;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Admin on 7/26/2018.
 */

public class UploadDownloadImages   {

    Context context;
Activity activity;
    int permission ;
    private String mCurrentPhotoPath;
    private FirebaseStorage storage;
    private ProgressDialog progressDialog;
    private ProgressBar progressBar;
    public UploadDownloadImages(Activity activity) {
        this.activity = activity;
        context=Variables.context;
        permission = ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        storage=FirebaseStorage.getInstance();
        progressDialog=new ProgressDialog(activity);
        progressDialog.setCancelable(false);


    }

    public UploadDownloadImages() {
        storage=FirebaseStorage.getInstance();

        this.context = Variables.context;
    }

    public UploadDownloadImages(ProgressBar progressBar) {
        storage=FirebaseStorage.getInstance();

        this.progressBar = progressBar;
        this.context = Variables.context;
    }

    Intent fromcamera(){
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create the File where the photo should go
        File photoFile = null;
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "PNG_" + timeStamp + "_.png";
        photoFile =  new File(Environment.getExternalStorageDirectory(),imageFileName);
        mCurrentPhotoPath=photoFile.getPath();
        if(!photoFile.exists())
            try {


                    photoFile.createNewFile();

            } catch (IOException e) {
                e.printStackTrace();
            }
        // Continue only if the File was successfully created
        if (photoFile != null) {

            /*    if(Build.VERSION.SDK_INT>=24) {
                  //  takePicture.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File("content:" + mCurrentPhotoPath)));
                }else {
                    takePicture.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));

                }*/


            return takePicture;

        }


        return takePicture;
    }





    Intent fromgalary(){
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        return pickPhoto;
    }

    void uploadData(Bitmap bitmap){
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
        // Create a storage reference from our app
        StorageReference storageRef = storage.getReference();

// Create a reference to "mountains.jpg"
        final StorageReference storageReference = storageRef.child(Variables.user_key+context.getResources().getString(R.string.png));

// Create a reference to 'images/mountains.jpg'
        StorageReference mountainImagesRef = storageRef.child("images/"+Variables.user_key+".png");

// While the file names are the same, the references point to different files
        storageReference.getName().equals(mountainImagesRef.getName());    // true

// Get the data from an ImageView as bytes

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        final UploadTask uploadTask = storageReference.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                progressDialog.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
         /*
                mFirebaseDatabase.child(Variables.user_key).child(context.getResources().getString(R.string.profile_image)).setValue(taskSnapshot.getDownloadUrl().toString());
*/
                progressDialog.dismiss();

            }

        });

    }

void download(final String image_name, final String tag, final ImageView imageView, final ArrayList<list_item> arrayList, final int position){
    // Create a storage reference from our app
    StorageReference storageRef = storage.getReference();
    StorageReference storageReference = storageRef.child(image_name);
try {

    final long ONE_MEGABYTE = 1024 * 1024;

    storageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
        @Override
        public void onSuccess(byte[] bytes) {
            // Data for "images/island.jpg" is returns, use this as needed
            if (progressBar!=null){
                progressBar.setVisibility(View.GONE);

            }
            try {


            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (tag.equals("user")){
                Variables.my_photo=bitmap;
                bitmap = Bitmap.createScaledBitmap(bitmap, 150, 150, false);

                DBConnection.marker.setIcon(BitmapDescriptorFactory.fromBitmap(new CommonMethods().getCustomMapMarker(bitmap)));
                DBConnection.myBitmap=bitmap;
            }else  if (tag.equals(context.getResources().getString(R.string.friend))) {
                Variables.friend_photo=bitmap;
                if ( DBConnection.friend_marker != null && DBConnection.popupProfile != null) {
                    bitmap = Bitmap.createScaledBitmap(bitmap, 150, 150, false);

                    DBConnection.friend_marker.setIcon(BitmapDescriptorFactory.fromBitmap(new CommonMethods().getCustomMapMarker(bitmap)));
                    DBConnection.popupProfile.setImageBitmap(bitmap);

                }
            }else  if (tag.equals(context.getResources().getString(R.string.profile))) {
              // imageView.setImageBitmap( new CommonMethods().getCroppedBitmap(bitmap));
                   imageView.setImageBitmap( bitmap);


            }else  if (tag.equals("recycleview")) {

                if(arrayList.get(position).profilePic==null) {
                    arrayList.get(position).profilePic = bitmap;
                    imageView.setImageBitmap(bitmap);
                }

             }
            }catch (Exception e){


            }
                     }
    }).addOnFailureListener(
            new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception exception) {
            // Handle any errors
            try {
                if(arrayList.get(position).profilePic==null) {

                    Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.profile_icon);
                    arrayList.get(position).profilePic = bitmap;
                    imageView.setImageBitmap(bitmap);
                }
                progressBar.setVisibility(View.GONE);
            }catch (Exception e){

            }

        }
    });

}catch (Exception e){

}
}
}
