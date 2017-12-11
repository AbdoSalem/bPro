package com.hci.apps.bpro;

import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String SERVICE_STARTED_KEY = "SERVICE_STARTED_KEY";
    TextView tvTitle;
    RecyclerView recyclerView;
    RecyclerAdapter adapter;
    FloatingActionButton serviceButton;
    private PendingIntent pendingIntent;
    private SharedPreferences sharedPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvTitle = (TextView) findViewById(R.id.tv_title);
        recyclerView = (RecyclerView) findViewById(R.id.list);
        serviceButton = (FloatingActionButton) findViewById(R.id.btn_service);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        sharedPref =  getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        if(checkForPermission(this)) {
            Map stats = LoggerManager.getInstance().QueryForDay(this);
            adapter = new RecyclerAdapter(this);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));


        }
        else{
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));

        }

        serviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onServiceButtonClicked();
            }
        });




        boolean alarmUp = sharedPref.getBoolean(SERVICE_STARTED_KEY,false);
        if(!alarmUp) {
            serviceButton.setImageDrawable(getDrawable(R.drawable.ic_play_arrow_white_24dp));
        }else{
            serviceButton.setImageDrawable(getDrawable(R.drawable.ic_stop_white_24dp));
        }
    }

    private void onServiceButtonClicked() {
        boolean alarmUp = sharedPref.getBoolean(SERVICE_STARTED_KEY,false);
        if(!alarmUp) {
            Intent intent = new Intent(getApplicationContext(), MyAlarmService.class);
            pendingIntent = PendingIntent.getService(getApplicationContext(), 125, intent, 0);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    15*60*1000, 15*60*1000, pendingIntent);
            Toast.makeText(this, "Start Tracking", Toast.LENGTH_LONG).show();
            serviceButton.setImageDrawable(getDrawable(R.drawable.ic_stop_white_24dp));
            writeServiceState(true);

        }else {
            Toast.makeText(this, "Stop Tracking", Toast.LENGTH_LONG).show();
            pendingIntent = (PendingIntent.getBroadcast(getApplicationContext(), 125,
                    new Intent(this,MyAlarmService.class),
                    0) );
            Log.d(TAG,"The intent to cancel is "+ pendingIntent);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
            serviceButton.setImageDrawable(getDrawable(R.drawable.ic_play_arrow_white_24dp));
            pendingIntent =null;
            writeServiceState(false);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    private boolean checkForPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }
    private void writeServiceState(boolean isActive){
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(SERVICE_STARTED_KEY, isActive);
        editor.commit();
    }

}
