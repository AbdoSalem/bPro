package com.hci.apps.bpro.services;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
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

    public static FloatingService service;
    public FloatingService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
       return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setTheme(R.style.AppTheme);
        mOverlayView = LayoutInflater.from(this).inflate(R.layout.floating_widget, null);
        service = this;

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);


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
                        ismoved=false;

                        //get the touch location
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();


                        return true;
                    case MotionEvent.ACTION_UP:
                        if(!ismoved)
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
                        if(Xdiff>10 || Ydiff >10)
                             ismoved = true;

                        return true;
                }
                return false;
            }
        });
        counterFab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Log.d(TAG,"on click");
                onWidgetClicked();
            }
        });
    }

    private void onWidgetClicked() {
        if(counterFab.getCount()>0){
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(MainActivity.SHOW_ONLY_PASS_THRESHOLD,true);
            startActivity(intent);
        }else
            startActivity(new Intent(this, MainActivity.class));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        service = null;
        if (mOverlayView != null)
            mWindowManager.removeView(mOverlayView);
    }

    public void setState(List<ListItemModel> list){
        if(!list.isEmpty()){
            //counterFab.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(255,0,0)));
            counterFab.setCount(list.size());
            counterFab.setImageDrawable(getDrawable(R.drawable.face));
        }else{
            //counterFab.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(0,255,0)));
            counterFab.setImageDrawable(getDrawable(R.drawable.smiley_sourire));
            counterFab.setCount(0);
        }

    }
}