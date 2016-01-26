package com.kakudalab.kokushiseiya.resleep;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Calendar;
import java.util.TimeZone;

import greendao.DataFlag;
import greendao.DataFlagDao;
import greendao.Sensor;
import greendao.SensorDao;
import greendao.User;
import greendao.UserDao;

/**
 * Created by kokushiseiya on 15/12/24.
 */
public class DateChangedReceiver extends BroadcastReceiver {
    Context context;

    @Override
    public void onReceive(Context context, Intent intent){
        this.context = context;
        String action = intent.getAction();
        if(getDateChangedAction().equals(action)){
            onDateChanged(context);
            registerDateChangeReceiver(context);
        }
    }

    /**
     * 翌日の0時に {@link AlarmManager} をセットする
     */
    public static void registerDateChangeReceiver(Context context){
        unregisterDateChangeReceiver(context);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = createDateChangePendingIntent(context);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    /**
     * すでにセットされた Alarm を解除する
     */
    public static void unregisterDateChangeReceiver(Context context){
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(createDateChangePendingIntent(context));
    }

    /**
     * 日付変更の PendingIntent を生成する
     */
    private static PendingIntent createDateChangePendingIntent(Context context){
        Intent intent = new Intent(getDateChangedAction());
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    public static String getDateChangedAction() {
        return "DATE_CHANGED";
    }

    /**
     * 日付が変わった時の処理
     * @param context
     */
    public void onDateChanged(Context context){

    }

    private double loadSensorInfo(long id) {
        Sensor sensor = getSensorDao(context).loadByRowId(id);
        double data;
        if (sensor == null){
            data = 0.0;
        } else {
            data = sensor.getValue();
        }
        return data;
    }

    private static SensorDao getSensorDao(Context context){
        return ((ReSleepApplication)context.getApplicationContext()).getDaoSession().getSensorDao();
    }

    private int loadUserInfo(long id) {
        User user = getUserDao(context).loadByRowId(id);
        return Integer.valueOf(user.getText());
    }

    private void insertUserInfo(String txt, long date, String category, long id){
        User user = new User();
        user.setCategory(category);
        user.setDate(date);
        user.setText(txt);
        user.setId(id);
        getUserDao(context).insertOrReplace(user);
    }

    private static UserDao getUserDao(Context context){
        return ((ReSleepApplication)context.getApplicationContext()).getDaoSession().getUserDao();
    }

    private void insertFlagInfo(boolean is, long id){
        DataFlag flag = new DataFlag();
        flag.setId(id);
        flag.setIs(is);
        getFlagDao(context).insertOrReplace(flag);
    }

    private static DataFlagDao getFlagDao(Context context) {
        return ((ReSleepApplication)context.getApplicationContext()).getDaoSession().getDataFlagDao();
    }
}
