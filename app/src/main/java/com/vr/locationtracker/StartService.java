package com.vr.locationtracker;

import android.content.Context;
import android.content.Intent;

/**
 * Created by Admin on 7/25/2018.
 */

public class StartService {
    Context context;

    public StartService(Context context) {
        this.context = context;
        if (!new CommonMethods().isMyServiceRunning(BackgroundService.class)) {

            context.startService(new Intent(context, BackgroundService.class));
        }
    }
}
