package com.vr.latlng;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MapsActivity extends AppCompatActivity  implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    private Marker now;
    private double i;
Toolbar toolbar;
    private AlertDialog.Builder alertDialogbuilder;
    private AlertDialog alertDialog;
    private boolean backPressed=false;
    @SuppressLint("StaticFieldLeak")
    protected static LinearLayout maps_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Variables.context=this;
        setContentView(R.layout.activity_maps);
        new CommonMethods().loadAd();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);

        toggle.syncState();


        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);
        maps_layout=findViewById(R.id.maps_layout);
        if (  new CommonMethods().getSharedPref(getResources().getString(R.string.help),getResources().getString(R.string.app_name)).equals("")) {
            new CommonMethods().showHelp();
        }
    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        GoogleMap mMap = googleMap;

        Variables.mMap= mMap;
        if (Variables.user_key!=null) {
            new LocationService().mapReady();
        }
        if (Variables.start_tracking && Variables.myLatLng!=null){


           new LocationService().friendTrack();
        }else {
            FirebaseDatabase mFirebaseInstance = FirebaseDatabase.getInstance();

            DatabaseReference tempFirebaseDatabase = mFirebaseInstance.getReference(getResources().getString(R.string.you_tracking_user)).child(Variables.user_key);
            Query query = tempFirebaseDatabase.orderByChild(getResources().getString(R.string.track_to));

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot data : dataSnapshot.getChildren()) {

                            Variables.friend_key = data.child(getResources().getString(R.string.track_to)).getValue(String.class);
                            new LocationService().friendTrack();
                        }
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }

           @Override
           public boolean onCreateOptionsMenu(Menu menu) {
               getMenuInflater().inflate(R.menu.main_menu, menu);
               return true;
           }
           @Override
           public boolean onOptionsItemSelected(MenuItem item) {
               if (item.getItemId() == R.id.menu_sign_out) {
                   GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                           .requestIdToken(getString(R.string.default_web_client_id))
                           .requestEmail()
                           .build();
// Build a GoogleSignInClient with the options specified by gso.
                   GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

                   mGoogleSignInClient.signOut()
                           .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                               @Override
                               public void onComplete(@NonNull Task<Void> task) {
                                   // ...
                               }
                           });
                   LoginManager.getInstance().logOut();
                   FirebaseAuth.getInstance().signOut();
                   new StopService(this);

                   stopService(new Intent(this, LocationService.class));


                   stopService(new Intent(this, ListenerService.class));

                   startActivity(new Intent(MapsActivity.this, LoginActivity.class));
                      finish();
                         }


               return true;
           }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent=new Intent(this,RecycleViewLayout.class);

        if (id == R.id.notifications) {
              intent.putExtra(getResources().getString(R.string.tag),getResources().getString(R.string.notifications));
            startActivity(intent);
            finish();

        }else       if (id == R.id.user_profile) {
            intent=new Intent(this,UserProfile.class);

            intent.putExtra(getResources().getString(R.string.tag),getResources().getString(R.string.user_profile));
            startActivity(intent);
            finish();
        }
        else       if (id == R.id.find_friends) {
            intent.putExtra(getResources().getString(R.string.tag),getResources().getString(R.string.find_friends));
            startActivity(intent);
            finish();
        }
        else       if (id == R.id.track_friends) {
            intent.putExtra(getResources().getString(R.string.tag),getResources().getString(R.string.track_friends));
            startActivity(intent);
            finish();

        }else       if (id == R.id.tracking_you) {
            intent.putExtra(getResources().getString(R.string.tag),getResources().getString(R.string.tracking_you));
            startActivity(intent);
            finish();

        }else       if (id == R.id.my_friends) {
            intent.putExtra(getResources().getString(R.string.tag),getResources().getString(R.string.my_fiends));
            startActivity(intent);
            finish();

        }
        else       if (id == R.id.rate) {
            new CommonMethods().rate();
                  }
        else       if (id == R.id.share) {

            new CommonMethods().share();

        }else       if (id == R.id.help) {

            new CommonMethods().showHelp();

        }else       if (id == R.id.about) {
            alertDialogbuilder=new AlertDialog.Builder(this);


            alertDialogbuilder.setTitle("About");
            alertDialogbuilder.setMessage(getApplicationName(this)+" v"+new CommonMethods().currentVersion()+"\n" +
                    "\n" +
                    "Developer\n" +
                    "\"VR apps\"");
            alertDialog = alertDialogbuilder.create();
            alertDialog.show();

        }




        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }
private void back(){
        finish();
}
    @Override
    public void onBackPressed() {
         if (backPressed){
            back();
        }else {
             backPressed=true;
            Toast.makeText(getApplicationContext(),"Press back again to exit",Toast.LENGTH_SHORT).show();
        }
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        // your code here
                        backPressed=false;
                    }
                },
                3000
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == Variables.REQUEST_PERMISSION) {
            // for each permission check if the user granted/denied them
            // you may want to group the rationale in a single dialog,
            // this is just an example
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
                    } else if (android.Manifest.permission.ACCESS_FINE_LOCATION.equals(permission)) {
                        showRationale( R.string.permission_denied_location);
                        // user did NOT check "never ask again"
                        // this is a good place to explain the user
                        // why you need the permission and ask if he wants
                        // to accept it (the rationale)
                    } /*else if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permission)) {
                        showRationale(permission, R.string.permission_denied_write);
                        // user did NOT check "never ask again"
                        // this is a good place to explain the user
                        // why you need the permission and ask if he wants
                        // to accept it (the rationale)
                    }*/
                }
            }
        }
    }

    private void showRationale( int permission_denied_contacts) {
        alertDialogbuilder=new AlertDialog.Builder(this);

        alertDialogbuilder.setMessage(getResources().getString(permission_denied_contacts)).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.dismiss();
                requestPermission();
            }
        }).setCancelable(false);

        alertDialog=alertDialogbuilder.create();
        alertDialog.show();
    }
    private void requestPermission(){
        ActivityCompat.requestPermissions(this, new String[]{  android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION/*, Manifest.permission.WRITE_EXTERNAL_STORAGE*/}, Variables.REQUEST_PERMISSION);

    }

    @Override
    protected void onStart() {
        super.onStart();

        new StartService(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        new StartService(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        new StopService(this);
    }
}
