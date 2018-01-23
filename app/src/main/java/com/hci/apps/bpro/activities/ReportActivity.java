package com.hci.apps.bpro.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hci.apps.bpro.Helper;
import com.hci.apps.bpro.LoggerManager;
import com.hci.apps.bpro.MapItemModel;
import com.hci.apps.bpro.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.hci.apps.bpro.activities.MainActivity.CHEAT_POINTS_KEY;
import static com.hci.apps.bpro.activities.MainActivity.VERSION_KEY;

public class ReportActivity extends AppCompatActivity {
    private static final String ID_KEY = "ID_KEY";
    private BarChart mChart;
    private FirebaseDatabase database;
    DatabaseReference myRef;
    private SharedPreferences sharedPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mChart = (BarChart) findViewById(R.id.chart1);
        //mChart.setOnChartValueSelectedListener(this);
        mChart.getDescription().setEnabled(false);

//        mChart.setDrawBorders(true);

        // scaling can now only be done on x- and y-axis separately
        mChart.setPinchZoom(false);

        mChart.setDrawBarShadow(false);

        mChart.setDrawGridBackground(false);

        sharedPref =  getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);



        Legend l = mChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(true);
//        l.setTypeface(mTfLight);
        l.setYOffset(0f);
        l.setXOffset(10f);
        l.setYEntrySpace(0f);
        l.setTextSize(8f);

        XAxis xAxis = mChart.getXAxis();
//        xAxis.setTypeface(mTfLight);
        xAxis.setGranularity(1f);
        xAxis.setCenterAxisLabels(true);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return value== 0?"Before Application":"After Application";
            }
        });

        YAxis leftAxis = mChart.getAxisLeft();
//        leftAxis.setTypeface(mTfLight);
        leftAxis.setValueFormatter(new LargeValueFormatter(){
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return Helper.getTimeSpent((long) value);
            }
        });
        leftAxis.setDrawGridLines(false);
        leftAxis.setSpaceTop(35f);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        mChart.getAxisRight().setEnabled(false);
        Map<String,MapItemModel>map = LoggerManager.getInstance().queryMonthBeforeAsList(this);
        BarData data = new BarData();

        int[] colors = new int[]{Color.rgb(104, 241, 175),Color.rgb(164, 228, 251),Color.rgb(242, 247, 158),Color.rgb(255, 102, 0),Color.rgb(0, 102, 255)};
        int i =0;

        for (Map.Entry<String,MapItemModel> item:map.entrySet()) {
            List<BarEntry> entry= new ArrayList<>();
            BarEntry oldEntry= new BarEntry(0,item.getValue().getOldStats().getTotalTimeInForeground()-item.getValue().getNewStats().getTotalTimeInForeground());
            BarEntry newEntry= new BarEntry(0,item.getValue().getNewStats().getTotalTimeInForeground());
            entry.add(oldEntry);
            entry.add(newEntry);
            try {
                BarDataSet set = new BarDataSet(entry, Helper.getApplicationName(item.getKey(),this));
                set.setColor(colors[i++]);
                set.setValueFormatter(new LargeValueFormatter(){
                    @Override
                    public String getFormattedValue(float value, AxisBase axis) {
                        return Helper.getTimeSpent((long) value);
                    }
                });
                data.addDataSet(set);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        data.setValueFormatter(new LargeValueFormatter(){
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return Helper.getTimeSpent((long) value);
            }
        });
        mChart.setData(data);
        float groupSpace = 0.1f;
        float barSpace = 0.03f; // x4 DataSet
        float barWidth = 0.15f; // x4 DataSet
        // specify the width each bar should have
        mChart.getBarData().setBarWidth(barWidth);
        try {
            // restrict the x-axis range
            mChart.getXAxis().setAxisMinimum(0);

            // barData.getGroupWith(...) is a helper that calculates the width each group needs based on the provided parameters
            mChart.getXAxis().setAxisMaximum(0 + mChart.getBarData().getGroupWidth(groupSpace, barSpace) * 2);
            mChart.groupBars(0, groupSpace, barSpace);
            mChart.invalidate();
        }catch(Exception ex){
            Toast.makeText(this,"No data to display yet",Toast.LENGTH_LONG).show();
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //store to database
        DatabaseReference mobileRoot = init();
        DatabaseReference pointsRef =  mobileRoot.child("points");
        int points = sharedPref.getInt(CHEAT_POINTS_KEY,0);
        pointsRef.setValue(String.valueOf(points));
        for (Map.Entry<String,MapItemModel> item:map.entrySet()) {
            try {
                String appName = Helper.getApplicationName(item.getKey(),this);
                DatabaseReference appRoot =  mobileRoot.child(String.valueOf(appName));
                DatabaseReference afterRef = appRoot.child("After");
                DatabaseReference startRef =  afterRef.child("Start");
                startRef.setValue(Helper.sdf.format(new Date(item.getValue().getNewStats().getFirstTimeStamp())));
                DatabaseReference endRef =  afterRef.child("End");
                endRef.setValue(Helper.sdf.format(new Date(item.getValue().getNewStats().getLastTimeStamp())));
                DatabaseReference timeRef =  afterRef.child("time");
                timeRef.setValue(Helper.getTimeSpent(item.getValue().getNewStats().getTotalTimeInForeground()));

                DatabaseReference beforeRef = appRoot.child("Before");
                beforeRef.setValue(Helper.getTimeSpent(item.getValue().getOldStats().getTotalTimeInForeground()));
                startRef =  beforeRef.child("Start");
                startRef.setValue(Helper.sdf.format(new Date(item.getValue().getOldStats().getFirstTimeStamp())));
                endRef =  beforeRef.child("End");
                endRef.setValue(Helper.sdf.format(new Date(item.getValue().getNewStats().getFirstTimeStamp())));
                timeRef =  beforeRef.child("time");
                timeRef.setValue(Helper.getTimeSpent(item.getValue().getOldStats().getTotalTimeInForeground() - item.getValue().getNewStats().getTotalTimeInForeground()));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

        }


    }
    private String getID(){
        String ID;

        ID = sharedPref.getString(ID_KEY,null);
        if(ID == null){
            ID = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(ID_KEY,ID);
            editor.commit();
        }
        return ID;
    }
    private DatabaseReference init(){
        // Write a message to the database
        if(database == null)
            database = FirebaseDatabase.getInstance();
        if(myRef == null)
            myRef= database.getReference();
        String version = sharedPref.getString(VERSION_KEY,null);
        DatabaseReference ret = myRef.child(getID());
        if(version != null ){
            if(version.equals(MainActivity.VERSION_CONTROL_KEY))
                ret= ret.child("version_in_control");
            else
                ret= ret.child("version_no_control");
        }
        return ret;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }
}
