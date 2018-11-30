package com.vr.latlng;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.Date;

public class UserProfile extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView profile_image;
    private Button save;
    private EditText name,email,mob_no,date;

    private DatabaseReference mFirebaseDatabase;

    private String mCurrentPhotoPath;
    private FirebaseDatabase mFirebaseInstance;
    private int permission;
    private android.app.AlertDialog alertDialog;

    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Variables.context=this;
        setContentView(R.layout.activity_user_profile);
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        progressBar=findViewById(R.id.progressBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        name=findViewById(R.id.name);
        email=findViewById(R.id.email);
        mob_no=findViewById(R.id.mob_no);
         date=findViewById(R.id.date);
        profile_image=(ImageView)findViewById(R.id.profile_image);
        save=findViewById(R.id.save);
        final ProgressDialog progressDialog;

        if (getIntent().getStringExtra(getResources().getString(R.string.tag))!=null) {
            getSupportActionBar().setTitle(getIntent().getStringExtra(getResources().getString(R.string.tag)));
        }
        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
        mFirebaseInstance = FirebaseDatabase.getInstance();

        mFirebaseDatabase = mFirebaseInstance.getReference(getResources().getString(R.string.users));

        mFirebaseDatabase.child(Variables.user_key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               String s_name,s_mob,s_email,s_date;
                if (dataSnapshot.exists()) {
                    s_name = dataSnapshot.child(getResources().getString(R.string.name)).getValue(String.class);
                    s_mob = dataSnapshot.child(getResources().getString(R.string.phone_no)).getValue(String.class);
                    s_email = dataSnapshot.child(getResources().getString(R.string.user_email)).getValue(String.class);
                    s_date = dataSnapshot.child(getResources().getString(R.string.date)).getValue(String.class);
                    if (s_name != null) {
                        name.setText(s_name);
                    }
                    if (s_mob != null) {
                        mob_no.setText(s_mob);

                    }
                    if (s_email != null) {
                        email.setText(s_email);

                    }
                    if (s_date != null) {
                        date.setText(DateFormat.format(getString(R.string.date_format), new Date(Long.parseLong(s_date)).getTime()));

                    }
                    new UploadDownloadImages(progressBar).download(Variables.user_key+getString(R.string.png),getString(R.string.profile),profile_image,null,0);

                }
                      progressDialog.dismiss();

                               }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("Error list data retrive", "loadPost:onCancelled", databaseError.toException());

            }
        });
        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               showFileChooser();
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFirebaseDatabase.child(Variables.user_key).child(getResources().getString(R.string.name)).setValue(name.getText().toString());
                mFirebaseDatabase.child(Variables.user_key).child(getResources().getString(R.string.phone_no)).setValue(mob_no.getText().toString());
                Toast.makeText(getApplicationContext(),"Updated",Toast.LENGTH_SHORT).show();
            }
        });

        new StartService(this);
    }

    protected void showFileChooser() {
      /*  Intent intent = new Intent();
        intent.setType("image*//*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);*/
      /*  Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, 555);*/
        final CharSequence[] items = { "View enlarged image","Take Photo", "Choose from galary",
                "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (items[item].equals("View enlarged image")) {

                    dialog.dismiss();
                    profile_image.invalidate();
                    BitmapDrawable drawable = (BitmapDrawable) profile_image.getDrawable();
                    Bitmap bitmap = drawable.getBitmap();
                    new CommonMethods().zoomImage(bitmap);
                }else
                    if (items[item].equals("Take Photo")) {
                    if (ActivityCompat.checkSelfPermission(UserProfile.this,  android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED  ) {

                        verifyCameraPermissions();
                                  }else {
                        startActivityForResult(new UploadDownloadImages(UserProfile.this).fromcamera(), Variables.TAKE_PICTURE);//zero can be replaced with any action code

                    }
                      dialog.dismiss();


                } else if (items[item].equals("Choose from galary")) {
                    if (ActivityCompat.checkSelfPermission(UserProfile.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED  || ActivityCompat.checkSelfPermission(UserProfile.this,  Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED  ) {


                        verifyStoragePermissions();
                    }else {
                        startActivityForResult(new UploadDownloadImages(UserProfile.this).fromgalary(), Variables.FROM_GALARY);//zero can be replaced with any action code
                    }
                    dialog.dismiss();

                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

            for (int i = 0, len = permissions.length; i < len; i++) {
                String permission = permissions[i];
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    // user rejected the permission
                    boolean showRationale = false;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        showRationale = shouldShowRequestPermissionRationale( permission );
                    }
                    if (! showRationale) {
                        // user also CHECKED "never ask again"
                        // you can either enable some fall back,
                        // disable features of your app
                        // or open another dialog explaining
                        // again the permission and directing to
                        // the app setting
                        //  Toast.makeText(getApplicationContext(),"Allow app to acess location from settings",Toast.LENGTH_LONG).show();
                    } else if (android.Manifest.permission.CAMERA.equals(permission)) {
                        showRationale( R.string.permission_denied_camera);
                        break;
                        // user did NOT check "never ask again"
                        // this is a good place to explain the user
                        // why you need the permission and ask if he wants
                        // to accept it (the rationale)
                    }
                    else if (android.Manifest.permission.READ_EXTERNAL_STORAGE.equals(permission) || android.Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permission)) {
                        showRationale(R.string.permission_denied_storage);
                        break;
                        // user did NOT check "never ask again"
                        // this is a good place to explain the user
                        // why you need the permission and ask if he wants
                        // to accept it (the rationale)
                    }

                  /*else if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permission)) {
                        showRationale(permission, R.string.permission_denied_write);
                        // user did NOT check "never ask again"
                        // this is a good place to explain the user
                        // why you need the permission and ask if he wants
                        // to accept it (the rationale)
                    }*/
                }else {
                    if (requestCode==Variables.CAMERA_PERMISSION){
                        startActivityForResult(new UploadDownloadImages(UserProfile.this).fromcamera(), Variables.TAKE_PICTURE);//zero can be replaced with any action code

                    }else    if (requestCode==Variables.STORAGE_PERMISSION) {
                        startActivityForResult(new UploadDownloadImages(UserProfile.this).fromgalary(), Variables.FROM_GALARY);//zero can be replaced with any action code

                    }
                    }
            }
        }


    public void verifyStoragePermissions() {
          String mPermission = android.Manifest.permission.READ_EXTERNAL_STORAGE;

            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(this, new String[]{mPermission, android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Variables.STORAGE_PERMISSION);



    }
    public void verifyCameraPermissions() {
        String mPermission = android.Manifest.permission.CAMERA;

        // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(this, new String[]{mPermission},
                    Variables.CAMERA_PERMISSION);



    }

    private void showRationale(final int permission_denied) {
        android.app.AlertDialog.Builder builder=new android.app.AlertDialog.Builder(this);

        builder.setMessage(getResources().getString(permission_denied)).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.dismiss();
               if (permission_denied==R.string.permission_denied_camera){
                   verifyCameraPermissions();
               }else    if (permission_denied==R.string.permission_denied_storage){
                    verifyStoragePermissions();

               }
            }
        }).setCancelable(true);

        alertDialog=builder.create();
        alertDialog.show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data!=null) {
            if (requestCode == Variables.TAKE_PICTURE) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                profile_image.setImageBitmap(photo);

                new UploadDownloadImages(UserProfile.this).uploadData(photo);
            } else if (requestCode == Variables.FROM_GALARY) {

                // Let's read picked image data - its URI
                Bitmap bitmap = null;
                final Uri uri = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                profile_image.setImageBitmap(bitmap);
                new UploadDownloadImages(this).uploadData(bitmap);

            }
        }
    }

    private void back(){
        startActivity(new Intent(this,MapsActivity.class));
        finish();
    }
    @Override
    public boolean onSupportNavigateUp() {
      back();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        back();
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
}
