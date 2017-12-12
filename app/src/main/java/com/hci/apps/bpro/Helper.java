package com.hci.apps.bpro;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

/**
 * Created by abdo on 11/12/17.
 */

public class Helper {
    public static String  DATE_FORMAT_NOW = "yyyy-MM-dd";
    public static SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
    public static final int DRAW_OVER_OTHER_APP_PERMISSION = 123;

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
    public static void askForSystemOverlayPermission(Activity ctxt) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(ctxt)) {

            //If the draw over permission is not available to open the settings screen
            //to grant the permission.
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + ctxt.getPackageName()));
            ctxt.startActivityForResult(intent, DRAW_OVER_OTHER_APP_PERMISSION);
        }
    }
    public static int getPixels (Context ctxt,int dp){
        final float scale = ctxt.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
    public static LatLng location2LatLng(Location location){
        return new LatLng(location.getLatitude(),location.getLongitude());
    }
}
