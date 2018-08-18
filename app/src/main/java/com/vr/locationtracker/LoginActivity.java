package com.vr.locationtracker;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInApi;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;

import static com.vr.locationtracker.Variables.context;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    AlertDialog alertDialog ;

    private int GOOGLE_SIGN_IN=1,FB_SIGN_IN=2;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager callbackManager;
    private LoginButton fb_login_button;
    private static final String EMAIL = "email";
    private TextView privacy_accept;
    private ProgressDialog progressDialog;
    private Activity activity;
    private int REQUEST_CHECK_SETTINGS=105;
    private FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this;
        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Signing in...");
        progressDialog.setCancelable(false);
        new Variables();
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        context=getApplicationContext();
        setContentView(R.layout.activity_login);
        privacy_accept=findViewById(R.id.privacy_accept);
        privacy_accept.setMovementMethod(LinkMovementMethod.getInstance());

        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(this);
        fb_login_button=findViewById(R.id.fb_login_button);
        fb_login_button.setOnClickListener(this);

        if (ActivityCompat.checkSelfPermission(this,  android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED  ) {
            requestPermission();

        }

        mAuth = FirebaseAuth.getInstance();

    }

    void fbSignin(){


        callbackManager = CallbackManager.Factory.create();
        fb_login_button.setReadPermissions(EMAIL ,"public_profile");

        fb_login_button.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("", "facebook:onSuccess:" + loginResult);

               handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d("", "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("", "facebook:onError", error);
                // ...
            }
        });
    }
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("", "handleFacebookAccessToken:" + token);
        progressDialog.show();
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("", "signInWithCredential:success");
                             user = mAuth.getCurrentUser();
                            updateUI(user,LoginActivity.this);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("", "signInWithCredential:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                             updateUI(null,LoginActivity.this);
                        }

                        // ...
                    }
                });
    }
    void googleSignIn(){
        // Configure Google Sign In

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
// Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        signIn();
    }
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("", "firebaseAuthWithGoogle:" + acct.getId());
progressDialog.show();
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            LoginActivity.this.user = user;
                            updateUI(user,LoginActivity.this);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("", "signInWithCredential:failure", task.getException());
                            Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();

                            updateUI(null,LoginActivity.this);
                        }

                        // ...
                    }
                });
    }
    @Override
    public void onClick(View view) {
        if (view.getId()==R.id.sign_in_button){

                 googleSignIn();


        }else   if (view.getId()==R.id.fb_login_button){

                fbSignin();


        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

            // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
            if (requestCode == GOOGLE_SIGN_IN) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    firebaseAuthWithGoogle(account);
                } catch (ApiException e) {
                    // Google Sign In failed, update UI appropriately
                    Log.w("", "Google sign in failed", e);
                    Toast.makeText(getApplicationContext(),"Google sign in failed",Toast.LENGTH_LONG).show();

                    // ...
                }
            }else if (requestCode==REQUEST_CHECK_SETTINGS){

                updateUI(user,this);
        }
            else {
                // Pass the activity result back to the Facebook SDK
                try {
                    callbackManager.onActivityResult(requestCode, resultCode, data);
                }catch (Exception e){
                  //  Toast.makeText(getApplicationContext(),"Turn on GPS and try again!!",Toast.LENGTH_LONG).show();

                }
            }
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
        AlertDialog.Builder builder=new AlertDialog.Builder(this);

        builder.setMessage(getResources().getString(permission_denied_contacts)).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.dismiss();
                requestPermission();
            }
        }).setCancelable(false);

        alertDialog=builder.create();
        alertDialog.show();
    }
    private void requestPermission(){
        ActivityCompat.requestPermissions(this, new String[]{  android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION/*, Manifest.permission.WRITE_EXTERNAL_STORAGE*/}, Variables.REQUEST_PERMISSION);

    }


    protected void updateUI(FirebaseUser user,Activity activity) {
        this.activity=activity;
        if (user!=null){
              if (!new CommonMethods().checkGpsEnabled()){
                displayLocationSettingsRequest();

            }else if (new CommonMethods().checkGpsEnabled()) {
                // Load contents

                  AccessToken accessToken = AccessToken.getCurrentAccessToken();
                  boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
                  if (mAuth!=null) {
                      Intent intent=new Intent(context, LocationService.class);
                      intent.putExtra(getResources().getString(R.string.tag),"initial");
                      context.startService(intent);


                  }else
                  if (isLoggedIn) {
                      LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
                      //fbSignin();
                  }

            }

        }
    }


    protected void displayLocationSettingsRequest() {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:

                        updateUI(user,activity);
                        Log.i("LocationSettingsStatus", "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.CANCELED:

                        updateUI(user,activity);
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i("LocationSettingsStatus", "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {

                            Log.i("LocationSettingsStatus", "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i("LocationSettingsStatus", "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        context=this;
        user = mAuth.getCurrentUser();

        updateUI(user,this);
        // Check if user is signed in (non-null) and update UI accordingly.

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }


}
