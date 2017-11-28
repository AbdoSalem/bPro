package com.hci.apps.bpro;

import android.app.usage.UsageStats;

/**
 * Created by abdo on 28/11/17.
 */

public class ListItemModel {
    private String packageName;
    private UsageStats stats;

    public ListItemModel(String packageName, UsageStats stats) {
        this.packageName = packageName;
        this.stats = stats;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public UsageStats getStats() {
        return stats;
    }

    public void setStats(UsageStats stats) {
        this.stats = stats;
    }
}
