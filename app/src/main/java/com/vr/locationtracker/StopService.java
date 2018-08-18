package com.vr.locationtracker;

import android.content.Context;
import android.content.Intent;

/**
 * Created by Admin on 7/19/2018.
 */

public class StopService {
Context context;

    public StopService(Context context) {
        this.context = context;
        if (new CommonMethods().isMyServiceRunning(BackgroundService.class)) {
            context.stopService(new Intent(context, BackgroundService.class));
        }

    }
}
