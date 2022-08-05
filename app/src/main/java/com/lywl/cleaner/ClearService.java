package com.lywl.cleaner;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.style.TtsSpan;
import android.util.Log;

import com.lywl.cleaner.utils.NotificationSetUtil;

//后台清理服务
public class ClearService extends Service {

    private static final String ALARM_ACTION = "SAVE_HISTORY_DATA_ACTION";
    private static final int TIME_INTERVAL = 1000; // 1s = 1000  5min 1000 * 60 * 5
    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;

    //广播接收器
    private BroadcastReceiver alarmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //定时任务
            Log.i("lywl", "onReceive: 执行定时任务-清理通知栏");
            NotificationSetUtil.cancelNotification(context,true);

            //如果版本高于4.4，需要重新设置闹钟
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + TIME_INTERVAL, pendingIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + TIME_INTERVAL, pendingIntent);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter intentFilter = new IntentFilter(ALARM_ACTION);
        registerReceiver(alarmReceiver, intentFilter);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ALARM_ACTION), 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//6.0低电耗模式需要使用此方法才能准时触发定时任务
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {//4.4以上，使用此方法触发定时任务时间更为精准
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), pendingIntent);
        } else {//4.4以下，使用旧方法
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), TIME_INTERVAL, pendingIntent);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(alarmReceiver);
        alarmManager.cancel(pendingIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

