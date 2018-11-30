package com.vr.latlng;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

/**
 * Created by Admin on 7/19/2018.
 */

public class Notifications {
    Context context;
    String CHANNEL_ID = "my_channel_01";

    public Notifications(Context context) {
        this.context = context;
    }

    void shownotification(Class aClass,String intentData,String notificatoinTitle,String notificationMessage){
        int NOTIFICATION_ID = 234;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {


            CharSequence name = "my_channel";
            String Description = "This is my channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            //mChannel.enableLights(true);
           // mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(mChannel);
        }
        Intent resultIntent = new Intent(context,aClass);
        resultIntent.putExtra(context.getResources().getString(R.string.tag),intentData);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
          stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notificatoinTitle)
                .setAutoCancel(true)
                .setContentIntent(resultPendingIntent)

               // .addAction(R.drawable.ic_launcher,"Stop",resultPendingIntent)
                .setContentText(notificationMessage);
        builder.setVibrate(new long[]{100,200});

       /* Intent resultIntent = new Intent(context, LoginActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(LoginActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(resultPendingIntent);
*/
        notificationManager.notify(NOTIFICATION_ID, builder.build());

    }
}
