package com.hci.apps.bpro;

import android.app.usage.UsageStats;
import android.support.annotation.NonNull;

/**
 * Created by abdo on 28/11/17.
 */

public class MapItemModel {
    private String packageName;
    private UsageStats oldStats;
    private UsageStats newStats;

    public MapItemModel(String packageName, UsageStats oldStats,UsageStats newStats) {
        this.packageName = packageName;
        this.oldStats = oldStats;
        this.newStats= newStats;
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
