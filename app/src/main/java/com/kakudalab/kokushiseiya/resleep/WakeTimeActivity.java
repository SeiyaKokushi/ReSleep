package com.kakudalab.kokushiseiya.resleep;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

import greendao.Graph;
import greendao.GraphDao;
import greendao.Memo;
import greendao.MemoDao;
import greendao.User;
import greendao.UserDao;

/**
 * Created by kokushiseiya on 15/12/24.
 */
public class WakeTimeActivity extends Activity implements View.OnClickListener{
    TimePicker timePicker;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wake_time_frame);

        timePicker = (TimePicker)findViewById(R.id.wakeTimePicker);
        button = (Button)findViewById(R.id.startButton);
        button.setOnClickListener(this);
    }

    public void onClick(View view){
        if (view == button){
            insertUserInfo(getIntent().getStringExtra("NAME"), System.currentTimeMillis(), "NAME", 0L);
            outputUserInfo(getIntent().getStringExtra("NAME") + ", 名前");
            if (getIntent().getBooleanExtra("MALE", true) == true) {
                insertUserInfo("男", System.currentTimeMillis(), "MALE", 1L);
                outputUserInfo("男, 性別");
            }else{
                insertUserInfo("女", System.currentTimeMillis(), "MALE", 1L);
                outputUserInfo("女, 性別");
            }
            insertUserInfo(String.valueOf(getIntent().getIntExtra("OLD", 20)), System.currentTimeMillis(), "OLD", 2L);
            outputUserInfo(String.valueOf(getIntent().getIntExtra("OLD", 20)) + ", 年齢");
            insertUserInfo(String.valueOf(timePicker.getCurrentHour()), System.currentTimeMillis(), "WAKE_HOUR", 3L);
            outputUserInfo(String.valueOf(timePicker.getCurrentHour()) + ", wake_hour");
            insertUserInfo(String.valueOf(timePicker.getCurrentMinute()), System.currentTimeMillis(), "WAKE_MIN", 4L);
            outputUserInfo(String.valueOf(timePicker.getCurrentMinute()) + ", wake_min");
            insertUserInfo(String.valueOf(0), System.currentTimeMillis(), "CALORY", 9L);
            insertUserInfo(String.valueOf(0), System.currentTimeMillis(), "DAY", 10L);

            loadTimeInfo();
            setRange();
            setLocalNotification();

            //BandServiceの作成と起動
            Intent bandIntent = new Intent(this, BandService.class);
            bandIntent.setAction("bandStreaming");
            bandIntent.putExtra("preference", true);
            startService(bandIntent);

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            this.finish();
        }
    }

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


    private void loadTimeInfo(){

        User userWakeHour = getUserDao(this).loadByRowId(3L);
        int wakeHour = Integer.valueOf(userWakeHour.getText());

        User userWakeMin = getUserDao(this).loadByRowId(4L);
        int wakeMin = Integer.valueOf(userWakeMin.getText());

        int age = Integer.valueOf(getUserDao(this).loadByRowId(2L).getText());
        int sleepHour, sleepMin;

        sleepMin = wakeMin;
        if (age >= 18) {
            sleepHour = wakeHour - 7;
            insertUserInfo(String.valueOf(7), System.currentTimeMillis(), "SLEEPTIME_HOUR", 7L);
            outputUserInfo(String.valueOf(7) + ", sleeptime_hour");
            insertUserInfo(String.valueOf(0), System.currentTimeMillis(), "SLEEPTIME_MIN", 8L);
            outputUserInfo(String.valueOf(0) + ", sleeptime_min");
        } else {
            sleepHour = wakeHour - 8;
            insertUserInfo(String.valueOf(8), System.currentTimeMillis(), "SLEEPTIME_HOUR", 7L);
            outputUserInfo(String.valueOf(8) + ", sleeptime_hour");
            insertUserInfo(String.valueOf(0), System.currentTimeMillis(), "SLEEPTIME_MIN", 8L);
            outputUserInfo(String.valueOf(0) + ", sleeptime_min");
        }

        Toast.makeText(this, sleepHour + ", " + wakeHour, Toast.LENGTH_SHORT).show();
        if (sleepHour < wakeHour) {
            sleepHour += 24;
        }

        insertUserInfo(String.valueOf(sleepHour), System.currentTimeMillis(), "SLEEP_HOUR", 5L);
        outputUserInfo(String.valueOf(sleepHour) + ", sleep_hour");
        insertUserInfo(String.valueOf(sleepMin), System.currentTimeMillis(), "SLEEP_MIN", 6L);
        outputUserInfo(String.valueOf(sleepMin) + ", sleep_min");
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

    /**
     * 検知した行動をテキストファイルへの書き込みを行うメソッド
     * @param string 書き込む文字列
     */
    public void outputUserInfo(String string){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss");
        String nowDate = dateFormat.format(calendar.getTime());
        FileOutput fileOutput = new FileOutput();
        fileOutput.writeFile(this, nowDate + ", " + string, "user_info.txt");
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
