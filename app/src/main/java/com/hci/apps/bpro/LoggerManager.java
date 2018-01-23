package com.hci.apps.bpro;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.hci.apps.bpro.activities.MainActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by abdo on 11/12/17.
 */

public class LoggerManager {
    private static final long THRESHOLD = 1000 * 60 * 60 * 2;
    private static final LoggerManager ourInstance = new LoggerManager();
    private static final String TAG = LoggerManager.class.getSimpleName();

    public static LoggerManager getInstance() {
        return ourInstance;
    }

    private LoggerManager() {
    }

    public Map queryForPeriod(Context ctxt, long start, long end) {
        UsageStatsManager usageStatsManager = (UsageStatsManager) ctxt.getSystemService(Context.USAGE_STATS_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(start);

        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        StringBuilder title = new StringBuilder();
        title.append("The start period is ");
        title.append(format1.format(calendar.getTime()));
        title.append(" till ");
        title.append(format1.format(new Date(end)));
        title.append(" number of apps to display is ");
        Log.d(TAG,title.toString());

        //get the application status starting from start time to end time
        return usageStatsManager.queryAndAggregateUsageStats(start, end);
    }

    public Map queryForDay(Context ctxt) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -1 * calendar.get(Calendar.HOUR_OF_DAY));
        calendar.add(Calendar.MINUTE, -1 * calendar.get(Calendar.MINUTE));
        calendar.add(Calendar.SECOND, -1 * calendar.get(Calendar.SECOND));
        //start time
        long start = calendar.getTimeInMillis();
        //end time
        long end = System.currentTimeMillis();
        return queryForPeriod(ctxt, start, end);
    }

    public List<ListItemModel> queryDayAsList(Context ctxt) {
        Map<String, UsageStats> data = queryForDay(ctxt);
        List<ListItemModel> returnList = new ArrayList<>(data.size());
        for (Map.Entry<String, UsageStats> item : data.entrySet()) {
            returnList.add(new ListItemModel(item.getKey(), item.getValue()));
        }
        Collections.sort(returnList);
        return returnList;
    }

    public List<ListItemModel> queryThresholdAsList(Context ctxt) {
        List<ListItemModel> all = queryDayAsList(ctxt);
        List<ListItemModel> exceededThreshold = new ArrayList<>();
        int points = ctxt.getSharedPreferences(
                ctxt.getString(R.string.preference_file_key), Context.MODE_PRIVATE).getInt(MainActivity.CHEAT_POINTS_KEY, 0);
        for (ListItemModel model : all) {
            if (model.getStats().getTotalTimeInForeground() > (THRESHOLD + (points * 1000 * 60 * 15)))
                exceededThreshold.add(model);
            else
                break;
        }
        return exceededThreshold;
    }

    public List<ListItemModel> queryPassedDayAsList(Context ctxt) {
        Map<String, UsageStats> data = queryForDay(ctxt);
        List<ListItemModel> returnList = new ArrayList<>(data.size());
        int points = ctxt.getSharedPreferences(
                ctxt.getString(R.string.preference_file_key), Context.MODE_PRIVATE).getInt(MainActivity.CHEAT_POINTS_KEY, 0);
        for (Map.Entry<String, UsageStats> item : data.entrySet()) {
            if (item.getValue().getTotalTimeInForeground() > (THRESHOLD + (points * 1000 * 60 * 15)))
                returnList.add(new ListItemModel(item.getKey(), item.getValue()));
        }
        Collections.sort(returnList);
        return returnList;
    }

    public Map<String,MapItemModel> queryMonthBeforeAsList(Context ctxt) {
        SharedPreferences sharedPref = ctxt.getSharedPreferences(
                ctxt.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String date = sharedPref.getString(MainActivity.FIRST_INSTALL_DATE_KEY, null);
        Map<String,MapItemModel> returnList = new HashMap<>();
        if (date == null) {
            return returnList;
        }
        try {
            Date firstInstall = Helper.sdf.parse(date);
            Date now = new Date();
            long diff= now.getTime()- firstInstall.getTime();
            long startlLong = firstInstall.getTime()-diff;
            Calendar Mnthb4Install = Calendar.getInstance();
            Mnthb4Install.setTimeInMillis(startlLong);


            Map<String, UsageStats> noUsedata = queryForPeriod(ctxt,startlLong,now.getTime());
            Map<String, UsageStats> usedata = queryForPeriod(ctxt,firstInstall.getTime(),now.getTime());

            List<ListItemModel> list = new ArrayList<>();
            for (Map.Entry<String, UsageStats> item : noUsedata.entrySet()) {
                list.add(new ListItemModel(item.getKey(), item.getValue()));
            }

            Collections.sort(list);
            if(list.size()>0) {
                for (int i = 0; i < 5; i++) {
                    ListItemModel noUseItem = list.get(i);
                    UsageStats useStats = usedata.get(noUseItem.getPackageName());
                    returnList.put(noUseItem.getPackageName(), new MapItemModel(noUseItem.getPackageName(), noUseItem.getStats(), useStats));
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return returnList;
    }


}
