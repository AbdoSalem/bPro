package com.hci.apps.bpro;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MyAlarmService extends Service {

    private static final String TAG = MyAlarmService.class.getSimpleName();

    @Override
    public void onCreate() {
        Log.d(TAG,"Started the service now");
        List<ListItemModel>data = LoggerManager.getInstance().queryDayAsList(this);
        if(data.get(0).getStats().getTotalTimeInForeground()>1000*60*60) {
            String appName="unknown";
            try {
                appName = Helper.getApplicationName(data.get(0).getPackageName().toString(),this);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            String time = Helper.getTimeSpent(data.get(0).getStats().getTotalTimeInForeground());
            showNotification(appName,time );
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"My alarm service destroyed");
        super.onDestroy();

    }

    @Override
    public boolean onUnbind(Intent intent) {
        Toast.makeText(this, "MyAlarmService.onUnbind()", Toast.LENGTH_LONG).show();
        return super.onUnbind(intent);
    }
    private void showNotification(String application,String time){
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        String title = "Applications are consuming too much time";
        String description = application + " is consuming " + time + " of your time";
        if (Build.VERSION.SDK_INT >= 26) {


            NotificationChannel channel = new NotificationChannel("152",
                    title,
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }

        Log.d(TAG, "showing notification for " + application + " with time " + time);

        // prepare intent which is triggered if the
// notification is selected

        Intent intent = new Intent(this, MainActivity.class);
// use System.currentTimeMillis() to have a unique ID for the pending intent
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

        Notification n = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            n = new Notification.Builder(this,"152")
                    .setContentTitle(title)
                    .setContentText(description)
                    .setSmallIcon(R.drawable.ic_launcher_round)
                    .setContentIntent(pIntent)
                    .setStyle(new Notification.BigTextStyle().bigText(application + " is consuming " + time + " of your time"))
                    .setAutoCancel(true)
                    .build();
        }else{
            n = new Notification.Builder(this)
                    .setContentTitle(title)
                    .setContentText(description)
                    .setSmallIcon(R.drawable.ic_launcher_round)
                    .setContentIntent(pIntent)
                    .setStyle(new Notification.BigTextStyle().bigText(application + " is consuming " + time + " of your time"))
                    .setAutoCancel(true)
                    .build();
        }
        Log.d(TAG, "Finished building notification");
        notificationManager.notify(152, n);
        Log.d(TAG,"Finished showing notification");


    }

}