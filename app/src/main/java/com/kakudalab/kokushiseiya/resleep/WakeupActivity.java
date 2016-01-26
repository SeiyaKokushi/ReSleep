package com.kakudalab.kokushiseiya.resleep;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

import greendao.DataFlag;
import greendao.DataFlagDao;
import greendao.Graph;
import greendao.GraphDao;
import greendao.Sensor;
import greendao.SensorDao;
import greendao.User;
import greendao.UserDao;

/**
 * Created by kokushiseiya on 15/12/30.
 */
public class WakeupActivity extends Activity {
    long sleepTime, cal, preCal, activityStart, activityEnd;
    private UpdateTextReceiver updateTextReceiver;
    private IntentFilter intentFilter;
    int day;
    private SharedPreferences preference;
    private SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wakeup);

        updateTextReceiver = new UpdateTextReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction("UPDATE_ACTION");
        registerReceiver(updateTextReceiver, intentFilter);
        updateTextReceiver.registerHandler(updateTextHandler);

        preference = getSharedPreferences("Preference Name", MODE_PRIVATE);
        editor = preference.edit();

        editor.putBoolean("Wake", false);
        editor.commit();

        sendBroadCast("STOP", 4);

        int sleepH = loadUserInfo(5L);
        int sleepM = loadUserInfo(6L);
        double sleepT = (double) sleepH + ((double) sleepM / 60.0);
        Calendar calendar = Calendar.getInstance();
        double currentT = (double) calendar.get(Calendar.HOUR_OF_DAY) + ((double) calendar.get(Calendar.MINUTE) / 60.0);

        if (currentT < sleepT) {
            NotificationManager notificationManager = (NotificationManager)this.getSystemService(Service.NOTIFICATION_SERVICE);
            notificationManager.cancel(4);
        }

        if (preference.getLong("SleepTime", 0) == 0) {
            final Calendar sleep = Calendar.getInstance();
            sleepTime = sleep.getTimeInMillis();
            editor.putLong("SleepTime", sleepTime);
            editor.commit();
            outputSleepTime("sleep");
        }

        day = loadUserInfo(10L);
        preCal = (long)loadUserInfo(9L);

        Button cantSleepButton = (Button) findViewById(R.id.cantSleepButton);
        cantSleepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                outputSleepTime("can't sleep");
                Intent intent = new Intent(getApplicationContext(), CantSleepActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Button button = (Button) findViewById(R.id.wakeButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadCast("STOP", 3);

                Calendar wake = Calendar.getInstance();
                long wakeTime = wake.getTimeInMillis();

                sleepTime = preference.getLong("SleepTime", 0);
                long sleepRange = wakeTime - sleepTime;

                outputSleepTime("wake");
                outputSleepTime("" + sleepRange);

                double sleepSec = sleepRange / 1000;
                double sleepMin = sleepSec / 60;
                double sleepHour = sleepMin / 60;

                Toast.makeText(getApplicationContext(), sleepSec + "", Toast.LENGTH_SHORT).show();

                int h = loadUserInfo(7L);
                int m = loadUserInfo(8L);
                double hour = (double) h + ((double) m / 60.0);

                double difference = sleepHour - hour;

                int sleepH = loadUserInfo(5L);
                int sleepM = loadUserInfo(6L);

                if (difference <= 2 && difference >= -2) {

                } else if (difference > 2) {
                    //実際の睡眠時間が長い
                    insertUserInfo(String.valueOf(sleepH + (int) difference - 2), System.currentTimeMillis(), "SLEEP_HOUR", 5L);
                    insertUserInfo(String.valueOf(sleepM), System.currentTimeMillis(), "SLEEP_MIN", 6L);
                    LocalNotification localNotification = new LocalNotification();
                    Calendar calendar = Calendar.getInstance();
                    localNotification.setLocalNotification(getApplicationContext(), 0, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), 11);
                    sleepH += (int) difference - 2;
                } else if (difference < -2) {
                    //実際の睡眠時間が短い
                    insertUserInfo(String.valueOf(sleepH + (int) difference + 2), System.currentTimeMillis(), "SLEEP_HOUR", 5L);
                    insertUserInfo(String.valueOf(sleepM), System.currentTimeMillis(), "SLEEP_MIN", 6L);
                    LocalNotification localNotification = new LocalNotification();
                    Calendar calendar = Calendar.getInstance();
                    localNotification.setLocalNotification(getApplicationContext(), 0, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), 12);
                    sleepH += (int) difference + 2;
                }

                setRange();
                setLocalNotification();

                long totalCal = cal + preCal;

                insertUserInfo(String.valueOf(totalCal), System.currentTimeMillis(), "CALORY", 9L);
                insertUserInfo(String.valueOf(day + 1), System.currentTimeMillis(), "DAY", 10L);


                editor.putBoolean("Wake", true);
                editor.commit();

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);

                Intent bandService = new Intent(getApplicationContext(), BandService.class);
                bandService.putExtra("CALORY", totalCal);
                bandService.putExtra("FROM", true);
                bandService.putExtra("DAY", day + 1);
                bandService.putExtra("SLEEP_HOUR", sleepH);
                bandService.putExtra("SLEEP_MIN", sleepM);
                startService(bandService);

                finish();
            }
        });
    }

    public synchronized void sleep(long msec) {
        try {
            wait(msec);
        } catch (InterruptedException e) {
        }
    }

    protected void sendBroadCast(String message, int id) {

        Intent broadcastIntent = new Intent();
        broadcastIntent.putExtra("MESSAGE", message);
        broadcastIntent.putExtra("ID", id);
        broadcastIntent.setAction("UPDATE_ACTION");
        getBaseContext().sendBroadcast(broadcastIntent);

    }

    private Handler updateTextHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String message = bundle.getString("MESSAGE");
            int id = bundle.getInt("ID");

            if (id == 2) {
                cal = Long.valueOf(message);
            }

        }
    };

    private void setRange(){
        double[] minValues = new double[7];
        double[] maxValues = new double[7];
        //bath sleep-6~sleep-0.5
        //nap 12~15
        //tabacco wake~sleep-1
        //alcohol sleep-6~sleep-4
        //cafe wake~sleep-6
        //eat wake~sleep-3
        //exercise wake~sleep-4
        int wakeHour = loadUserInfo(3L);
        int wakeMin = loadUserInfo(4L);
        int sleepHour = loadUserInfo(5L);
        int sleepMin = loadUserInfo(6L);

        minValues[0] = (double)sleepHour + ((double)sleepMin / 60.0) - 6.0;
        minValues[1] = 12.0;
        minValues[2] = (double)wakeHour + ((double)wakeMin / 60.0);
        minValues[3] = (double)sleepHour + ((double)sleepMin / 60.0) - 6.0;
        minValues[4] = (double)wakeHour + ((double)wakeMin / 60.0);
        minValues[5] = (double)wakeHour + ((double)wakeMin / 60.0);
        minValues[6] = (double)wakeHour + ((double)wakeMin / 60.0);

        maxValues[0] = (double)sleepHour + ((double)sleepMin / 60.0) - 0.5;
        maxValues[1] = 15.0;
        maxValues[2] = (double)sleepHour + ((double)sleepMin / 60.0) - 1.0;
        maxValues[3] = (double)sleepHour + ((double)sleepMin / 60.0) - 4.0;
        maxValues[4] = (double)sleepHour + ((double)sleepMin / 60.0) - 6.0;
        maxValues[5] = (double)sleepHour + ((double)sleepMin / 60.0) - 3.0;
        maxValues[6] = (double)sleepHour + ((double)sleepMin / 60.0) - 4.0;


        //0:exercise 1:eat 2:cafe 3:alcohol 4:tabacco 5:sleep 6:bath

        insertGraphInfo(minValues[6], 0L);
        insertGraphInfo(maxValues[6], 6L);
        insertGraphInfo(minValues[5], 1L);
        insertGraphInfo(maxValues[5], 7L);
        insertGraphInfo(minValues[4], 2L);
        insertGraphInfo(maxValues[4], 8L);
        insertGraphInfo(minValues[3], 3L);
        insertGraphInfo(maxValues[3], 9L);
        insertGraphInfo(minValues[2], 4L);
        insertGraphInfo(maxValues[2], 10L);
        insertGraphInfo(minValues[1], 5L);
        insertGraphInfo(maxValues[1], 11L);
    }

    @Override
    public void onDestroy(){
        unregisterReceiver(updateTextReceiver);
        super.onDestroy();
    }

    private void insertGraphInfo(double value, long id){
        Graph graph = new Graph();
        graph.setValue(value);
        graph.setId(id);
        getGraphDao(this).insertOrReplace(graph);
    }

    private static GraphDao getGraphDao(Context context){
        return ((ReSleepApplication)context.getApplicationContext()).getDaoSession().getGraphDao();
    }

    private int loadUserInfo(long id) {
        User user = getUserDao(this).loadByRowId(id);
        return Integer.valueOf(user.getText());
    }

    private void insertUserInfo(String txt, long date, String category, long id){
        User user = new User();
        user.setCategory(category);
        user.setDate(date);
        user.setText(txt);
        user.setId(id);
        getUserDao(this).insertOrReplace(user);
    }

    private static UserDao getUserDao(Context context){
        return ((ReSleepApplication)context.getApplicationContext()).getDaoSession().getUserDao();
    }

    private double loadSensorInfo(long id) {
        Sensor sensor = getSensorDao(getApplicationContext()).loadByRowId(id);
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

    private void insertFlagInfo(boolean is, long id){
        DataFlag flag = new DataFlag();
        flag.setId(id);
        flag.setIs(is);
        getFlagDao(getApplicationContext()).insertOrReplace(flag);
    }

    private static DataFlagDao getFlagDao(Context context) {
        return ((ReSleepApplication)context.getApplicationContext()).getDaoSession().getDataFlagDao();
    }

    @Override
    public void onResume() {
        Calendar calendar = Calendar.getInstance();
        activityStart = calendar.getTimeInMillis();

        super.onResume();
    }

    @Override
    public void onPause() {
        Calendar calendar = Calendar.getInstance();
        activityEnd = calendar.getTimeInMillis();

        long interactTime = activityEnd - activityStart;

        outputDetectedAction(interactTime + ", " + getClass().getSimpleName());

        super.onPause();
    }

    /**
     * interact時間をテキストファイルへの書き込みを行うメソッド
     * @param string 書き込む文字列
     */
    public void outputDetectedAction(String string){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss");
        String nowDate = dateFormat.format(calendar.getTime());
        FileOutput fileOutput = new FileOutput();
        fileOutput.writeFile(this, nowDate + ", " + string, "interact_time.txt");
    }

    /**
     * 睡眠時間をテキストファイルへの書き込みを行うメソッド
     * @param string 書き込む文字列
     */
    public void outputSleepTime(String string){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss");
        String nowDate = dateFormat.format(calendar.getTime());
        FileOutput fileOutput = new FileOutput();
        fileOutput.writeFile(this, nowDate + ", " + string, "sleep_time.txt");
    }

    public void setLocalNotification(){
        LocalNotification localNotification = new LocalNotification();
        Random rnd = new Random();
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);

        User userWakeHour = getUserDao(this).loadByRowId(3L);
        int wakeHour = Integer.valueOf(userWakeHour.getText());

        User userWakeMin = getUserDao(this).loadByRowId(4L);
        int wakeMin = Integer.valueOf(userWakeMin.getText());

        User userSleepHour = getUserDao(this).loadByRowId(5L);
        int sleepHour = Integer.valueOf(userSleepHour.getText());

        User userSleepMin = getUserDao(this).loadByRowId(6L);
        int sleepMin = Integer.valueOf(userSleepMin.getText());

        //朝の通知をセット
        //起床1時間後に通知
        if (currentHour <= wakeHour + 1) {
            localNotification.setLocalNotification(this, 0, wakeHour + 1, wakeMin, 0);
        }

        //日中の通知をセット
        //12時に通知
        if (currentHour <= 12) {
            localNotification.setLocalNotification(this, 0, 12, 0, 1);
        }

        //夜の通知をセット
        //就寝1時間前と就寝3時間前と就寝6時間前に通知
        if (currentHour < sleepHour - 6) {
            localNotification.setLocalNotification(this, 0, sleepHour - 6, sleepMin, 2);
        }
        if (currentHour < sleepHour - 3) {
            localNotification.setLocalNotification(this, 0, sleepHour - 3, sleepMin, 2);
        }
        if (currentHour < sleepHour - 1) {
            localNotification.setLocalNotification(this, 0, sleepHour - 1, sleepMin, 13);
        }

        //夜中の通知をセット
        //ユーザの目標としている就寝時間以降に起きていたら通知
        localNotification.setLocalNotification(this, 0, sleepHour, sleepMin, 3);

        //時間指定なしの通知をセット
        //朝と日中の通知の間と日中と夜の通知の間の2回通知
        if (12 - (wakeHour + 1) > 1){
            int dayTime = wakeHour + 2 + rnd.nextInt(12 - (wakeHour + 1) - 1);
            if (currentHour < dayTime) {
                localNotification.setLocalNotification(this, 0, dayTime, wakeMin, 4);
            }
        }

        if ((sleepHour - 6) - 12 > 1) {
            int dayTime2 = 13 + rnd.nextInt((sleepHour - 6) - 12 - 1);
            if (currentHour < dayTime2) {
                localNotification.setLocalNotification(this, 0, dayTime2, sleepMin, 4);
            }
        }

    }
}
