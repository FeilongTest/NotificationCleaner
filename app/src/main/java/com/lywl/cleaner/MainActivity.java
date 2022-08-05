package com.lywl.cleaner;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.View;

import androidx.core.app.NotificationCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.lywl.cleaner.databinding.ActivityMainBinding;
import com.lywl.cleaner.utils.NotificationSetUtil;
import com.lywl.cleaner.utils.ServiceUtils;
import com.lywl.cleaner.utils.ShellUtils;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;


    String channel_id = "notificationChannelId"; //NotificationChannel的ID
    String channel_name = "notificationChannel的名称";  //NotificationChannel的名称
    String channel_desc = "notificationChannel的描述"; //NotificationChannel的描述
    String notification_title = "notification的标题";
    String notification_text = "notification的内容";
    int notificationId = 10086;

    NotificationManager notificationManager; //NotificationManager：是状态栏通知的管理类，负责发通知、清除通知等操作。

    private Context context;

    private String Tag = "lywl";
    private boolean isEnabledNLS = false;


    @Override
    protected void onResume() {
        super.onResume();
        isEnabledNLS = NotificationSetUtil.isOpenNotificationSetting(this);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        context = this;

        //创建通知渠道
        createNotificationChannel();

        binding.clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "开始尝试清理", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                NotificationSetUtil.cancelNotification(context,true);
            }
        });

        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NotificationSetUtil.isOpenNotificationSetting(context)){
                    Toast.makeText(context, "已开启通知权限", Toast.LENGTH_SHORT).show();

                    //发送通知
                    sendNotification();
                }
            }
        });

        binding.startService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });

        binding.bindService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listCurrentNotification();
                Toast.makeText(context, "尝试获取数据", Toast.LENGTH_SHORT).show();
            }
        });


        //开启关闭定时清理服务
        Intent clearService = new Intent(context,ClearService.class);

        binding.startMonitor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(clearService);
                Snackbar.make(v, "定期清理服务已开始", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                NotificationSetUtil.cancelNotification(context,true);
            }
        });

        binding.stopMonitor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(clearService);
                Snackbar.make(v, "定期清理服务已停止", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                NotificationSetUtil.cancelNotification(context,true);
            }
        });


    }




    private void listCurrentNotification() {
        String result = "";
        if (isEnabledNLS) {
            if (NotificationListener.getCurrentNotifications() == null) {
                Log.d("lywl","mCurrentNotifications.get(0) is null");
                return;
            }
            int n = NotificationListener.mCurrentNotificationsCounts;
            if (n == 0) {
                result = "为空";
            }else {
                result = "数量:"+n;
            }
            result = result + "\n" + getCurrentNotificationString();
            Log.d("lywl",result);
        }else {
            Log.d("lywl","请开启通知权限");
        }
    }

    private String getCurrentNotificationString() {
        String listNos = "";
        StatusBarNotification[] currentNos = NotificationListener.getCurrentNotifications();
        if (currentNos != null) {
            for (int i = 0; i < currentNos.length; i++) {
                listNos = i +" " + currentNos[i].getPackageName() + "\n" + listNos;
            }
        }
        return listNos;
    }

    /**
     * 创建通知渠道
     */
    private void createNotificationChannel() {
        //Android8.0(API26)以上需要调用下列方法，但低版本由于支持库旧，不支持调用
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            //获得通知渠道对象
            NotificationChannel channel = new NotificationChannel(channel_id, channel_name, importance);
            //通知渠道设置描述
            channel.setDescription(channel_desc);
            // 设置通知出现时声音，默认通知是有声音的
            channel.setSound(null, null);
            // 设置通知出现时的闪灯（如果 android 设备支持的话）
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            // 设置通知出现时的震动（如果 android 设备支持的话）
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            //设置自定义的提示音
            //获得NotificationManager对象
            notificationManager = (NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE);
            //在 notificationManager 中创建该通知渠道
            notificationManager.createNotificationChannel(channel);
        } else {//Android8.0(API26)以下
            notificationManager = (NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE);
        }
    }

    /**
     * 发送通知
     */
    private void sendNotification() {
        //定义一个PendingIntent点击Notification后启动一个Activity
        Intent it = new Intent(this, MainActivity.class);
        PendingIntent pit = PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE);

        //配置通知栏的各个属性
        Notification notification = new NotificationCompat.Builder(this, channel_id)
                .setContentTitle(notification_title) //标题
                .setContentText(notification_text) //内容
                .setWhen(System.currentTimeMillis()) //设置通知时间，不设置默认当前时间
                .setSmallIcon(R.mipmap.ic_launcher) //设置小图标
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE)    //设置默认的三色灯与振动器
                .setPriority(NotificationCompat.PRIORITY_HIGH)

                .setAutoCancel(true) //设置点击通知后，通知自动消失
                .setContentIntent(pit)  //设置PendingIntent
                .build();
        //用于显示通知，第一个参数为id，每个通知的id都必须不同。第二个参数为具体的通知对象
        notificationManager.notify(notificationId, notification);

        Toast.makeText(context, "发送通知成功！", Toast.LENGTH_SHORT).show();
    }



}