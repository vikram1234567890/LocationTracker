package com.vr.locationtracker;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by Admin on 7/25/2018.
 */

public class CommonMethods {
    private Context context;
    private Query query;

    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private android.support.v7.app.AlertDialog alertDialog;
    private android.support.v7.app.AlertDialog.Builder builder;

    private InterstitialAd mInterstitialAd;
    private FirebaseUser currentUser;
    private Activity activity;

    public CommonMethods() {

        context=Variables.context;
        mFirebaseInstance = FirebaseDatabase.getInstance();

    }
    void loadAd(){
        MobileAds.initialize(context, context.getResources().getString(R.string.admob_app_id));
        mInterstitialAd = new InterstitialAd(context);
        mInterstitialAd.setAdUnitId(Variables.interstitial_ad_unit_id);
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                mInterstitialAd.show();

            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when when the interstitial ad is closed.
            }
        });
    /*    //load ad
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.");
        }*/
    }
    protected double CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);

        return Radius * c;
    }
    protected Bitmap getCustomMapMarker(Bitmap bitmap){
            Bitmap result = null;
            try {
                result = Bitmap.createBitmap(dp(62), dp(76), Bitmap.Config.ARGB_8888);
                result.eraseColor(Color.TRANSPARENT);
                Canvas canvas = new Canvas(result);
                Drawable drawable = context.getResources().getDrawable(R.drawable.live_marker);
                drawable.setBounds(0, 0, dp(62), dp(76));
                drawable.draw(canvas);

                Paint roundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                RectF bitmapRect = new RectF();
                canvas.save();

                 //Bitmap bitmap = BitmapFactory.decodeFile(path.toString()); /*generate bitmap here if your image comes from any url*/
                if (bitmap != null) {
                    BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                    Matrix matrix = new Matrix();
                    float scale = dp(52) / (float) bitmap.getWidth();
                    matrix.postTranslate(dp(5), dp(5));
                    matrix.postScale(scale, scale);
                    roundPaint.setShader(shader);
                    shader.setLocalMatrix(matrix);
                    bitmapRect.set(dp(5), dp(5), dp(52 + 5), dp(52 + 5));
                    canvas.drawRoundRect(bitmapRect, dp(26), dp(26), roundPaint);
                }
                canvas.restore();
                try {
                    canvas.setBitmap(null);
                } catch (Exception e) {}
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return result;
    }
    private int dp(float value) {
        if (value == 0) {
            return 0;
        }
        return (int) Math.ceil(context.getResources().getDisplayMetrics().density * value);
    }
    protected void zoomImage(Bitmap bitmap){
      /*  builder=new android.support.v7.app.AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView= inflater.inflate(R.layout.zoomable_image_view, null);
        builder.setView(dialogView);
*/
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView= inflater.inflate(R.layout.zoomable_image_view, null);
        Dialog d = new Dialog(context, android.R.style.Theme_Holo_NoActionBar_TranslucentDecor);
        d.setCancelable(true);

        d.setContentView(dialogView);

        ZoomableImageView touch = dialogView.findViewById(R.id.zoom_image);
        touch.setImageBitmap(bitmap);

        d.show();
       /* alertDialog = builder.setCancelable(true).create();
        alertDialog.show();*/



    }
    protected boolean isMyServiceRunning(Class<?> serviceClass) {
        try {

        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }

        }catch (Exception e){

        }
        return false;
    }
    protected void shareMessage(){
        final CharSequence[] items = { "Share", "Never :(",
                "Later" };
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Share and track more friends");
      //  builder.setMessage("Track more users!!!Share this app now :)");

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (items[item].equals("Share")) {
                    share();
                    dialog.dismiss();
                    changeSharedPref(context.getResources().getString(R.string.share),"1",context.getResources().getString(R.string.app_name));

                }else
                if (items[item].equals("Never :(")) {
                    changeSharedPref(context.getResources().getString(R.string.share),"1",context.getResources().getString(R.string.app_name));

                    dialog.dismiss();


                } else if (items[item].equals("Later")) {
                    Variables.NO_SHARE=true;
                    dialog.dismiss();

                }
            }
        });
        builder.show();



    }
    protected void rateMessage(){
        final CharSequence[] items = { "Rate now", "Never :(",
                "Later" };
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Love this app!!!");
       // builder.setMessage("Take a second to rate this app on play store");

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (items[item].equals("Rate now")) {
                    rate();
                    dialog.dismiss();
                    changeSharedPref(context.getResources().getString(R.string.rate),"1",context.getResources().getString(R.string.app_name));

                }else
                if (items[item].equals("Never :(")) {
                    changeSharedPref(context.getResources().getString(R.string.rate),"1",context.getResources().getString(R.string.app_name));

                    dialog.dismiss();


                } else if (items[item].equals("Later")) {
                    Variables.NO_RATE=true;
                    dialog.dismiss();

                }
            }
        });
        builder.show();




    }
    protected void rate(){
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id="+context.getPackageName())));


    }


    protected void share(){
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Hey,This app let's you track anyone from anywhere.Give it a try now.\nhttps://play.google.com/store/apps/details?id=" + context.getPackageName());
        sendIntent.setType("text/plain");
        context.startActivity(sendIntent);
    }
    protected void changeSharedPref(String key, String value, String shared_pref_name)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences(shared_pref_name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, "");
        editor.putString(key, value);
        editor.commit();
    }
    protected String getSharedPref(String key, String shared_pref_name)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences(shared_pref_name, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key,"");
    }
    protected void clearSharedPref(String shared_pref_name){
        SharedPreferences.Editor editor;
        // INSTANTIATE THE SHAREDPREFERENCE INSTANCE
        SharedPreferences sharedPreferences = context.getSharedPreferences(shared_pref_name, Context.MODE_PRIVATE);

// INSTANTIATE THE EDITOR INSTANCE
        editor = sharedPreferences.edit();
        editor.clear();
    }
    protected void stopTracking(String friend_Key,String user_key){
        mFirebaseDatabase = mFirebaseInstance.getReference(context.getResources().getString(R.string.you_tracking_user));
        query = mFirebaseDatabase.child(user_key).orderByChild(context.getResources().getString(R.string.track_to)).equalTo(friend_Key);
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
        mFirebaseDatabase = mFirebaseInstance.getReference(context.getResources().getString(R.string.user_tracking_me));
        query = mFirebaseDatabase.child(friend_Key).orderByChild(context.getResources().getString(R.string.tracked_by)).equalTo(user_key);
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
    protected void showHelp(){

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Help");
        builder.setMessage(context.getResources().getString(R.string.tutorial));
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.dismiss();
            }
        }).setNegativeButton("Don't show again", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                changeSharedPref(context.getResources().getString(R.string.help),"1",context.getResources().getString(R.string.app_name));

            }
        });

        alertDialog=builder.setCancelable(false).create();
        alertDialog.show();




    }
    protected void updateMessage(){

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Update");
        builder.setMessage("New version is available.Update now");
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                rate();
            }
        });

        alertDialog=builder.setCancelable(false).create();
        alertDialog.show();




    }
