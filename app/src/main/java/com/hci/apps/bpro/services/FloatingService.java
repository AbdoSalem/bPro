package com.hci.apps.bpro.services;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.view.LayoutInflaterCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.andremion.counterfab.CounterFab;
import com.hci.apps.bpro.ListItemModel;
import com.hci.apps.bpro.LoggerManager;
import com.hci.apps.bpro.R;
import com.hci.apps.bpro.activities.MainActivity;

import java.util.List;

public class FloatingService extends Service {

    private static final String TAG = FloatingService.class.getSimpleName();
    private WindowManager mWindowManager;
    private View mOverlayView;
    CounterFab counterFab;
    WindowManager.LayoutParams params;
    public static FloatingService service;
    public FloatingService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
       return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    try {
        setTheme(R.style.AppTheme);
        LayoutInflater inflater = LayoutInflater.from(this);

        mOverlayView = inflater.inflate(R.layout.floating_widget, null);
        service = this;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1)
            startForeground(1, new Notification());
        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);

        } else {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }

        //Specify the view position
        params.gravity = Gravity.TOP | Gravity.LEFT;        //Initially view will be added to top-left corner
        params.x = 0;
        params.y = 100;


        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mOverlayView, params);


        counterFab = (CounterFab) mOverlayView.findViewById(R.id.fabHead);


        setState(LoggerManager.getInstance().queryThresholdAsList(this));

        counterFab.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            private boolean ismoved;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        //remember the initial position.
                        initialX = params.x;
                        initialY = params.y;
                        ismoved = false;

                        //get the touch location
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();


                        return true;
                    case MotionEvent.ACTION_UP:
                        if (!ismoved)
                            onWidgetClicked();


                        return true;
                    case MotionEvent.ACTION_MOVE:


                        float Xdiff = Math.round(event.getRawX() - initialTouchX);
                        float Ydiff = Math.round(event.getRawY() - initialTouchY);


                        //Calculate the X and Y coordinates of the view.
                        params.x = initialX + (int) Xdiff;
                        params.y = initialY + (int) Ydiff;

                        //Update the layout with new X & Y coordinates
                        mWindowManager.updateViewLayout(mOverlayView, params);
                        if (Xdiff > 10 || Ydiff > 10)
                            ismoved = true;

                        return true;
                }
                return false;
            }
        });
        counterFab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Log.d(TAG, "on click");
                onWidgetClicked();
            }
        });
        MainActivity.writeServiceState(this, true);
    }catch(Exception ex){

    }

    }

    private void onWidgetClicked() {

        Intent intent;
        if(counterFab.getCount()>0){
            intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(MainActivity.SHOW_ONLY_PASS_THRESHOLD,true);
            startActivity(intent);
        }else {
            intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        service = null;
        if (mOverlayView != null)
            mWindowManager.removeView(mOverlayView);
        MainActivity.writeServiceState(this,false);
    }

    public void setState(List<ListItemModel> list){
        if(counterFab!= null && !list.isEmpty()){
            //counterFab.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(255,0,0)));
            counterFab.setCount(list.size());
            counterFab.setImageDrawable(getDrawable(R.drawable.face));
        }else if (counterFab!= null){
            //counterFab.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(0,255,0)));
            counterFab.setImageDrawable(getDrawable(R.drawable.smiley_sourire));
            counterFab.setCount(0);
        }

    }
}
