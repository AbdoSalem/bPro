package com.hci.apps.bpro;

import android.app.Application;
import android.app.usage.UsageStats;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by abdo on 28/11/17.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder>{

    List<ListItemModel> data = new ArrayList<>();
    private Context ctxt;

    public RecyclerAdapter( Context ctxt,boolean showPassed){
        if(showPassed)
            this.data = LoggerManager.getInstance().queryPassedDayAsList(ctxt);
        else
            this.data = LoggerManager.getInstance().queryDayAsList(ctxt);
        this.ctxt = ctxt;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ListItemModel item = data.get(position);
        holder.packageText.setText(item.getPackageName());
        Drawable icon = null;
        try {
            icon = ctxt.getPackageManager().getApplicationIcon(item.getPackageName());
            holder.iconView.setImageDrawable(icon);
            ApplicationInfo ai = ctxt.getPackageManager().getApplicationInfo(item.getPackageName(),0);
            holder.packageText.setText(ctxt.getPackageManager().getApplicationLabel(ai)== null?"(unknown)":ctxt.getPackageManager().getApplicationLabel(ai));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        long millis = item.getStats().getTotalTimeInForeground();
        String toDisplay =String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        );
        holder.statsText.setText(toDisplay);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView packageText;
        public TextView statsText;
        public ImageView iconView;
        public ViewHolder(View v) {
            super(v);
            packageText = (TextView) v.findViewById(R.id.tv_package);
            statsText = (TextView) v.findViewById(R.id.tv_stats);
            iconView = (ImageView) v.findViewById(R.id.imvw_icon);
        }
    }
}