protected String currentVersion(){

    PackageInfo pInfo = null;
    try {
        pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
    } catch (PackageManager.NameNotFoundException e) {
        int verCode = pInfo.versionCode;
        e.printStackTrace();
    }

    String version = pInfo.versionName;
    return version;
}
    protected String currentVersionCode(){

        PackageInfo pInfo = null;
        int verCode;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
                 e.printStackTrace();
        }
        verCode = pInfo.versionCode;

        return String.valueOf(verCode);
    }



    protected void turnGPSOn(){
        String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if(!checkGpsEnabled()){ //if gps is disabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
           context.sendBroadcast(poke);
        }
    }
    protected boolean checkGpsEnabled(){
        String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
    return provider.contains("gps");
    }
    protected void turnGPSOff(){
try {


    if (checkGpsEnabled()) { //if gps is enabled
        final Intent poke = new Intent();
        poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
        poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
        poke.setData(Uri.parse("3"));
        context.sendBroadcast(poke);
    }
}catch (Exception e){

}
    }

protected String getAddress(double latitude, double longitude){
    Geocoder geoCoder = new Geocoder(context, Locale.getDefault());

    String address = "";
    try {
        List<Address> addresses = geoCoder.getFromLocation(latitude, longitude, 1);

        if (addresses.size() > 0)
        {

            address += addresses.get(0).getLocality();
            address += ","+addresses.get(0).getAdminArea();

            address += ","+addresses.get(0).getCountryName();

        }

    }
    catch (IOException e1) {
        e1.printStackTrace();
    }
return address;
}
}

