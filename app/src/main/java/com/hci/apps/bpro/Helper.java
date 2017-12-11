package com.hci.apps.bpro;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.concurrent.TimeUnit;

/**
 * Created by abdo on 11/12/17.
 */

public class Helper {
    public static String getApplicationName (String packageName, Context ctxt) throws PackageManager.NameNotFoundException {
        ApplicationInfo ai = ctxt.getPackageManager().getApplicationInfo(packageName,0);
        return  ctxt.getPackageManager().getApplicationLabel(ai)== null?"unknown":ctxt.getPackageManager().getApplicationLabel(ai).toString();
    }
    public static String getTimeSpent(long millis){
        return String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        );
    }
}
