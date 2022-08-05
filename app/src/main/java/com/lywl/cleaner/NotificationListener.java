package com.lywl.cleaner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


public class NotificationListener extends NotificationListenerService {
    private static final String TAG = "lywl";
    private static final String TAG_PRE = "[" + NotificationListener.class.getSimpleName() + "] ";
    private static final int EVENT_UPDATE_CURRENT_NOS = 0;
    public static final String ACTION_NLS_CONTROL = "com.seven.notificationlistenerdemo.NLSCONTROL";
    public static List<StatusBarNotification[]> mCurrentNotifications = new ArrayList<StatusBarNotification[]>();
    public static int mCurrentNotificationsCounts = 0;
    public static StatusBarNotification mPostedNotification;
    public static StatusBarNotification mRemovedNotification;
    private CancelNotificationReceiver mReceiver = new CancelNotificationReceiver();


    private Handler mMonitorHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_UPDATE_CURRENT_NOS:
                    updateCurrentNotifications();
                    break;
                default:
                    break;
            }
        }
    };


    class CancelNotificationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action;
            if (intent != null && intent.getAction() != null) {
                action = intent.getAction();
                if (action.equals(ACTION_NLS_CONTROL)) {
                    cancelAllNotifications();
                }
            }
        }

    }



    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_NLS_CONTROL);
        registerReceiver(mReceiver, filter);
        mMonitorHandler.sendMessage(mMonitorHandler.obtainMessage(EVENT_UPDATE_CURRENT_NOS));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    //当系统收到新的通知后出发回调
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        updateCurrentNotifications();
        logNLS("onNotificationPosted...");
        logNLS("have " + mCurrentNotificationsCounts + " active notifications");
        mPostedNotification = sbn;
    }

    //当系统通知被删掉后出发回调
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        updateCurrentNotifications();
        logNLS("removed...");
        logNLS("have " + mCurrentNotificationsCounts + " active notifications");
        mRemovedNotification = sbn;
    }

    public static StatusBarNotification[] getCurrentNotifications() {
        if (mCurrentNotifications.size() == 0) {
            logNLS("mCurrentNotifications size is ZERO!!");
            return null;
        }
        return mCurrentNotifications.get(0);
    }

    private void updateCurrentNotifications() {
        try {
            StatusBarNotification[] activeNos = getActiveNotifications();
            if (mCurrentNotifications.size() == 0) {
                mCurrentNotifications.add(null);
            }
            mCurrentNotifications.set(0, activeNos);
            mCurrentNotificationsCounts = activeNos.length;
        } catch (Exception e) {
            logNLS("Should not be here!!");
            e.printStackTrace();
        }
    }


    private static void logNLS(Object object) {
        Log.i(TAG, TAG_PRE + object);
    }


}
