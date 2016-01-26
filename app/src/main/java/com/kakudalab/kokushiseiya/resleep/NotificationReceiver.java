package com.kakudalab.kokushiseiya.resleep;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.view.WindowManager;

/**
 * 通知を作成するレシーバー
 */
public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent){
        ChooseMessage chooseMessage = new ChooseMessage();

        Intent dialogIntent = new Intent(context, AlertDialogActivity.class);
        dialogIntent.putExtra("MESSAGE", chooseMessage.getMessage(intent.getIntExtra("WHEN", 4)));
        dialogIntent.putExtra("ID", intent.getIntExtra("WHEN", 4));
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, dialogIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentIntent(pendingIntent);
        builder.setTicker("ReSleepからのメッセージ");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("ReSleep");
        builder.setContentText("ReSleepからメッセージがあります");
        builder.setLargeIcon(largeIcon);
        builder.setWhen(System.currentTimeMillis());
        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE);
        //builder.setOngoing(true);

        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Service.NOTIFICATION_SERVICE);
        notificationManager.notify(intent.getIntExtra("WHEN", 4) + 1, builder.build());

        PowerManager.WakeLock wakelock = ((PowerManager) context.getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "disableLock");
        wakelock.acquire();
        wakelock.release();
    }
}
