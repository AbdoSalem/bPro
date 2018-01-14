package com.hci.apps.bpro.activities;

import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hci.apps.bpro.services.FloatingService;
import com.hci.apps.bpro.Helper;
import com.hci.apps.bpro.LoggerManager;
import com.hci.apps.bpro.services.MyAlarmService;
import com.hci.apps.bpro.R;
import com.hci.apps.bpro.RecyclerAdapter;

import java.util.Date;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String SERVICE_STARTED_KEY = "SERVICE_STARTED_KEY";
    public static final String CHEAT_POINTS_KEY = "CHEAT_POINTS_KEY";
    public static final String FIRST_INSTALL_DATE_KEY = "FIRST_INSTALL_DATE_KEY";
    public static final String SHOW_ONLY_PASS_THRESHOLD = "SHOW_ONLY_PASS_THRESHOLD";

    public static final String CHEAT_POINTS_DATE_KEY = "CHEAT_POINTS_DATE_KEY";
    TextView tvTitle;
    TextView tvText;
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

        tvTitle = (TextView) findViewById(R.id.tv_points);
        tvText = (TextView) findViewById(R.id.tv_text2);
        recyclerView = (RecyclerView) findViewById(R.id.list);
        serviceButton = (FloatingActionButton) findViewById(R.id.btn_service);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        tvText.setPaintFlags(tvText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                startActivity(new Intent(MainActivity.this,MapsActivity.class));
                return false;
            }
        });

        sharedPref =  getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String date = sharedPref.getString(FIRST_INSTALL_DATE_KEY,null);

        if(date == null){
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(FIRST_INSTALL_DATE_KEY, Helper.sdf.format(new Date()));
            editor.commit();
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        if(checkForPermission(this)) {
            adapter = new RecyclerAdapter(this,getIntent().getBooleanExtra(SHOW_ONLY_PASS_THRESHOLD,false));
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));


        }
        else{
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));

        }
        onServiceButtonClicked();
        serviceButton.setVisibility(View.GONE);
//        serviceButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });

        Helper.askForSystemOverlayPermission(this);


        boolean alarmUp = sharedPref.getBoolean(SERVICE_STARTED_KEY,false);
        if(!alarmUp) {
            serviceButton.setImageDrawable(getDrawable(R.drawable.ic_play_arrow_white_24dp));
        }else{
            serviceButton.setImageDrawable(getDrawable(R.drawable.ic_stop_white_24dp));
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    private void onServiceButtonClicked() {
//        boolean alarmUp = sharedPref.getBoolean(SERVICE_STARTED_KEY,false);
//        if(!alarmUp) {
            Intent intent = new Intent(getApplicationContext(), MyAlarmService.class);
            pendingIntent = PendingIntent.getService(getApplicationContext(), 125, intent, 0);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    15 * 60 * 1000, 15 * 60 * 1000, pendingIntent);
//            Toast.makeText(this, "Start Tracking", Toast.LENGTH_LONG).show();
            serviceButton.setImageDrawable(getDrawable(R.drawable.ic_stop_white_24dp));
            writeServiceState(true);
            // To prevent starting the service if the required permission is NOT granted.
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    //Permission is not available. Display error text.
                    Toast.makeText(this, "Draw over other app permission not available. Can't start the application without the permission.", Toast.LENGTH_LONG).show();
                    Helper.askForSystemOverlayPermission(this);
                    finish();
                }
            } else {
                //super.onActivityResult(Helper.R, resultCode, data);
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
                    startForegroundService(new Intent(this, FloatingService.class));
                } else {
                    startService(new Intent(this, FloatingService.class));
                }

            }


//        }else {
//            Toast.makeText(this, "Stop Tracking", Toast.LENGTH_LONG).show();
//            pendingIntent = (PendingIntent.getBroadcast(getApplicationContext(), 125,
//                    new Intent(this,MyAlarmService.class),
//                    0) );
//            Log.d(TAG,"The intent to cancel is "+ pendingIntent);
//            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//            alarmManager.cancel(pendingIntent);
//            serviceButton.setImageDrawable(getDrawable(R.drawable.ic_play_arrow_white_24dp));
//            pendingIntent =null;
//            writeServiceState(false);
//            stopService(new Intent(MainActivity.this, FloatingService.class));
//        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        updatePoints();
    }

    private void updatePoints() {
        int points = sharedPref.getInt(CHEAT_POINTS_KEY,0);
        tvTitle.setText(String.valueOf(points));
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

        if (id == R.id.nav_run) {
           startActivity(new Intent(this,MapsActivity.class));
        }else if (id== R.id.nav_report){
            startActivity(new Intent(this,ReportActivity.class));
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
