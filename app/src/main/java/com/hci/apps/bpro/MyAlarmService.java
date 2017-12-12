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
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MyAlarmService extends Service {

    private static final String TAG = MyAlarmService.class.getSimpleName();

    @Override
    public void onCreate() {
        Log.d(TAG,"Started the service now");
        List<ListItemModel>data = LoggerManager.getInstance().queryThresholdAsList(this);
        if( FloatingService.service!= null) {
            FloatingService.service.setState(data);
        }
        if(data.size()>0) {
            String appName="unknown";

            String[] apps = new String[data.size()];
            String[] times = new String[data.size()];
            for (int i =0;i<data.size();i++){
                times[i]= Helper.getTimeSpent(data.get(i).getStats().getTotalTimeInForeground());
                try {
                    apps[i] = Helper.getApplicationName(data.get(i).getPackageName().toString(),this);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
            showNotification(apps,times);
        }
        SharedPreferences sharedPref=getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String datastr = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE).getString(MainActivity.CHEAT_POINTS_DATE_KEY,Helper.sdf.format(new Date()));
        try {
            Date date1 = Helper.sdf.parse(datastr);
            Date now = new Date();
            if (date1.getDay()!= now.getDay() && date1.before(now))
            {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt(MainActivity.CHEAT_POINTS_KEY, 0);
                editor.putString(MainActivity.CHEAT_POINTS_DATE_KEY,Helper.sdf.format(new Date()));
                editor.commit();
            }
        } catch (ParseException e) {
            e.printStackTrace();
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
    private void showNotification(String[] applications,String[] times){
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        String title = "Applications are consuming too much time";
        String smallText = "You have excedded the threshold for "+ applications.length +" applications";
        if (Build.VERSION.SDK_INT >= 26) {


            NotificationChannel channel = new NotificationChannel("152",
                    title,
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(smallText);
            notificationManager.createNotificationChannel(channel);
        }

        Log.d(TAG, "showing notification for " + applications + " with time " + times);

        // prepare intent which is triggered if the
// notification is selected

        Intent intent = new Intent(this, MainActivity.class);
// use System.currentTimeMillis() to have a unique ID for the pending intent
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);
        StringBuilder builder = new StringBuilder();

        for (int i =0;i<applications.length;i++){
            builder.append(applications[i]);
            builder.append(" consumes " );
            builder.append(times[i]);
            builder.append("\n");
        }
        builder.setLength(builder.length()-1);

        Notification n = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            n = new Notification.Builder(this,"152")
                    .setContentTitle(title)
                    .setContentText(smallText)
                    .setSmallIcon(R.drawable.ic_launcher_round)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                            R.drawable.ic_launcher_round))
                    .setContentIntent(pIntent)
                    .setStyle(new Notification.BigTextStyle().bigText(builder.toString()))
                    .setAutoCancel(true)
                    .build();
        }else{
            n = new Notification.Builder(this)
                    .setContentTitle(title)
                    .setContentText(smallText)
                    .setSmallIcon(R.drawable.ic_launcher_round)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                            R.drawable.ic_launcher_round))
                    .setContentIntent(pIntent)
                    .setStyle(new Notification.BigTextStyle().bigText(builder.toString()))
                    .setAutoCancel(true)
                    .build();
        }
        Log.d(TAG, "Finished building notification");
        notificationManager.notify(152, n);
        Log.d(TAG,"Finished showing notification");


    }

}