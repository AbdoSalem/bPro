package com.hci.apps.bpro;

import android.app.usage.UsageStats;
import android.support.annotation.NonNull;

import java.util.Date;

/**
 * Created by abdo on 28/11/17.
 */

public class MapItemModel {
    private String packageName;
    private UsageStats oldStats;
    private UsageStats newStats;
    private Date startDate;
    private Date middleDate;
    private Date endDate;

    public MapItemModel(String packageName, UsageStats oldStats, UsageStats newStats, Date startDate, Date middleDate, Date endDate) {
        this.packageName = packageName;
        this.oldStats = oldStats;
        this.newStats = newStats;
        this.startDate = startDate;
        this.middleDate = middleDate;
        this.endDate = endDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getMiddleDate() {
        return middleDate;
    }

    public void setMiddleDate(Date middleDate) {
        this.middleDate = middleDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public UsageStats getOldStats() {
        return oldStats;
    }

    public void setOldStats(UsageStats stats) {
        this.oldStats = stats;
    }


    public UsageStats getNewStats() {
        return newStats;
    }

    public void setNewStats(UsageStats newStats) {
        this.newStats = newStats;
    }


}
