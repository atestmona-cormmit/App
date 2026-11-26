package com.wolf.runner;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import com.janeasystems.rn_nodejs_mobile.NodeJS;
import java.util.HashMap;

public class NodeService extends Service {
    private NodeJS mNodeJS;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("ZIP_PATH")) {
            String zipPath = intent.getStringExtra("ZIP_PATH");
            String tokensPath = intent.getStringExtra("TOKENS_PATH");

            // عرض إشعار دائم لمنع النظام من إغلاق الخدمة
            Notification notification = new Notification.Builder(this, "WolfChannel")
                    .setContentTitle("Wolf Runner")
                    .setContentText("جاري تشغيل البوت في الخلفية...")
                    .setSmallIcon(android.R.drawable.stat_sys_download_done)
                    .build();
            startForeground(1, notification);

            startNodeEngine(zipPath, tokensPath);
        }
        return START_STICKY;
    }

    private void startNodeEngine(String zipPath, String tokensPath) {
        if (mNodeJS != null) return;

        mNodeJS = new NodeJS(this);
        
        // تمرير المسارات لبيئة Node.js
        HashMap<String, String> env = new HashMap<>();
        env.put("ZIP_PATH", zipPath);
        env.put("TOKENS_PATH", tokensPath);

        try {
            mNodeJS.start("main.js", "nodejs-project", env);
        } catch (Exception e) {
            Log.e("NodeService", "Error starting Node.js", e);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    "WolfChannel", "Wolf Runner Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mNodeJS != null) {
            mNodeJS.shutdown();
        }
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
