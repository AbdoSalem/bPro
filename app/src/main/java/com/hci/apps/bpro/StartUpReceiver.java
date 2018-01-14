package com.hci.apps.bpro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hci.apps.bpro.activities.MainActivity;
import com.hci.apps.bpro.services.FloatingService;

public class StartUpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, FloatingService.class));
    }
}
