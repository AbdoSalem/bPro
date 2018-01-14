package com.hci.apps.bpro;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import com.hci.apps.bpro.activities.MainActivity;
import com.hci.apps.bpro.services.FloatingService;
import com.hci.apps.bpro.services.MyAlarmService;

public class StartUpReceiver extends BroadcastReceiver {
    private static final String SERVICE_STARTED_KEY = "SERVICE_STARTED_KEY";

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPref =  context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            context.startForegroundService(new Intent(context, FloatingService.class));
        } else {
            context.startService(new Intent(context, FloatingService.class));
        }
        Intent intent1 = new Intent(context, MyAlarmService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 125, intent1, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                15*60*1000, 15*60*1000, pendingIntent);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(SERVICE_STARTED_KEY, true);
        editor.commit();
    }
}
