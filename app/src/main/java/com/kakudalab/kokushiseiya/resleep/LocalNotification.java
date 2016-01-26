package com.kakudalab.kokushiseiya.resleep;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

/**
 * ローカル通知を設定するクラス
 */
public class LocalNotification {

    /**
     * 通知をセットするメソッド
     * @param context
     * @param requestCode
     * @param hour
     * @param min
     */
    public static void setLocalNotification(Context context, int requestCode, int hour, int min, int when){
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("WHEN", when);
        PendingIntent sender = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        //calendar.setTimeInMillis(System.currentTimeMillis());
        if (hour >= 24){
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, hour - 24);
            calendar.set(Calendar.MINUTE, min);
        } else {
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, min);
        }

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
    }

    /**
     * 通知をキャンセルするメソッド
     * @param context
     * @param requestCode
     */
    public static void cancelLocalNotification(Context context, int requestCode){
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}
