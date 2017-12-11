package com.hci.apps.bpro;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by abdo on 11/12/17.
 */

public class LoggerManager {

    private static final LoggerManager ourInstance = new LoggerManager();

    static LoggerManager getInstance() {
        return ourInstance;
    }

    private LoggerManager() {
    }
    public Map QueryForDay(Context ctxt){
        UsageStatsManager usageStatsManager = (UsageStatsManager) ctxt.getSystemService(Context.USAGE_STATS_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -1 * calendar.get(Calendar.HOUR_OF_DAY));
        calendar.add(Calendar.MINUTE, -1 * calendar.get(Calendar.MINUTE));
        calendar.add(Calendar.SECOND, -1 * calendar.get(Calendar.SECOND));
        //start time
        long start = calendar.getTimeInMillis();
        //end time
        long end = System.currentTimeMillis();
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        StringBuilder title = new StringBuilder();
        title.append("The start period is ");
        title.append(format1.format(calendar.getTime()));
        title.append(" till ");
        title.append(format1.format( new Date(end)));
        title.append(" number of apps to display is ");

        //get the application status starting from start time to end time
        return  usageStatsManager.queryAndAggregateUsageStats(start, end);
    }
    public List<ListItemModel> queryDayAsList(Context ctxt){
        Map<String,UsageStats> data = QueryForDay(ctxt);
        List<ListItemModel> returnList = new ArrayList<>(data.size());
        for (Map.Entry<String,UsageStats> item:data.entrySet()) {
            returnList.add(new ListItemModel(item.getKey(),item.getValue()));
        }
        Collections.sort(returnList);
        return returnList;
    }

}
